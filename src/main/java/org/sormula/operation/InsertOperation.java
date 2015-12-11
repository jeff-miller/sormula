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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.sormula.Table;
import org.sormula.annotation.cascade.InsertCascade;
import org.sormula.annotation.cascade.InsertCascadeAnnotationReader;
import org.sormula.cache.Cache;
import org.sormula.cache.CacheException;
import org.sormula.log.ClassLogger;
import org.sormula.operation.cascade.CascadeOperation;
import org.sormula.operation.cascade.InsertCascadeOperation;
import org.sormula.reflect.RowField;
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
    private static final ClassLogger log = new ClassLogger();
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
        this(table, true);
    }
    
    
    /**
     * Constructs for optional identity column. For rows with identity columns, typically
     * set identity parameter to true for auto generated keys, {@link #setAutoGeneratedKeys(boolean)}.
     * To insert a row with an identity column defined but without generating keys, set
     * identity parameter to false.
     * 
     * @param table insert into this table
     * @param identity true to use autogenerated keys for identity columns;
     * false to insert identity columns as without generating key  
     * @throws OperationException if error
     * @since 3.0
     */
    public InsertOperation(Table<R> table, boolean identity) throws OperationException
    {
        super(table);
        
        identityColumnTranslator = table.getRowTranslator().getIdentityColumnTranslator();
        if (identityColumnTranslator != null)
        {
            // has identity column
            setIncludeIdentityColumns(!identity);
            setAutoGeneratedKeys(identity);
        }
        
        initBaseSql();
    }
    
    
    /**
     * Inserts a row. Set parameters, executes, closes. 
     * Alias for {@link #modify(Object)}.
     * 
     * @param row row to use for parameters
     * @return {@link #getRowsAffected()}
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
     * @return {@link #getRowsAffected()}
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
     * @param parameters operation parameters as objects (see {@link #setParameters(Object...)})
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
        rowTranslator.setIncludeReadOnlyColumns(false);
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
        List<CascadeOperation<R, ?>> co;
        InsertCascadeAnnotationReader car = new InsertCascadeAnnotationReader(field);
        InsertCascade[] insertCascades = car.getInsertCascades();
        
        if (insertCascades.length > 0 && isRequiredCascade(car.getName()))
        {
            // at least one insert cascade and (unnamed or is required)
            if (log.isDebugEnabled()) log.debug("prepareCascades() for " + field.getName() + " cascade name=" + car.getName());
            @SuppressWarnings("unchecked") // target field type is not known at compile time
            Table<R> targetTable = (Table<R>)getTargetTable(car.getTargetClass());
            RowField<R, ?> targetField = createRowField(targetTable, field);
            co = new ArrayList<>(insertCascades.length);
            
            // for each cascade operation
            int nextCascadeDepth = getCascadeDepth() + 1;
            for (InsertCascade c: insertCascades)
            {
                if (log.isDebugEnabled()) log.debug("prepare cascade " + c.operation());
                @SuppressWarnings("unchecked") // target field type is not known at compile time
                CascadeOperation<R, ?> operation = new InsertCascadeOperation(getTable(), targetField, targetTable, c);
                operation.setDepth(nextCascadeDepth);
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
     * Updates identity column with generated key if {@link #isAutoGeneratedKeys()} is true.
     */
    @Override
    protected void postExecute(R row) throws OperationException
    {
        if (isAutoGeneratedKeys())
        {
            processIdentityColumn(row);
        }
    }
    
    
    /**
     * Reads the generated key using {@link PreparedStatement#getGeneratedKeys()}. The 
     * generated key is set in row using the {@link ColumnTranslator} from the table for 
     * this operation as {@link RowTranslator#getIdentityColumnTranslator()}.
     * 
     * @param row row to affect
     * @throws OperationException if error
     */
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


    /**
     * Tests if row is managed by cache. Delegates to {@link Cache#insert(Object)}.
     * 
     * @param row test this row
     * @return true if cache will insert the row to database when appropriate;
     * false if this operation should insert row into database
     * @throws OperationException if cache reports an error
     * @since 3.0
     */
    @Override
    protected boolean notifyCacheModify(R row) throws OperationException
    {
        try
        {
            return getTable().getCache().insert(row);
        }
        catch (CacheException e)
        {
            throw new OperationException("cache error", e);
        }
    }


    /**
     * Notifies cache that row has been inserted into database. Delegates to {@link Cache#inserted(Object)}.
     * 
     * @param row row that was inserted
     * @throws OperationException if cache reports an error
     * @since 3.0
     */
    @Override
    public void notifyCacheModified(R row) throws OperationException
    {
        try
        {
            getTable().getCache().inserted(row);
        }
        catch (CacheException e)
        {
            throw new OperationException("cache error", e);
        }
    }
}
