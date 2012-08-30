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
package org.sormula.active;

import java.util.Collection;
import java.util.List;

import org.sormula.active.operation.Delete;
import org.sormula.active.operation.DeleteAll;
import org.sormula.active.operation.DeleteAllBatch;
import org.sormula.active.operation.Insert;
import org.sormula.active.operation.InsertAll;
import org.sormula.active.operation.InsertAllBatch;
import org.sormula.active.operation.Save;
import org.sormula.active.operation.SaveAll;
import org.sormula.active.operation.Select;
import org.sormula.active.operation.SelectAll;
import org.sormula.active.operation.SelectAllCustom;
import org.sormula.active.operation.SelectAllWhere;
import org.sormula.active.operation.SelectAllWhereOrdered;
import org.sormula.active.operation.SelectAvg;
import org.sormula.active.operation.SelectCount;
import org.sormula.active.operation.SelectCustom;
import org.sormula.active.operation.SelectMax;
import org.sormula.active.operation.SelectMin;
import org.sormula.active.operation.SelectSum;
import org.sormula.active.operation.SelectWhere;
import org.sormula.active.operation.Update;
import org.sormula.active.operation.UpdateAll;
import org.sormula.active.operation.UpdateAllBatch;
import org.sormula.annotation.Column;
import org.sormula.annotation.OrderBy;
import org.sormula.operation.ModifyOperation;


/**
 * Performs all database operations for {@link ActiveRecord} objects. This class is
 * used by {@link ActiveRecord} to perform single record operations. This class may also be 
 * created explicity to invoke collection methods like, {@link #saveAll(Collection)},
 * {@link #insertAll(Collection)}, {@link #updateAll(Collection)}, {@link #deleteAll(Collection)},
 * and select methods.
 * 
 * @author Jeff Miller
 * @since 1.7
 * 
 * @param <R> record type
 */
public class ActiveTable<R extends ActiveRecord<R>>
{
    final ActiveDatabase activeDatabase;
    final Class<R> recordClass;
    
    
    /**
     * Constructs for default active database and a record type. Uses the default active database
     * that is configured with {@link ActiveDatabase#setDefault(ActiveDatabase)}. The default
     * active database must be configured prior to first database access by this table.
     * 
     * @param recordClass Java class of record
     * @throws ActiveException if error
     */
    public ActiveTable(Class<R> recordClass) throws ActiveException
    {
        this.activeDatabase = null; // ActiveOperation constructor will initialize with ActiveDatabase.getDefault 
        this.recordClass = recordClass;
    }
    
    
    /**
     * Constructs for a database and record type.
     * 
     * @param activeDatabase active database that defines data source for records
     * @param recordClass Java class of record
     * @throws ActiveException if activeDatabase is null
     */
    public ActiveTable(ActiveDatabase activeDatabase, Class<R> recordClass) throws ActiveException
    {
        // fail here since activeDatabase must be known; use ActiveTable(Class<R> recordClass) to use default
        if (activeDatabase == null) throw new ActiveException("no active database for " + recordClass);
        
        this.activeDatabase = activeDatabase;
        this.recordClass = recordClass;
    }


    /**
     * Gets the active database.
     * 
     * @return active database that was supplied in constructor
     */
    public ActiveDatabase getActiveDatabase()
    {
        return activeDatabase;
    }

    
    /**
     * Gets the record class.
     * 
     * @return record class supplied in constructor
     */
    public Class<R> getRecordClass()
    {
        return recordClass;
    }


