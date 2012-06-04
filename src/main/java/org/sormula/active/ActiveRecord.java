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

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import org.sormula.Database;
import org.sormula.active.operation.ActiveLazySelector;
import org.sormula.annotation.cascade.SelectCascade;
import org.sormula.annotation.cascade.SelectCascadeAnnotationReader;
import org.sormula.log.ClassLogger;
import org.sormula.operation.cascade.lazy.LazySelectable;



/**
 * Base class for active records. See {@link #table(Class)} for an example how to add
 * a static member named table to the sublcass that extends ActiveRecord.  
 * 
 * @author Jeff Miller
 * @since 1.7
 * 
 * @param <R> record type
 */
public abstract class ActiveRecord<R extends ActiveRecord<R>> implements LazySelectable, Serializable
{
    private static final ClassLogger log = new ClassLogger();
    private static final long serialVersionUID = 1L;
    ActiveDatabase activeDatabase;
    Class<R> recordClass;
    boolean pendingLazySelects;  
    Set<String> pendingLazySelectFieldNames;
    
    
    /**
     * Constructs.
     */
    public ActiveRecord()
    {
        recordClass = getRecordClass();
    }
    
    
    /**
     * Gets database set by {@link #attach(ActiveDatabase)}.
     * 
     * @return active database for this active record
     */
    public ActiveDatabase getActiveDatabase()
    {
        return activeDatabase;
    }


    /**
     * Associates an active database with this active record.  {@link #insert()}, {@link #save()},
     * {@link #update()}, and {@link #delete()} methods use active database to know what
     * database to act upon. The method is invoked by {@link ActiveTable} methods insert or modify
     * table or when an active record is selected as result of a cascade. This method must be
     * invoked on an active record that was created with new operator prior to using {@link #insert()}, 
     * {@link #save()}, {@link #update()}, and {@link #delete()}. 
     *  
     * @param activeDatabase the database for this record
     */
    public void attach(ActiveDatabase activeDatabase)
    {
        this.activeDatabase = activeDatabase;
    }
    
    
    /**
     * Sets active database to null.
     */
    public void detach()
    {
        this.activeDatabase = null;
    }
    

    /**
     * Creates a table that can be used to for records of type recordClass for the default
     * active database. Use to initialize active record static table member like the following:
     * <blockquote><pre>
     * public class SomeRecord extends ActiveRecord&lg;SomeRecord&gt;
     * {
     *     private static final long serialVersionUID = 1L;
     *     public static final ActiveTable&lt;SomeRecord&gt; table = table(SomeRecord.class);
     *     ...
     * }
     * </pre></blockquote>
     * Use like:
     * <blockquote><pre>
     * List&lt;SomeRecord&gt; records = SomeRecord.table.selectAll();
     * </pre></blockquote>
     * 
     * @param recordClass type of records of table
     * @return ActiveTable instance for records of type R
     * @throws ActiveException if error
     */
    public static <R extends ActiveRecord<R>> ActiveTable<R> table(Class<R> recordClass) throws ActiveException
    {
        return new ActiveTable<>(recordClass);
    }
    
    
    /**
     * Saves record into database. Delegates to {@link ActiveTable#save(ActiveRecord)}.
     * The database used is {@link #getActiveDatabase()}. If no active database is set for
     * this record, then the default active database is used, {@link ActiveDatabase#getDefault()}.
     * 
     * @return number of records affected; typically 1 if record was saved or 0 if not saved
     * @throws ActiveException if error
     */
    public int save() throws ActiveException
    {
        return createTable().save(recordClass.cast(this));
    }


