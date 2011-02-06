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
package org.sormula;

import java.util.Collection;
import java.util.List;

import org.sormula.annotation.Column;
import org.sormula.annotation.Row;
import org.sormula.annotation.Where;
import org.sormula.log.ClassLogger;
import org.sormula.operation.ArrayListSelectOperation;
import org.sormula.operation.DeleteOperation;
import org.sormula.operation.FullDelete;
import org.sormula.operation.FullInsert;
import org.sormula.operation.FullListSelect;
import org.sormula.operation.FullScalarSelect;
import org.sormula.operation.FullUpdate;
import org.sormula.operation.InsertOperation;
import org.sormula.operation.ListSelectOperation;
import org.sormula.operation.ScalarSelectOperation;
import org.sormula.operation.SelectCountOperation;
import org.sormula.operation.UpdateOperation;
import org.sormula.translator.NameTranslator;
import org.sormula.translator.NoNameTranslator;
import org.sormula.translator.RowTranslator;


/**
 * A table within a sql database. Contains a {@linkplain RowTranslator} for reading/writing
 * data form/to database and contains methods for common input/output operations. The RowTranslator
 * is created based upon fields within the row class of type R and from annoations for the
 * class. All common input/output methods use the primary key defined by {@linkplain Column#primaryKey()} 
 * annotation on class R.
 * 
 * @since 1.0
 * @author Jeff Miller
 * @param <R> type of row objects
 */
public class Table<R>
{
    private static final ClassLogger log = new ClassLogger();
    
    Database database;
    String tableName;
    RowTranslator<R> rowTranslator;
    NameTranslator nameTranslator;
    
    
    /**
     * Constructs for a database and the class that used for row objects. Creates
     * a {@linkplain RowTranslator} for mapping row objects to/from database.
     * 
     * @param database database for this table
     * @param rowClass row objects are of this type
     * @throws SormulaException if error
     */
    public Table(Database database, Class<R> rowClass) throws SormulaException
    {
        this.database = database;

        // table name from annoation
        Row rowAnnotation = rowClass.getAnnotation(Row.class);
        
        if (rowAnnotation != null)
        {
            // row annotation was available
            
            // table name
            tableName = rowAnnotation.tableName();
            
            try
            {
                nameTranslator = rowAnnotation.nameTranslator().newInstance();
            }
            catch (Exception e)
            {
                log.error("error creating name translator", e);
            }
        }
        
        if (nameTranslator == null)
        {
            // no row annotation or error creating custom name translator
            nameTranslator = new NoNameTranslator();
        }
        
        if (tableName == null || tableName.length() == 0) 
        {
            // no table name is provided, table name is class name
            tableName = nameTranslator.translate(rowClass.getSimpleName(), rowClass);
        }

        rowTranslator = new RowTranslator<R>(rowClass, nameTranslator);
        
        if (log.isDebugEnabled())
        {
            log.debug("nameTranslator=" + nameTranslator.getClass().getCanonicalName());
            log.debug("table name = " + tableName);
            log.debug("number of columns=" + rowTranslator.getColumnTranslatorList().size());
        }
    }


    /**
     * @return database supplied in constructor
     */
    public Database getDatabase()
    {
        return database;
    }


    /**
     * @return table name (without schema prefix)
     */
    public String getTableName()
    {
        return tableName;
    }


    /**
     * Gets table with optional schema prefix if necessary.
     *  
     * @return schema.tablename if schema is empty string; otherwise tablename
     */
    public String getQualifiedTableName()
    {
        String schema = database.getSchema();
        if (schema.length() > 0) return schema + "." + tableName;
        else return tableName;
    }

    
    /**
     * Gets translator defined by {@linkplain Row#nameTranslator()}.
     * 
     * @return translator for converting java names to sql names
     */
    public NameTranslator getNameTranslator()
    {
        return nameTranslator;
    }


