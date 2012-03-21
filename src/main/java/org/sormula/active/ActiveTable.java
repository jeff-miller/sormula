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
import org.sormula.active.operation.Insert;
import org.sormula.active.operation.InsertAll;
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


/**
 * Performs all database operations for {@link ActiveRecord} objects. This class is
 * used by {@link ActiveRecord}. It may also be created explicity to invoke its 
 * public methods.
 * 
 * @author Jeff Miller
 * @since 1.7
 * 
 * @param <R> record type
 */
public class ActiveTable<R extends ActiveRecord>
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
     * record data bases is initialized with {@link ActiveRecord#attach(ActiveDatabase)}.
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
    
    
    // TODO copy javadoc from Table?
    public R select(Object... primaryKeys) throws ActiveException
    {
        return new Select<R>(this, primaryKeys).execute();
    }
    
    
    public R selectWhere(String whereConditionName, Object...parameters) throws ActiveException
    {
        return new SelectWhere<R>(this, whereConditionName, parameters).execute();
    }
    
    
    public R selectCustom(String customSql, Object... parameters) throws ActiveException
    {
        return new SelectCustom<R>(this, customSql, parameters).execute();
    }
    
    
    public List<R> selectAll() throws ActiveException
    {
        return new SelectAll<R>(this).execute();
    }
    
    
    public List<R> selectAllWhere(String whereConditionName, Object... parameters) throws ActiveException
    {
        return new SelectAllWhere<R>(this, whereConditionName, parameters).execute();
    }
    
    
    // whereConditionName empty string selects all rows
    public List<R> selectAllWhereOrdered(String whereConditionName, String orderByName, Object... parameters) throws ActiveException
    {
        return new SelectAllWhereOrdered<R>(this, whereConditionName, orderByName, parameters).execute();
    }
    
    
    public List<R> selectAllCustom(String customSql, Object... parameters) throws ActiveException
    {
        return new SelectAllCustom<R>(this, customSql, parameters).execute();
    }
    
    
    public <T> T selectCount() throws ActiveException
    {
        return new SelectCount<R, T>(this).execute();
    }
    
    
    public <T> T selectCount(String expression) throws ActiveException
    {
        return new SelectCount<R, T>(this, expression).execute();
    }
    
    
    public <T> T selectCount(String expression, String whereConditionName, Object... parameters) throws ActiveException
    {
        return new SelectCount<R, T>(this, expression, whereConditionName, parameters).execute();
    }
    
    
    public <T> T selectMin(String expression) throws ActiveException
    {
        return new SelectMin<R, T>(this, expression).execute();
    }
    
    
    public <T> T selectMin(String expression, String whereConditionName, Object... parameters) throws ActiveException
    {
        return new SelectMin<R, T>(this, expression, whereConditionName, parameters).execute();
    }
    
    
    public <T> T selectMax(String expression) throws ActiveException
    {
        return new SelectMax<R, T>(this, expression).execute();
    }
    
    
    public <T> T selectMax(String expression, String whereConditionName, Object... parameters) throws ActiveException
    {
        return new SelectMax<R, T>(this, expression, whereConditionName, parameters).execute();
    }
    
    
    public <T> T selectSum(String expression) throws ActiveException
    {
        return new SelectSum<R, T>(this, expression).execute();
    }
    
    
    public <T> T selectSum(String expression, String whereConditionName, Object...parameters)
    {
        return new SelectSum<R, T>(this, expression, whereConditionName, parameters).execute();
    }
    
    
    public <T> T selectAvg(String expression) throws ActiveException
    {
        return new SelectAvg<R, T>(this, expression).execute();
    }
    
    
    public <T> T selectAvg(String expression, String whereConditionName, Object...parameters) throws ActiveException
    {
        return new SelectAvg<R, T>(this, expression, whereConditionName, parameters).execute();
    }
    
    
    public int save(R record) throws ActiveException
    {
        return new Save<R>(this, record).execute();
    }

    
    public int saveAll(Collection<R> records) throws ActiveException
    {
        return new SaveAll<R>(this, records).execute();
    }

    
    public int insert(R record) throws ActiveException
    {
        return new Insert<R>(this, record).execute();
    }

    
    public int insertAll(Collection<R> records) throws ActiveException
    {
        return new InsertAll<R>(this, records).execute();
    }
    
    
    public int update(R record) throws ActiveException
    {
        return new Update<R>(this, record).execute();
    }

    
    public int updateAll(Collection<R> records) throws ActiveException
    {
        return new UpdateAll<R>(this, records).execute();
    }
    
    
    public int delete(R record) throws ActiveException
    {
        return new Delete<R>(this, record).execute();
    }

    
    public int deleteAll() throws ActiveException
    {
        return new DeleteAll<R>(this).execute();
    }

    
    public int deleteAll(Collection<R> records) throws ActiveException
    {
        // if records == null, don't execute since all records will be deleted
        if (records != null)
        {
            return new DeleteAll<R>(this, records).execute();
        }
        
        return 0;
    }
}