    /**
     * Creates a new active record of type R using zero-arg constructor. New
     * record is attached to this tables active database with
     * {@link ActiveRecord#attach(ActiveDatabase)}.
     * 
     * @return new active record
     * @throws ActiveException if error
     */
    public R newActiveRecord() throws ActiveException
    {
        R record;
        
        try
        {
            record = recordClass.newInstance();
            record.attach(activeDatabase);
        }
        catch (Exception e)
        {
            throw new ActiveException("error creating active record instance for " + recordClass.getName() +
                    "; make sure active record has public zero-arg constructor", e);
        }
        
        return record;
    }
    
    
    /**
     * Selects one record in table using primary key. Primary key must be defined by one
     * or more {@link Column#primaryKey()} annotations. 
     * {@link ActiveRecord#attach(ActiveDatabase)} is invoked on selected record to
     * attach it to the active database of this table.
     * <p>
     * Example:
     * <blockquote><pre>
     * ActiveDatabase activeDatabase = ...
     * ActiveTable&lt;Order&gt; table = new ActiveTable&lt;Order&gt;(activeDatabase, Order.class);
     * int orderNumber = 123456; // primary key
     * Order order = table.select(orderNumber);
     * </pre></blockquote>
     * @param primaryKeys primary key values to use for select (must be in same order as
     * primary key columns appear with record class)
     * @return record or null if none found
     * @throws ActiveException if error
     */
    public R select(Object... primaryKeys) throws ActiveException
    {
        return new Select<R>(this, primaryKeys).execute();
    }
    
    
    /**
     * Selects one record for where condition and parameters.
     * {@link ActiveRecord#attach(ActiveDatabase)} is invoked on selected record to
     * attach it to the active database of this table.
     * 
     * @param whereConditionName name of where condition to use; empty string to select arbitrary record in table
     * @param parameters parameter values for where condition
     * @return record for where condition and parameters; null if none found
     * @throws ActiveException if error
     */
    public R selectWhere(String whereConditionName, Object...parameters) throws ActiveException
    {
        return new SelectWhere<R>(this, whereConditionName, parameters).execute();
    }
    
    
    /**
     * Select list of records using custom sql. {@link ActiveRecord#attach(ActiveDatabase)} is invoked on selected record to
     * attach it to the active database of this table.
     * <p>
     * Example:
     * <blockquote><pre>
     * ActiveDatabase activeDatabase = ...
     * ActiveTable&lt;Order&gt; table = new ActiveTable&lt;Order&gt;(activeDatabase, Order.class);
     * List&ltOrder&gt; orders = table.selectAllCustom("where orderdate >= '2011-01-01'");
     * </pre></blockquote>
     * @param customSql custom sql to be appended to base sql (for example, "where somecolumn=?")
     * @param parameters parameter value to be set in customSql
     * @return record or null if none found
     * @throws ActiveException if error
     */
    public R selectCustom(String customSql, Object... parameters) throws ActiveException
    {
        return new SelectCustom<R>(this, customSql, parameters).execute();
    }
    
    
    /**
     * Selects all records in table.
     * {@link ActiveRecord#attach(ActiveDatabase)} is invoked on selected records to
     * attach them to the active database of this table.
     * <p>
     * Example:
     * <blockquote><pre>
     * ActiveDatabase activeDatabase = ...
     * ActiveTable&lt;Order&gt; table = new ActiveTable&lt;Order&gt;(activeDatabase, Order.class);
     * List&ltOrder&gt; orders = table.selectAll();
     * </pre></blockquote>
     * @return list of all records; empty list if none found
     * @throws ActiveException if error
     */
    public List<R> selectAll() throws ActiveException
    {
        return new SelectAll<R>(this).execute();
    }
    
    
    /**
     * Selects list of records for where condition and parameters.
     * {@link ActiveRecord#attach(ActiveDatabase)} is invoked on selected records to
     * attach them to the active database of this table.
     * 
     * @param whereConditionName name of where condition to use; empty string to select all records in table
     * @param parameters parameter values for where condition
     * @return records for where condition and parameters; empty list if none found
     * @throws ActiveException if error
     */
    public List<R> selectAllWhere(String whereConditionName, Object... parameters) throws ActiveException
    {
        return new SelectAllWhere<R>(this, whereConditionName, parameters).execute();
    }
    
    
    /**
     * Selects list of records for where condition and parameters.
     * {@link ActiveRecord#attach(ActiveDatabase)} is invoked on selected records to
     * attach them to the active database of this table.
     * 
     * @param whereConditionName name of where condition to use; empty string to select all records in table
     * @param orderByName name of order phrase to use as defined in {@link OrderBy#name()}
     * @param parameters parameter values for where condition
     * @return records for where condition and parameters; empty list if none found
     * @throws ActiveException if error
     */
    public List<R> selectAllWhereOrdered(String whereConditionName, String orderByName, Object... parameters) throws ActiveException
    {
        return new SelectAllWhereOrdered<R>(this, whereConditionName, orderByName, parameters).execute();
    }
    
    
    /**
     * Select list of records using custom sql.
     * {@link ActiveRecord#attach(ActiveDatabase)} is invoked on selected records to
     * attach them to the active database of this table.
     * <p>
     * Example:
     * <blockquote><pre>
     * ActiveDatabase activeDatabase = ...
     * ActiveTable&lt;Order&gt; table = new ActiveTable&lt;Order&gt;(activeDatabase, Order.class);
     * List&ltOrder&gt; orders = table.selectAllCustom("where orderdate >= '2011-01-01'");
     * </pre></blockquote>
     * @param customSql custom sql to be appended to base sql (for example, "where somecolumn=?")
     * @param parameters parameter value to be set in customSql
     * @return list of records selected; empty list if none found
     * @throws ActiveException if error
     */
    public List<R> selectAllCustom(String customSql, Object... parameters) throws ActiveException
    {
        return new SelectAllCustom<R>(this, customSql, parameters).execute();
    }
    
    
    /**
     * Selects count of records using "select count(*) from ...".
     * <p>
     * The data type returned from database is the same type as the expression. The
     * return type is database dependent since expression is "*".
     * 
     * @param <T> aggregate result type
     * @return count of records for expression 
     * @throws ActiveException if error
     */
    public <T> T selectCount() throws ActiveException
    {
        return new SelectCount<R, T>(this).execute();
    }
    
    
    /**
     * Selects count of records.
     * <p>
     * The data type returned from database is the same type as the expression. For example,
     * if expression is a column, then the returned type is the same type as column. If
     * expression is "*", then return types are database dependent.
     * 
     * @param <T> aggregate result type
     * @param expression expression to use as parameter to function; typically it is the name of a column
     * @return count of records for expression 
     * @throws ActiveException if error
     */
    public <T> T selectCount(String expression) throws ActiveException
    {
        return new SelectCount<R, T>(this, expression).execute();
    }
    
    
    /**
     * Selects count of records.
     * <p>
     * The data type returned from database is the same type as the expression. For example,
     * if expression is a column, then the returned type is the same type as column. If
     * expression is "*", then return types are database dependent.
     * 
     * @param <T> aggregate result type
     * @param expression expression to use as parameter to function; typically it is the name of a column
     * @param whereConditionName name of where condition to use; empty string to count all records in table
     * @param parameters parameter values for where condition
     * @return count of records for expression and where condition
     * @throws ActiveException if error
     */
    public <T> T selectCount(String expression, String whereConditionName, Object... parameters) throws ActiveException
    {
        return new SelectCount<R, T>(this, expression, whereConditionName, parameters).execute();
    }
    
    
    /**
     * Selects minimum value.
     * 
     * @param <T> aggregate result type
     * @param expression expression to use as parameter to function; typically it is the name of a column
     * @return minimum value for expression  
     * @throws ActiveException if error
     */
    public <T> T selectMin(String expression) throws ActiveException
    {
        return new SelectMin<R, T>(this, expression).execute();
    }
    
    
    /**
     * Selects minimum value.
     * 
     * @param <T> aggregate result type
     * @param expression expression to use as parameter to function; typically it is the name of a column
     * @param whereConditionName name of where condition to use; empty string to count all records in table
     * @param parameters parameter values for where condition
     * @return minimum value for expression and where condition 
     * @throws ActiveException if error
     */
    public <T> T selectMin(String expression, String whereConditionName, Object... parameters) throws ActiveException
    {
        return new SelectMin<R, T>(this, expression, whereConditionName, parameters).execute();
    }
    
    
    /**
     * Selects maximum value.
     * 
     * @param <T> aggregate result type
     * @param expression expression to use as parameter to function; typically it is the name of a column
     * @return maximum value for expression  
     * @throws ActiveException if error
     */
    public <T> T selectMax(String expression) throws ActiveException
    {
        return new SelectMax<R, T>(this, expression).execute();
    }
    
    
    /**
     * Selects maximum value.
     * 
     * @param <T> aggregate result type
     * @param expression expression to use as parameter to function; typically it is the name of a column
     * @param whereConditionName name of where condition to use; empty string to count all records in table
     * @param parameters parameter values for where condition
     * @return maximum value for expression and where condition 
     * @throws ActiveException if error
     */
    public <T> T selectMax(String expression, String whereConditionName, Object... parameters) throws ActiveException
    {
        return new SelectMax<R, T>(this, expression, whereConditionName, parameters).execute();
    }
    
    
    /**
     * Selects sum.
     * 
     * @param <T> aggregate result type
     * @param expression expression to use as parameter to function; typically it is the name of a column
     * @return sum for expression  
     * @throws ActiveException if error
     */
    public <T> T selectSum(String expression) throws ActiveException
    {
        return new SelectSum<R, T>(this, expression).execute();
    }
    
    
    /**
     * Selects sum.
     * 
     * @param <T> aggregate result type
     * @param expression expression to use as parameter to function; typically it is the name of a column
     * @param whereConditionName name of where condition to use; empty string to count all records in table
     * @param parameters parameter values for where condition
     * @return sum for expression and where condition 
     * @throws ActiveException if error
     */
    public <T> T selectSum(String expression, String whereConditionName, Object...parameters)
    {
        return new SelectSum<R, T>(this, expression, whereConditionName, parameters).execute();
    }
    
    
    /**
     * Selects average value.
     * 
     * @param <T> aggregate result type
     * @param expression expression to use as parameter to function; typically it is the name of a column
     * @return average value for expression  
     * @throws ActiveException if error
     */
    public <T> T selectAvg(String expression) throws ActiveException
    {
        return new SelectAvg<R, T>(this, expression).execute();
    }
    
    
    /**
     * Selects average value.
     * 
     * @param <T> aggregate result type
     * @param expression expression to use as parameter to function; typically it is the name of a column
     * @param whereConditionName name of where condition to use; empty string to count all records in table
     * @param parameters parameter values for where condition
     * @return average value for expression and where condition 
     * @throws ActiveException if error
     */
    public <T> T selectAvg(String expression, String whereConditionName, Object...parameters) throws ActiveException
    {
        return new SelectAvg<R, T>(this, expression, whereConditionName, parameters).execute();
    }
    
    
    /**
     * Updates an existing record or insert record if it is not already in database.
     * {@link ActiveRecord#attach(ActiveDatabase)} is invoked on record to
     * attach it to the active database of this table prior to save.
     * Typically {@link ActiveRecord#save()} is used instead of this method.
     * 
     * @param record record to save
     * @return count of records affected
     * @throws ActiveException if error
     */
    public int save(R record) throws ActiveException
    {
        return new Save<R>(this, record).execute();
    }

    
    /**
     * Updates an existing records or insert records if they are not already in database.
     * {@link ActiveRecord#attach(ActiveDatabase)} is invoked on records to
     * attach them to the active database of this table prior to saving them.
     * 
     * @param records collection of new and/or existing records to save (may be mixture of new and existing)
     * @return count of records affected
     * @throws ActiveException if error
     */
    public int saveAll(Collection<R> records) throws ActiveException
    {
        return new SaveAll<R>(this, records).execute();
    }

    
    /**
     * Inserts one record into table. 
     * {@link ActiveRecord#attach(ActiveDatabase)} is invoked on record to
     * attach it to the active database of this table prior to insert.
     * Typically {@link ActiveRecord#insert()} is used instead of this method.
     * <p>
     * Example:
     * <blockquote><pre>
     * ActiveDatabase activeDatabase = ...
     * ActiveTable&lt;Student&gt; table = new ActiveTable&lt;Student&gt;(activeDatabase, Student.class);
     * Student s = new Student();
     * s.setId(1234);
     * s.setFirstName("Jeff");
     * s.setLastName("Miller");
     * s.setGraduationDate(new Date(System.currentTimeMillis()));
     * table.insert(s);
     * </pre></blockquote>
     * @param record record to insert
     * @return count of records affected
     * @throws ActiveException if error
     */
    public int insert(R record) throws ActiveException
    {
        return new Insert<R>(this, record).execute();
    }

    
    /**
     * Inserts collection of records.
     * {@link ActiveRecord#attach(ActiveDatabase)} is invoked on records to
     * attach them to the active database of this table prior to insert.
     * <p>
     * Example:
     * <blockquote><pre>
     * ActiveDatabase activeDatabase = ...
     * ActiveTable&lt;Student&gt; table = new ActiveTable&lt;Student&gt;(activeDatabase, Student.class);
     * ArrayList&lt;Student&gt; list = new ArrayList&lt;Student&gt;();
     * list.add(s1);
     * list.add(s2);
     * list.add(s3);
     * table.insertAll(list);
     * </pre></blockquote>
     * @param records records to insert
     * @return count of records affected
     * @throws ActiveException if error
     */
    public int insertAll(Collection<R> records) throws ActiveException
    {
        return new InsertAll<R>(this, records).execute();
    }
    

