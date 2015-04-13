/* sormula - Simple object relational mapping
 * Copyright (C) 2011-2012 Jeff Miller
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.sormula.Table;
import org.sormula.annotation.cascade.DeleteCascade;
import org.sormula.annotation.cascade.DeleteCascadeAnnotationReader;
import org.sormula.cache.Cache;
import org.sormula.cache.CacheException;
import org.sormula.log.ClassLogger;
import org.sormula.operation.cascade.CascadeOperation;
import org.sormula.operation.cascade.DeleteCascadeOperation;
import org.sormula.reflect.RowField;


/**
 * SQL delete operation for row of type R. 
 *
 * @since 1.0
 * @author Jeff Miller
 * @param <R> class type which contains members for columns of a row in a table
 */
public class DeleteOperation<R> extends ModifyOperation<R>
{
    private static final ClassLogger log = new ClassLogger();
    
    
    /**
     * Constructs standard sql delete for primary key as:<br>
     * DELETE FROM table WHERE primary key clause
     * 
     * @param table delete from this table
     * @throws OperationException if error
     */
    public DeleteOperation(Table<R> table) throws OperationException
    {
        this(table, "primaryKey");
    }
    
    
    /**
     * Constructs for standard sql delete statement as:<br>
     * DELETE FROM table WHERE ...
     * 
     * @param table delete from this table
     * @param whereConditionName name of where condition to use ("primaryKey" to delete
     * by primary key; empty string to delete all rows in table)
     * @throws OperationException if error
     */
    public DeleteOperation(Table<R> table, String whereConditionName) throws OperationException
    {
        super(table);
        initBaseSql();
        setWhere(whereConditionName);
    }


    /**
     * Deletes a row. Set parameters, executes, closes. 
     * Alias for {@link #modify(Object)}.
     * 
     * @param row row to use for parameters
     * @return {@link #getRowsAffected()}
     * @throws OperationException if error
     * @since 1.4
     */
    public int delete(R row) throws OperationException
    {
        return super.modify(row);
    }


    /**
     * Deletes all rows in collection. Set parameters, executes, closes. 
     * Alias for {@link #modifyAll(Collection)}.
     * 
     * @param rows collection of rows to use as parameters 
     * @return {@link #getRowsAffected()}
     * @throws OperationException if error
     * @since 1.4
     */
    public int deleteAll(Collection<R> rows) throws OperationException
    {
        return super.modifyAll(rows);
    }


    /**
     * Deletes rows based upon parameters. Set parameters, executes, closes. 
     * Alias for {@link #modify(Object...)}.
     * 
     * @param parameters operation parameters as objects (see {@link #setParameters(Object...)})
     * @return count of rows affected
     * @throws OperationException if error
     * @since 1.4
     */
    public int delete(Object... parameters) throws OperationException
    {
        return super.modify(parameters);
    }


    /**
     * Does nothing since delete statements have no columns.
     */
    @Override
    protected void writeColumns(R row) throws OperationException
    {
        // no columns are prepared for delete
    }


    /**
     * Sets base sql with {@link #setBaseSql(String)}.
     */
    protected void initBaseSql()
    {
        String tableName = getTable().getQualifiedTableName();
        StringBuilder sql = new StringBuilder(tableName.length() + 50);
        
        sql.append("DELETE FROM ");
        sql.append(tableName);
        
        setBaseSql(sql.toString());
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected List<CascadeOperation<R, ?>> prepareCascades(Field field) throws OperationException
    {
        List<CascadeOperation<R, ?>> co = null;
        DeleteCascadeAnnotationReader car = new DeleteCascadeAnnotationReader(field);
        DeleteCascade[] deleteCascades = car.getDeleteCascades();
        
        if (deleteCascades.length > 0 && isRequiredCascade(car.getName()))
        {
            // at least one delete cascade and (unnamed or is required)
            if (log.isDebugEnabled()) log.debug("prepareCascades() for " + field.getName());
            @SuppressWarnings("unchecked") // target field type is not known at compile time
            Table<R> targetTable = (Table<R>)getTargetTable(car.getTargetClass(), field);
            RowField<R, ?> targetField = createTargetRowField(targetTable, field);
            co = new ArrayList<>(deleteCascades.length);
            
            // for each cascade operation
            for (DeleteCascade c: deleteCascades)
            {
                if (log.isDebugEnabled()) log.debug("prepare cascade " + c.operation());
                @SuppressWarnings("unchecked") // target field type is not known at compile time
                CascadeOperation<R, ?> operation = new DeleteCascadeOperation(getTable(), targetField, targetTable, c);
                operation.setNamedParameterMap(getNamedParameterMap());
                if (c.setForeignKeyValues()) operation.setForeignKeyFieldNames(car.getForeignKeyValueFields());
                if (c.setForeignKeyReference()) operation.setForeignKeyReferenceFieldName(car.getForeignKeyReferenceField());

                // cascade operation uses same required cascade names as this operation
                operation.setRequiredCascades(getRequiredCascades());

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


    /**
     * Tests if row is managed by cache. Delegates to {@link Cache#delete(Object)}.
     * 
     * @param row test this row
     * @return true if cache will delete the row from database when appropriate;
     * false if this operation should delete row from database
     * @throws OperationException if cache reports an error
     * @since 3.0
     */
    @Override
    protected boolean notifyCacheModify(R row) throws OperationException
    {
        try
        {
            return getTable().getCache().delete(row);
        }
        catch (CacheException e)
        {
            throw new OperationException("cache error", e);
        }
    }


    /**
     * Notifies cache that row has been deleted from database. Delegates to {@link Cache#deleted(Object)}.
     * 
     * @param row row that was deleted
     * @throws OperationException if cache reports an error
     * @since 3.0
     */
    @Override
    public void notifyCacheModified(R row) throws OperationException
    {
        try
        {
            getTable().getCache().deleted(row);
        }
        catch (CacheException e)
        {
            throw new OperationException("cache error", e);
        }
    }
}
