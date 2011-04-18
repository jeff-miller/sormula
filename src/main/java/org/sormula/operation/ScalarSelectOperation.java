/* sormula - Simple object relational mapping
 * Copyright (C) 2011 Jeff Miller
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.sormula.operation;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.sormula.Table;
import org.sormula.annotation.Column;
import org.sormula.annotation.OrderBy;
import org.sormula.annotation.Where;
import org.sormula.annotation.cascade.Cascade;
import org.sormula.annotation.cascade.OneToManyCascade;
import org.sormula.annotation.cascade.OneToOneCascade;
import org.sormula.annotation.cascade.SelectCascade;
import org.sormula.log.ClassLogger;
import org.sormula.operation.cascade.CascadeOperation;
import org.sormula.operation.cascade.SelectCascadeOperation;
import org.sormula.reflect.SormulaField;
import org.sormula.translator.OrderByTranslator;
import org.sormula.translator.RowTranslator;
import org.sormula.translator.TranslatorException;


/**
 * Select operation that reads one value at a time. It does not have any methods
 * that use {@link Collection} objects.
 * 
 * @since 1.0
 * @author Jeff Miller
 * @param <R> class type which contains members for columns of a row in a table
 */
public class ScalarSelectOperation<R> extends SqlOperation<R>
{
    private static final ClassLogger log = new ClassLogger();
    
    ResultSet resultSet;
    R rowParameters;
    String orderByName;
    OrderByTranslator<R> orderByTranslator;
    
    
    /**
     * Constructs for standard sql select statement as:<br>
     * SELECT c1, c2, c3, ... FROM table
     * 
     * @param table select from this table
     * @throws OperationException if error
     */
    public ScalarSelectOperation(Table<R> table) throws OperationException
    {
        super(table);
        initBaseSql();
    }
    
    
    /**
     * Set parameters using values from a row object. Use this instead of {@link #setParameters(Object...)}.
     * 
     * @param rowParameters where parameters are read from this object using fields definted in
     * {@link Where} or {@link Column#primaryKey()}.
     */
    public void setRowParameters(R rowParameters) 
    {
        this.rowParameters = rowParameters;
        
        // rowParameters and object parameters are mutually exclusive
        super.setParameters((Object)null); // note: must invoke super method
    }
    

    /**
     * {@inheritDoc}
     */
    @Override
    public void setParameters(Object... parameters)
    {
        super.setParameters(parameters);
        
        // rowParameters and object parameters are mutually exclusive
        rowParameters = null;
    }


    /**
     * Performs query. Use {@link #readNext()} to get the next row selected.
     *  
     * @throws OperationException if error
     */
    @Override
    public void execute() throws OperationException
    {
        prepareCheck();
        setNextParameter(1);
        
        if (rowParameters != null)
        {
            // where values from row object
            prepareWhere(rowParameters);
        }
        else 
        {
            // where values from objects
            prepareParameters();
        }
        
        try
        {
            log.debug("execute query");
            resultSet = preparedStatement.executeQuery();
        }
        catch (Exception e)
        {
            throw new OperationException("execute() error", e);
        }
    }
    

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws OperationException
    {
        try
        {
            if (resultSet != null)
            {
                resultSet.close();
                resultSet = null;
            }
        }
        catch (Exception e)
        {
            throw new OperationException("close() error", e);
        }
        
        super.close();
    }
    
    
    /**
     * Reads one row from current result set.
     * 
     * @return new instance of row or null if no more row in the current result set
     * @throws OperationException if error
     */
    public R readNext() throws OperationException
    {
        R row = null;
        
        try
        {
            if (resultSet.next())
            {
                row = table.getRowTranslator().newInstance();
                preReadCascade(row);
                preRead(row);
                table.getRowTranslator().read(resultSet, 1, row);
                postRead(row);
                postReadCascade(row);
            }
        }
        catch (Exception e)
        {
            throw new OperationException("readNext() error", e);
        }
        
        return row;
    }
    