    /**
     * @return row translator for this table
     */
    public RowTranslator<R> getRowTranslator()
    {
        return rowTranslator;
    }
    
    
    /**
     * Selects all rows in table.
     * 
     * @return list of all rows
     * @throws SormulaException if error
     */
    public List<R> selectAll() throws SormulaException
    {
        return new FullListSelect<R>(createSelectAllOperation()).executeAll();
    }
    
    
    /**
     * Selects one row in table using primary key. Primary key must be defined by one
     * or more {@linkplain Column#primaryKey()} annotations.
     * 
     * @param parameters primary key values to use for select (must be in same order as
     * primary key columns appear with row class)
     * @return row or null if none found
     * @throws SormulaException if error
     */
    public R select(Object... parameters) throws SormulaException
    {
        return new FullScalarSelect<R>(createSelectOperation()).execute(parameters);
    }
    
    
    /**
     * Creates operation to select all rows in table.
     * 
     * @return select operation
     * @throws SormulaException if error
     */
    public ListSelectOperation<R> createSelectAllOperation() throws SormulaException
    {
        return createSelectOperation("");
    }
    
    
    /**
     * Creates operation to select by primary key. Primary key must be defined by one
     * or more {@linkplain Column#primaryKey()} annotations.
     *  
     * @return select operation
     * @throws SormulaException if error
     */
    public ScalarSelectOperation<R> createSelectOperation() throws SormulaException
    {
        ScalarSelectOperation<R> selectOperation = new ScalarSelectOperation<R>(this);
        selectOperation.setWhere("primaryKey");
        return selectOperation;
    }
    
    
    /**
     * Creates operation for select by a named where condition.
     * 
     * @param whereConditionName name of where condition to use or use empty string to count all rows in table
     * @return select operation
     * @throws SormulaException if error
     * @see Where
     */
    public ListSelectOperation<R> createSelectOperation(String whereConditionName) throws SormulaException
    {
        ListSelectOperation<R> selectOperation = new ArrayListSelectOperation<R>(this);
        selectOperation.setWhere(whereConditionName);
        return selectOperation;
    }
    
    
    /**
     * @return count of all rows in table
     * @throws SormulaException if error
     */
    public int selectCount() throws SormulaException
    {
        return selectCount("");
    }
    
    
    /**
     * Selects count for a subset of rows.
     * 
     * @param whereConditionName name of where condition to use empty string to count all rows in table
     * @param parameters parameters for where condition
     * @return count of all rows in table
     * @throws SormulaException if error
     */
    public int selectCount(String whereConditionName, Object...parameters) throws SormulaException
    {
        SelectCountOperation<R> selectCountOperation = createSelectCountOperation(whereConditionName);
        
        if (parameters != null)
        {
            selectCountOperation.setParameters(parameters);
        }
        
        selectCountOperation.execute();
        int count = selectCountOperation.readCount();
        selectCountOperation.close();
        return count;
    }
    
    
    /**
     * Creates operation to select count of rows for a named where condition.
     * 
     * @param whereConditionName name of where condition to use empty string to count all rows in table
     * @return select operation
     * @throws SormulaException if error
     * @see Where
     */
    public SelectCountOperation<R> createSelectCountOperation(String whereConditionName) throws SormulaException
    {
        SelectCountOperation<R> selectCountOperation = new SelectCountOperation<R>(this);
        selectCountOperation.setWhere(whereConditionName);
        return selectCountOperation;
    }
    
    
    /**
     * Inserts one row into table.
     * 
     * @param row row to insert
     * @return count of rows affected
     * @throws SormulaException if error
     */
    public int insert(R row) throws SormulaException
    {
        return new FullInsert<R>(createInsertOperation()).execute(row);
    }
    
    
    /**
     * Inserts collection of rows.
     * 
     * @param rows rows to insert
     * @return count of rows affected
     * @throws SormulaException if error
     */
    public int insertAll(Collection<R> rows) throws SormulaException
    {
        return new FullInsert<R>(createInsertOperation()).executeAll(rows);
    }
    
    
    /**
     * Creates insert operation.
     * 
     * @return insert operation
     * @throws SormulaException if error
     */
    public InsertOperation<R> createInsertOperation() throws SormulaException
    {
        return new InsertOperation<R>(this);
    }
    
    
    /**
     * Updates one row in table by primary key. Primary key must be defined by one
     * or more {@linkplain Column#primaryKey()} annotations.
     * 
     * @param row row to update
     * @return count of rows affected
     * @throws SormulaException if error
     */
    public int update(R row) throws SormulaException
    {
        return new FullUpdate<R>(createUpdateOperation()).execute(row);
    }
    
    
    /**
     * Updates collection of rows using primary key. Primary key must be defined by one
     * or more {@linkplain Column#primaryKey()} annotations.
     * 
     * @param rows rows to update
     * @return count of rows affected
     * @throws SormulaException if error
     */
    public int updateAll(Collection<R> rows) throws SormulaException
    {
        return new FullUpdate<R>(createUpdateOperation()).executeAll(rows);
    }
    
    
    /**
     * Creates update by primary key operation. Primary key must be defined by one
     * or more {@linkplain Column#primaryKey()} annotations.
     * 
     * @return update operation
     * @throws SormulaException if error
     */
    public UpdateOperation<R> createUpdateOperation() throws SormulaException
    {
        return createUpdateOperation("primaryKey");
    }
    
    
    /**
     * Creates operation to update by named where condition.
     * 
     * @param whereConditionName name of where condition to use
     * @return update operation
     * @throws SormulaException if error
     */
    public UpdateOperation<R> createUpdateOperation(String whereConditionName) throws SormulaException
    {
        UpdateOperation<R> updateOperation = new UpdateOperation<R>(this);
        updateOperation.setWhere(whereConditionName);
        return updateOperation;
    }
    
    
    /**
     * Deletes by primary key. Primary key must be defined by one
     * or more {@linkplain Column#primaryKey()} annotations.
     * 
     * @param parameters where condition values to use for delete (must be in same order as
     * primary key fields appear with row class)
     * @return count of rows affected
     * @throws SormulaException if error
     */
    public int delete(Object... parameters) throws SormulaException
    {
        return new FullDelete<R>(createDeleteOperation()).executeObject(parameters);
    }
    
    
    /**
     * Deletes by primary key. Primary key must be defined by one
     * or more {@linkplain Column#primaryKey()} annotations.
     * 
     * @param row get primary key values from this row
     * @return count of rows affected
     * @throws SormulaException if error
     */
    public int delete(R row) throws SormulaException
    {
        return new FullDelete<R>(createDeleteOperation()).execute(row);
    }
    
    
    /**
     * Deletes many rows by primary key. Primary key must be defined by one
     * or more {@linkplain Column#primaryKey()} annotations.
     * 
     * @param rows get primary key values from each row in this collection
     * @return count of rows affected
     * @throws SormulaException if error
     */
    public int deleteAll(Collection<R> rows) throws SormulaException
    {
        return new FullDelete<R>(createDeleteOperation()).executeAll(rows);
    }
    
    
    /**
     * Deletes all rows in table.
     * 
     * @return count of rows affected
     * @throws SormulaException if error
     */
    public int deleteAll() throws SormulaException
    {
        return new FullDelete<R>(createDeleteAllOperation()).executeObject();
    }

    
    /**
     * Creates operation to delete all rows in table (no where condition).
     * 
     * @return delete operation
     * @throws SormulaException if error
     */
    public DeleteOperation<R> createDeleteAllOperation() throws SormulaException
    {
        return createDeleteOperation("");
    }
    
    
    /**
     * Creates operation to delete by primary key.
     * 
     * @return delete operation
     * @throws SormulaException if error
     */
    public DeleteOperation<R> createDeleteOperation() throws SormulaException
    {
        return createDeleteOperation("primaryKey");
    }
    
    
    /**
     * Creates delete operation to delete by named where condition.
     * 
     * @param whereConditionName name of where condition to use
     * @return delete operation
     * @throws SormulaException if error
     */
    public DeleteOperation<R> createDeleteOperation(String whereConditionName) throws SormulaException
    {
        DeleteOperation<R> deleteOperation = new DeleteOperation<R>(this);
        deleteOperation.setWhere(whereConditionName);
        return deleteOperation;
    }
}
