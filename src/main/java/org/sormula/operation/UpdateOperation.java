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
import org.sormula.annotation.cascade.UpdateCascade;
import org.sormula.annotation.cascade.UpdateCascadeAnnotationReader;
import org.sormula.cache.Cache;
import org.sormula.cache.CacheException;
import org.sormula.log.ClassLogger;
import org.sormula.operation.cascade.CascadeOperation;
import org.sormula.operation.cascade.UpdateCascadeOperation;
import org.sormula.reflect.RowField;
import org.sormula.translator.RowTranslator;


/**
 * SQL update operation for row of type R. 
 *
 * @param <R> class type which contains members for columns of a row in a table
 * 
 * @since 1.0
 * @author Jeff Miller
 */
public class UpdateOperation<R> extends ModifyOperation<R>
{
    private static final ClassLogger log = new ClassLogger();
    
    
    /**
     * Constructs standard sql update for primary key as:<br>
     * UPDATE table SET c1=?, c2=?, c3... WHERE primary key clause
     * 
     * @param table update this table
     * @throws OperationException if error
     */
    public UpdateOperation(Table<R> table) throws OperationException
    {
        this(table, "primaryKey");
    }
    
    
    /**
     * Constructs for standard sql update statement as:<br>
     * UPDATE table SET c1=?, c2=?, c3... WHERE ...
     * 
     * @param table update this table
     * @param whereConditionName name of where condition to use ("primaryKey" to update
     * by primary key; empty string to update all rows in table)
     * @throws OperationException if error
     */
    public UpdateOperation(Table<R> table, String whereConditionName) throws OperationException
    {
        super(table);
        
        if (table.getRowTranslator().getIdentityColumnTranslator() != null)
        {
            // has identity column, don't update it
            setIncludeIdentityColumns(false);
        }
        
        initBaseSql();
        setWhere(whereConditionName);
    }


    /**
     * Updates a row. Set parameters, executes, closes. 
     * Alias for {@link #modify(Object)}.
     * 
     * @param row row to use for parameters
     * @return {@link #getRowsAffected()}
     * @throws OperationException if error
     * @since 1.4
     */
    public int update(R row) throws OperationException
    {
        return super.modify(row);
    }


    /**
     * Updates all rows in collection. Set parameters, executes, closes. 
     * Alias for {@link #modifyAll(Collection)}.
     * 
     * @param rows collection of rows to use as parameters 
     * @return {@link #getRowsAffected()}
     * @throws OperationException if error
     * @since 1.4
     */
    public int updateAll(Collection<R> rows) throws OperationException
    {
        return super.modifyAll(rows);
    }


    /**
     * Updates rows based upon parameters. Set parameters, executes, closes. 
     * Alias for {@link #modify(Object...)}.
     * 
     * @param parameters operation parameters as objects (see {@link #setParameters(Object...)})
     * @return count of rows affected
     * @throws OperationException if error
     * @since 1.4
     */
    public int update(Object... parameters) throws OperationException
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
        rowTranslator.setIncludeIdentityColumns(isIncludeIdentityColumns()); // usually false for updates that have identity column
        rowTranslator.setIncludeReadOnlyColumns(false);
        String columnParameterPhrase = rowTranslator.createColumnParameterPhrase();
        
        StringBuilder sql = new StringBuilder(columnParameterPhrase.length() +  tableName.length() + 50);
        sql.append("UPDATE ");
        sql.append(tableName);
        sql.append(" SET ");
        sql.append(columnParameterPhrase);
        
        setBaseSql(sql.toString());
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected List<CascadeOperation<R, ?>> prepareCascades(Field field) throws OperationException
    {
        List<CascadeOperation<R, ?>> co = null;
        UpdateCascadeAnnotationReader car = new UpdateCascadeAnnotationReader(field);
        UpdateCascade[] updateCascades = car.getUpdateCascades();
        
        if (updateCascades.length > 0 && isRequiredCascade(car.getName()))
        {
            // at least one update cascade and (unnamed or is required)
            if (log.isDebugEnabled()) log.debug("prepareCascades() for " + field.getName());
            @SuppressWarnings("unchecked") // target field type is not known at compile time
            Table<R> targetTable = (Table<R>)getTargetTable(car.getTargetClass());
            RowField<R, ?> targetField = createRowField(targetTable, field);
            co = new ArrayList<>(updateCascades.length);
            
            // for each cascade operation
            int nextCascadeDepth = getCascadeDepth() + 1;
            for (UpdateCascade c: updateCascades)
            {
                if (log.isDebugEnabled()) log.debug("prepare cascade " + c.operation());
                @SuppressWarnings("unchecked") // target field type is not known at compile time
                CascadeOperation<R, ?> operation = new UpdateCascadeOperation(this, targetField, targetTable, c);
                operation.setDepth(nextCascadeDepth);
                if (c.setForeignKeyValues()) operation.setForeignKeyFieldNames(car.getForeignKeyValueFields());
                if (c.setForeignKeyReference()) operation.setForeignKeyReferenceFieldName(car.getForeignKeyReferenceField());
                
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
     * Tests if row is managed by cache. Delegates to {@link Cache#update(Object)}.
     * 
     * @param row test this row
     * @return true if cache will update the row in database when appropriate;
     * false if this operation should update database
     * @throws OperationException if cache reports an error
     * @since 3.0
     */
    @Override
    protected boolean notifyCacheModify(R row) throws OperationException
    {
        try
        {
            return getTable().getCache().update(row);
        }
        catch (CacheException e)
        {
            throw new OperationException("cache error", e);
        }
    }


    /**
     * Notifies cache that row has been updated into database. Delegates to {@link Cache#updated(Object)}.
     * 
     * @param row row that was updated
     * @throws OperationException if cache reports an error
     * @since 3.0
     */
    @Override
    public void notifyCacheModified(R row) throws OperationException
    {
        try
        {
            getTable().getCache().updated(row);
        }
        catch (CacheException e)
        {
            throw new OperationException("cache error", e);
        }
    }
}