    /**
     * Sets order of results in list. Setting the order condition affects the
     * order of the rows are read from database but is only meaningful if the 
     * resulting collection type is also ordered.
     * 
     * @param orderByName name of order phrase to use as defined in {@linkplain OrderBy#name()}
     * in {@link OrderBy} annotation on row R; use empty string for no ordering
     * @throws OperationException if error
     */
    public void setOrderBy(String orderByName) throws OperationException
    {
        this.orderByName = orderByName;
        
        if (orderByName.length() > 0)
        {
            try
            {
                orderByTranslator = new OrderByTranslator<R>(table.getRowTranslator(), orderByName);
            }
            catch (TranslatorException e)
            {
                throw new OperationException("can't create OrderTranslator for " + orderByName, e);
            }
        }
        else
        {
            orderByTranslator = null;
        }
    }
    
    
    /** 
     * Gets order by name set with {@link #setOrderBy(String)}.
     * 
     * @return order by name 
     */
    public String getOrderByName()
    {
        return orderByName;
    }
    
    
    /**
     * {@inheritDoc}
     * Order by clause is appended to super{@link #getSql()}.
     */
    @Override
    protected String getSql()
    {
        String sql = super.getSql();
        
        if (orderByTranslator != null)
        {
            // order by specified
            sql += " " + orderByTranslator.createSql();
        }
        
        return sql;
    }
    
    
    /**
     * Gets the result set of the most recent select.
     * 
     * @return jdbc result set
     */
    protected ResultSet getResultSet()
    {
        return resultSet;
    }


    /**
     * Sets the base sql with {@link #setBaseSql(String)}.
     */
    protected void initBaseSql()
    {
        String tableName = getTable().getQualifiedTableName();
        RowTranslator<R> rowTranslator = getTable().getRowTranslator();
        rowTranslator.setIncludeIdentityColumns(isIncludeIdentityColumns()); // usually true for selects
        String selectColumnPhrase = rowTranslator.createColumnPhrase(); 
        StringBuilder sql = new StringBuilder(selectColumnPhrase.length() + tableName.length() + 50);
        
        sql.append("SELECT ");
        sql.append(selectColumnPhrase);
        sql.append(" FROM ");
        sql.append(tableName);
        
        setBaseSql(sql.toString());
    }

    
    /**
     * Invoked prior to reading columns into row. Override to modify the row prior to jdbc reads.
     * Default implementaion does nothing.
     * 
     * @param row row that will get column values from table
     */
    protected void preRead(R row)
    {
    }
    
    
    /**
     * Invoked after reading columns into row. Override to modify the row after to jdbc reads.
     * Default implementaion does nothing.
     * 
     * @param row row that got column values from table
     */
    protected void postRead(R row)
    {
    }

    
    /**
     * Invoked prior to reading row. Performs all select cascade operations 
     * where {@linkplain SelectCascade#post()} is false.
     * 
     * @param row row that will get column values from table 
     */
    protected void preReadCascade(R row) throws OperationException
    {
        cascade(row, false);
    }


    /**
     * Invoked after reading row. Performs all select cascade operations 
     * where {@linkplain SelectCascade#post()} is true.
     * 
     * @param row row that got column values from table 
     */
    protected void postReadCascade(R row) throws OperationException
    {
        cascade(row, true);
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected List<CascadeOperation<R, ?>> prepareCascades(Field field) throws OperationException
    {
        List<CascadeOperation<R, ?>> co = null;
        Table<?> targetTable = null;
        SelectCascade[] selectCascades = null;
        
        if (field.isAnnotationPresent(OneToManyCascade.class))
        {
            OneToManyCascade cascadesAnnotation = field.getAnnotation(OneToManyCascade.class);
            targetTable = getTargetTable(cascadesAnnotation.targetClass(), field);
            selectCascades = cascadesAnnotation.selects();            
        }
        else if (field.isAnnotationPresent(OneToOneCascade.class))
        {
            OneToOneCascade cascadesAnnotation = field.getAnnotation(OneToOneCascade.class);
            targetTable = getTargetTable(field.getType(), field);
            selectCascades = cascadesAnnotation.selects();            
        }
        else if (field.isAnnotationPresent(Cascade.class))
        {
            Cascade cascadesAnnotation = field.getAnnotation(Cascade.class);
            targetTable = getTargetTable(cascadesAnnotation.targetClass(), field);
            selectCascades = cascadesAnnotation.selects();
        }
        
        if (targetTable != null && selectCascades != null)
        {
            SormulaField<R, ?> targetField = createTargetField(field);
            co = new ArrayList<CascadeOperation<R, ?>>(selectCascades.length);
            
            // for each cascade operation
            for (SelectCascade c: selectCascades)
            {
                @SuppressWarnings("unchecked") // target field type is not known at compile time
                CascadeOperation<R, ?> operation = new SelectCascadeOperation(targetField, targetTable, c);
                operation.prepare();
                co.add(operation);
            }
        }
        
        return co;
    }
}
