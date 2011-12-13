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
import org.sormula.annotation.cascade.Cascade;
import org.sormula.annotation.cascade.InsertCascade;
import org.sormula.annotation.cascade.OneToManyCascade;
import org.sormula.annotation.cascade.OneToOneCascade;
import org.sormula.operation.cascade.CascadeOperation;
import org.sormula.operation.cascade.InsertCascadeOperation;
import org.sormula.reflect.SormulaField;
import org.sormula.translator.ColumnTranslator;
import org.sormula.translator.RowTranslator;


/**
 * SQL insert operation for row of type R. 
 *
 * @since 1.0
 * @author Jeff Miller
 * @param <R> class type which contains members for columns of a row in a table
 */
public class InsertOperation<R> extends ModifyOperation<R>
{
    ColumnTranslator<R> identityColumnTranslator;
    
    
    /**
     * Constructs for standard sql insert statement as:<br>
     * INSERT INTO table (c1, c2, c3, ...) VALUES (?, ?, ?, ...)
     * 
     * @param table insert into this table
     * @throws OperationException if error
     */
    public InsertOperation(Table<R> table) throws OperationException
    {
        super(table);
        
        identityColumnTranslator = table.getRowTranslator().getIdentityColumnTranslator();
        if (identityColumnTranslator != null)
        {
            // has identity column
            setIncludeIdentityColumns(false);
            setAutoGeneratedKeys(true);
        }
        
        initBaseSql();
    }
    
    
    /**
     * Inserts a row. Set parameters, executes, closes. 
     * Alias for {@link #modify(Object)}.
     * 
     * @param row row to use for parameters
     * @return {@linkplain #getRowsAffected()}
     * @throws OperationException if error
     * @since 1.4
     */
    public int insert(R row) throws OperationException
    {
        return super.modify(row);
    }


    /**
     * Inserts all rows in collection. Set parameters, executes, closes. 
     * Alias for {@link #modifyAll(Collection)}.
     * 
     * @param rows collection of rows to use as parameters 
     * @return {@linkplain #getRowsAffected()}
     * @throws OperationException if error
     * @since 1.4
     */
    public int insertAll(Collection<R> rows) throws OperationException
    {
        return super.modifyAll(rows);
    }


    /**
     * Inserts rows based upon parameters. Set parameters, executes, closes. 
     * Alias for {@link #modify(Object...)}.
     * 
     * @param parameters operation parameters as objects (see {@linkplain #setParameters(Object...)})
     * @return count of rows affected
     * @throws OperationException if error
     * @since 1.4
     */
    public int insert(Object... parameters) throws OperationException
    {
        return super.modify(parameters);
    }

    
    /**
     * Sets base sql with {@link #setBaseSql(String)}.
     */
    protected void initBaseSql()
    {
        String tableName = getTable().getQualifiedTableName();
        RowTranslator<R> rowTranslator = getTable().getRowTranslator();
        rowTranslator.setIncludeIdentityColumns(isIncludeIdentityColumns()); // usually false for inserts that have identity columns 
        String columnPhrase = rowTranslator.createColumnPhrase();
        String unusedColumnNames = rowTranslator.getUnusedColumnInsertNamesSql();
        String parameterPhrase = rowTranslator.createParameterPhrase();
        String unusedColumnValues = rowTranslator.getUnusedColumnInsertValuesSql();
        
        StringBuilder sql = new StringBuilder(columnPhrase.length() + unusedColumnNames.length() +
                parameterPhrase.length() + unusedColumnValues.length() + tableName.length() + 50);
        
        sql.append("INSERT INTO ");
        sql.append(tableName);
        sql.append("(");
        sql.append(columnPhrase);
        sql.append(unusedColumnNames);
        sql.append(") VALUES (");
        sql.append(parameterPhrase);
        sql.append(unusedColumnValues);
        sql.append(")");
        
        setBaseSql(sql.toString());
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected List<CascadeOperation<R, ?>> prepareCascades(Field field) throws OperationException
    {
        List<CascadeOperation<R, ?>> co = null;
        Table<?> targetTable = null;
        InsertCascade[] insertCascades = null;
        
        if (field.isAnnotationPresent(OneToManyCascade.class))
        {
            OneToManyCascade cascadesAnnotation = field.getAnnotation(OneToManyCascade.class);
            
            if (!cascadesAnnotation.readOnly())
            {
                targetTable = getTargetTable(cascadesAnnotation.targetClass(), field);
                insertCascades = cascadesAnnotation.inserts();
            }
        }
        else if (field.isAnnotationPresent(OneToOneCascade.class))
        {
            OneToOneCascade cascadesAnnotation = field.getAnnotation(OneToOneCascade.class);
            
            if (!cascadesAnnotation.readOnly())
            {
                targetTable = getTargetTable(field.getType(), field);
                insertCascades = cascadesAnnotation.inserts();
            }
        }
        else if (field.isAnnotationPresent(Cascade.class))
        {
            Cascade cascadesAnnotation = field.getAnnotation(Cascade.class);
            targetTable = getTargetTable(cascadesAnnotation.targetClass(), field);
            insertCascades = cascadesAnnotation.inserts();
        }
        
        if (targetTable != null && insertCascades != null)
        {
            SormulaField<R, ?> targetField = createTargetField(field);
            co = new ArrayList<CascadeOperation<R, ?>>(insertCascades.length);
            
            // for each cascade operation
            for (InsertCascade c: insertCascades)
            {
                @SuppressWarnings("unchecked") // target field type is not known at compile time
                CascadeOperation<R, ?> operation = new InsertCascadeOperation(targetField, targetTable, c);
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
    
    
    @Override
    protected void postExecute(R row) throws OperationException
    {
        if (isAutoGeneratedKeys())
        {
            processIdentityColumn(row);
        }
    }
    
    
    protected void processIdentityColumn(R row) throws OperationException
    {
        try
        {
            // get generated identity value and set into row
            // (assume only 1 since most db's allow only one identity column)
            ResultSet rs = getPreparedStatement().getGeneratedKeys();
            if (rs.next())
            {
                identityColumnTranslator.read(rs, 1, row);
            }
        }
        catch (Exception e)
        {
            throw new OperationException("error getting auto generated keys", e);
        }
    }
}
