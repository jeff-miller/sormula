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
import java.util.Collections;
import java.util.List;

import org.sormula.Table;
import org.sormula.annotation.Column;
import org.sormula.annotation.OrderBy;
import org.sormula.annotation.OrderByAnnotationReader;
import org.sormula.annotation.Where;
import org.sormula.annotation.cascade.Cascade;
import org.sormula.annotation.cascade.OneToManyCascade;
import org.sormula.annotation.cascade.OneToOneCascade;
import org.sormula.annotation.cascade.SelectCascade;
import org.sormula.log.ClassLogger;
import org.sormula.operation.cascade.CascadeOperation;
import org.sormula.operation.cascade.SelectCascadeOperation;
import org.sormula.operation.monitor.OperationTime;
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
    RowTranslator<R> rowTranslator;
    int maximumRowsRead = Integer.MAX_VALUE;
    int rowsReadCount;
    
    
    /**
     * Constructs standard sql select by primary key as:<br>
     * SELECT c1, c2, c3, ... FROM table WHERE primary key clause
     * 
     * @param table select from this table
     * @throws OperationException if error
     */
    public ScalarSelectOperation(Table<R> table) throws OperationException
    {
        this(table, "primaryKey");
    }
    
    
    /**
     * Constructs standard sql select by primary key as:<br>
     * SELECT c1, c2, c3, ... FROM table WHERE ...
     * 
     * @param table select from this table
     * @param whereConditionName name of where condition to use ("primaryKey" to select
     * by primary key; empty string to select all rows in table)
     * @throws OperationException if error
     */
    public ScalarSelectOperation(Table<R> table, String whereConditionName) throws OperationException
    {
        super(table);
        rowTranslator = table.getRowTranslator();
        initBaseSql();
        setWhere(whereConditionName);
    }
    
    
    /**
     * Gets the maximum number of rows to read from result set. The default
     * is {@linkplain Integer#MAX_VALUE}.
     * 
     * @return 0..{@linkplain Integer#MAX_VALUE}
     * @since 1.4
     */
    public int getMaximumRowsRead()
    {
        return maximumRowsRead;
    }


    /**
     * Gets the maximum number of rows to read from result set. This method 
     * does NOT alter SQL to contain anything to limit query but only 
     * limits the number of rows read by {@link #readNext()} and 
     * {@link SelectOperation#readAll()}. Limiting rows read is usefull to avoid
     * reading too many rows and thus creating memory or display problems.
     * <P>
     * In the future, when more databases support a standard way to limit rows,
     * I will add support for SQL level limits through a method like 
     * "setMaximumRows(int)" and SQL "FETCH FIRST n ROWS ONLY".
     * 
     * @param maximumRowsRead 0..{@linkplain Integer#MAX_VALUE}
     * @since 1.4
     */
    public void setMaximumRowsRead(int maximumRowsRead)
    {
        this.maximumRowsRead = maximumRowsRead;
    }
    
    
    /**
     * Gets the count of rows that were read since the most recent {@link #execute()}.
     * 
     * @return number of rows read
     * @since 1.4
     */
    public int getRowsReadCount()
    {
        return rowsReadCount;
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
        initOperationTime();
        prepareCheck();
        setNextParameter(1);
        rowsReadCount = 0;
        OperationTime operationTime = getOperationTime();
        
        operationTime.startWriteTime();
        if (rowParameters != null)
        {
            // where values from row object
            writeWhere(rowParameters);
        }
        else 
        {
            // where values from objects
            writeParameters();
        }
        operationTime.stop();
        
        try
        {
            log.debug("execute query");
            operationTime.startExecuteTime();
            resultSet = preparedStatement.executeQuery();
            operationTime.stop();
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
            // include resultSet.next() in read time but don't include cascade 
            // times since that would cause cascade timings summed twice into total
            operationTime.startReadTime();
            
            if (resultSet.next() && rowsReadCount < maximumRowsRead)
            {
                row = rowTranslator.newInstance();
                operationTime.pause();
                preReadCascade(row);
                preRead(row);
                operationTime.resume();
                
                rowTranslator.read(resultSet, 1, row);
                ++rowsReadCount;
                operationTime.stop();
                
                postRead(row);
                postReadCascade(row);
            }
            else
            {
                // don't stop timer since count will be 1 more than rows read
                // ignore time for ResultSet.next when no more rows
                operationTime.cancel();
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
                // look for order annotation in operation, table class, row class (in that order)
                OrderBy orderByAnnotation = new OrderByAnnotationReader(
                        this.getClass(), table.getClass(), table.getRowClass()).getAnnotation(orderByName);
                if (orderByAnnotation != null)
                {
                    setOrderByTranslator(new OrderByTranslator<R>(table.getRowTranslator(), orderByAnnotation));
                }
                else
                {
                    throw new OperationException("no OrderBy annotation named, " + orderByName);
                }
            }
            catch (TranslatorException e)
            {
                throw new OperationException("can't create OrderByTranslator for " + orderByName, e);
            }
        }
        else
        {
            // no order by
            setOrderByTranslator(null);
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
     * Set parameters, executes, reads one row, closes.
     * 
     * @param parameters query parameters as objects (see {@linkplain #setParameters(Object...)})
     * @return {@linkplain #readNext()}
     * @throws OperationException if error
     * @since 1.4
     */
    public R select(Object... parameters) throws OperationException
    {
        setParameters(parameters);
        execute();
        R row = readNext();
        close();
        return row;
    }
    
    
    /**
     * Set parameters, executes, reads one row, closes.
     * 
     * @param whereParameters query parameters are read from an existing row object 
     * (see {@linkplain #setRowParameters(Object)})
     * @return {@linkplain #readNext()}
     * @throws OperationException if error
     * @since 1.4
     */
    public R select(R whereParameters) throws OperationException
    {
        setRowParameters(whereParameters);
        execute();
        R row = readNext();
        close();
        return row;
    }
    
    
    protected OrderByTranslator<R> getOrderByTranslator()
    {
        return orderByTranslator;
    }
    protected void setOrderByTranslator(OrderByTranslator<R> orderByTranslator)
    {
        this.orderByTranslator = orderByTranslator;
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
     * @return JDBC result set
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
     * Invoked prior to reading columns into row. Override to modify the row prior to JDBC reads.
     * Default implementaion does nothing.
     * 
     * @param row row that will get column values from table
     */
    protected void preRead(R row)
    {
    }
    
    
    /**
     * Invoked after reading columns into row. Override to modify the row after to JDBC reads.
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
        else
        {
            // no cascades
            co = Collections.emptyList();
        }
        
        return co;
    }
}