    /**
     * Inserts record into database. Delegates to {@link ActiveTable#insert(ActiveRecord)}.
     * The database used is {@link #getActiveDatabase()}. If no active database is set for
     * this record, then the default active database is used, {@link ActiveDatabase#getDefault()}.
     * 
     * @return number of records affected; typically 1 if record was inserted or 0 if not inserted
     * @throws ActiveException if error
     */
    public int insert() throws ActiveException
    {
        return createTable().insert(recordClass.cast(this));
    }

    
    /**
     * Updates record in database. Delegates to {@link ActiveTable#update(ActiveRecord)}.
     * The database used is {@link #getActiveDatabase()}. If no active database is set for
     * this record, then the default active database is used, {@link ActiveDatabase#getDefault()}.
     * 
     * @return number of records affected; typically 1 if record was updated or 0 if not updated
     * @throws ActiveException if error
     */
    public int update() throws ActiveException
    {
        return createTable().update(recordClass.cast(this));
    }
    
    
    /**
     * Deletes record from database. Delegates to {@link ActiveTable#delete(ActiveRecord)}.
     * The database used is {@link #getActiveDatabase()}. If no active database is set for
     * this record, then the default active database is used, {@link ActiveDatabase#getDefault()}.
     * 
     * @return number of records affected; typically 1 if record was deleted or 0 if not deleted
     * @throws ActiveException if error
     */
    public int delete() throws ActiveException
    {
        return createTable().delete(recordClass.cast(this));
    }
    
    
    /**
     * Class definition of this record.
     * @return {@link Class#getClass()}
     */
    @SuppressWarnings("unchecked") // class is type R since R extends ActiveRecord
    public Class<R> getRecordClass()
    {
        return (Class<R>)getClass();
    }


    /**
     * Selects record(s) from database for field based upon definitions in select annotations of field
     * where {@link SelectCascade#lazy()} is true. Typically this method is invoked by the "get" method
     * associated with field. For example:
     * <blockquote><pre>
    public List&lt;SomeChild&gt; getChildList()
    {
        lazySelectCascade("childList");
        return childList;
    }
    </pre></blockquote>
     * <p>
     * This method may be invoked more than once per field but the field will only be selected upon 
     * the first invocation. Each time that an active record is selected, all lazy select cascaded
     * fields are initialized by {@link #pendingLazySelects(Database)}. Invoking this method on an
     * active record that has not been selected (created with new operator) has no affect.
     * 
     * @param fieldName field to select
     * @throws ActiveException if error
     * @since 1.8
     */
    public void checkLazySelects(String fieldName) throws ActiveException
    {
        if (log.isDebugEnabled()) log.debug("lazySelectCascade check " + fieldName);
        
        if (pendingLazySelects)
        {
            // perform the following test only if record was selected (not created some other way)
            // select cascades are only performed on records that have been selected
            if (pendingLazySelectFieldNames == null)
            {
                // initialize upon first request
                if (log.isDebugEnabled()) log.debug("lazySelectCascade determine lazy fields");
                pendingLazySelectFieldNames = new HashSet<String>();
                
                // for all fields
                for (Field field: getClass().getDeclaredFields())
                {
                    SelectCascadeAnnotationReader scar = new SelectCascadeAnnotationReader(field);
                    SelectCascade[] selectCascades = scar.getSelectCascades();
                    
                    for (SelectCascade c: selectCascades)
                    {
                        if (c.lazy()) pendingLazySelectFieldNames.add(field.getName());
                    }
                }
            }
            
            if (pendingLazySelectFieldNames.contains(fieldName))
            {
                // perform only if pending and fieldName is in pending set
                try
                {
                    // cascade
                    if (log.isDebugEnabled()) log.debug("lazy select " + fieldName);
                    Field field = getClass().getDeclaredField(fieldName);
                    SelectCascadeAnnotationReader scar = new SelectCascadeAnnotationReader(field);
                    new ActiveLazySelector<R>(createTable(), recordClass.cast(this), scar).execute();
                    
                    // don't do field again
                    pendingLazySelectFieldNames.remove(fieldName);
                    
                    if (pendingLazySelectFieldNames.size() == 0)
                    {
                        // don't check this record again
                        pendingLazySelects = false;
                    }
                }
                catch (NoSuchFieldException e)
                {
                    // not likely since pendingLazySelectCascadeFieldNames contains only valid names
                    throw new ActiveException("can't get field name " + fieldName + " in class " + getClass(), e);
                }
            }
        }
    }

    
    /**
     * {@inheritDoc}
     */
    public void pendingLazySelects(Database database) 
    {
        pendingLazySelects = true;
    }


    /**
     * Creates an {@link ActiveTable} for record class returned by {@link #getRecordClass()}.
     * Default active database is used if none is available for this record.
     * 
     * @return active table for performing database operations on this record
     * @throws ActiveException if error
     */
    protected ActiveTable<R> createTable() throws ActiveException
    {
        if (activeDatabase == null) return new ActiveTable<>(recordClass);
        else return new ActiveTable<>(activeDatabase, recordClass);
    }
}