    /**
     * Inserts collection of records in batch mode.
     * {@link ActiveRecord#attach(ActiveDatabase)} is invoked on records to
     * attach them to the active database of this table prior to insert.
     * See limitations about batch inserts in {@link ModifyOperation#setBatch(boolean)}.
     * 
     * @param records records to insert
     * @return count of records affected
     * @throws ActiveException if error
     * @since 1.9
     */
    public int insertAllBatch(Collection<R> records) throws ActiveException
    {
        return new InsertAllBatch<R>(this, records).execute();
    }
    
    
    /**
     * Updates one record in table by primary key. Primary key must be defined by one
     * or more {@link Column#primaryKey()} annotations.
     * {@link ActiveRecord#attach(ActiveDatabase)} is invoked on record to
     * attach it to the active database of this table prior to update.
     * Typically {@link ActiveRecord#update()} is used instead of this method.
     * <p>
     * Example:
     * <blockquote><pre>
     * ActiveDatabase activeDatabase = ...
     * ActiveTable&lt;Student&gt; table = new ActiveTable&lt;Student&gt;(activeDatabase, Student.class);
     * Student s = table.select(id);
     * s.setGraduationDate(...);
     * table.update(s);
     * </pre></blockquote>
     * @param record record to update
     * @return count of records affected
     * @throws ActiveException if error
     */
    public int update(R record) throws ActiveException
    {
        return new Update<R>(this, record).execute();
    }

    
    /**
     * Updates collection of records using primary key. Primary key must be defined by one
     * or more {@link Column#primaryKey()} annotations.
     * {@link ActiveRecord#attach(ActiveDatabase)} is invoked on records to
     * attach them to the active database of this table prior to update.
     * <p>
     * Example:
     * <blockquote><pre>
     * ActiveDatabase activeDatabase = ...
     * ActiveTable&lt;Student&gt; table = new ActiveTable&lt;Student&gt;(activeDatabase, Student.class);
     * List&lt;Student&gt; list = table.selectAll();
     * for (Student s: list)
     *     s.setGraduationDate(...);
     *   
     * table.updateAll(list);
     * </pre></blockquote>
     * @param records records to update
     * @return count of records affected
     * @throws ActiveException if error
     */
    public int updateAll(Collection<R> records) throws ActiveException
    {
        return new UpdateAll<R>(this, records).execute();
    }
    
    
    /**
     * Updates collection of records using primary key in batch mode. Primary key must be defined by one
     * or more {@link Column#primaryKey()} annotations.
     * {@link ActiveRecord#attach(ActiveDatabase)} is invoked on records to
     * attach them to the active database of this table prior to update.
     * See limitations about batch updates in {@link ModifyOperation#setBatch(boolean)}.
     * 
     * @param records records to update
     * @return count of records affected
     * @throws ActiveException if error
     * @since 1.9
     */
    public int updateAllBatch(Collection<R> records) throws ActiveException
    {
        return new UpdateAllBatch<R>(this, records).execute();
    }
    
    
    /**
     * Deletes by primary key. Primary key must be defined by one
     * or more {@link Column#primaryKey()} annotations.    
     * {@link ActiveRecord#attach(ActiveDatabase)} is invoked on record to
     * attach it to the active database of this table prior to delete.
     * Typically {@link ActiveRecord#delete()} is used instead of this method.
     * <p>
     * Example:
     * <blockquote><pre>
     * ActiveDatabase activeDatabase = ...
     * ActiveTable&lt;Student&gt; table = new ActiveTable&lt;Student&gt;(activeDatabase, Student.class);
     * // delete student with id of 1234
     * Student s = table.select(1234);
     * table.delete(s);
     * </pre></blockquote> 
     * @param record get primary key values from this record
     * @return count of records affected
     * @throws ActiveException if error
     */
    public int delete(R record) throws ActiveException
    {
        return new Delete<R>(this, record).execute();
    }

    
    /**
     * Deletes all records in table.
     * <p>
     * Example:
     * <blockquote><pre>
     * ActiveDatabase activeDatabase = ...
     * ActiveTable&lt;Student&gt; table = new ActiveTable&lt;Student&gt;(activeDatabase, Student.class);
     * table.deleteAll();
     * </pre></blockquote> 
     * @return count of records affected
     * @throws ActiveException if error
     */
    public int deleteAll() throws ActiveException
    {
        return new DeleteAll<R>(this).execute();
    }

    
    /**
     * Deletes many records by primary key. Primary key must be defined by one
     * or more {@link Column#primaryKey()} annotations.
     * {@link ActiveRecord#attach(ActiveDatabase)} is invoked on records to
     * attach them to the active database of this table prior to delete.
     * <p>
     * Example:
     * <blockquote><pre>
     * ActiveDatabase activeDatabase = ...
     * ActiveTable&lt;Student&gt; table = new ActiveTable&lt;Student&gt;(activeDatabase, Student.class);
     * List&lt;Student&gt; list = getSomeStudents();
     * table.deleteAll(list);
     * </pre></blockquote> 
     * @param records get primary key values from each record in this collection
     * @return count of records affected
     * @throws ActiveException if error
     */
    public int deleteAll(Collection<R> records) throws ActiveException
    {
        // if records == null, don't execute since all records will be deleted
        if (records != null)
        {
            return new DeleteAll<R>(this, records).execute();
        }
        
        return 0;
    }

    
    /**
     * Deletes many records by primary key in batch mode. Primary key must be defined by one
     * or more {@link Column#primaryKey()} annotations.
     * {@link ActiveRecord#attach(ActiveDatabase)} is invoked on records to
     * attach them to the active database of this table prior to delete.
     * 
     * @param records get primary key values from each record in this collection
     * @return count of records affected
     * @throws ActiveException if error
     * @since 1.9
     */
    public int deleteAllBatch(Collection<R> records) throws ActiveException
    {
        // if records == null, don't execute since all records will be deleted
        if (records != null)
        {
            return new DeleteAllBatch<R>(this, records).execute();
        }
        
        return 0;
    }
}
