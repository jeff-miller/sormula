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
package org.sormula.cache.writable;

import org.sormula.Table;
import org.sormula.annotation.cache.Cached;
import org.sormula.cache.CacheException;
import org.sormula.operation.DeleteOperation;
import org.sormula.operation.InsertOperation;
import org.sormula.operation.OperationException;
import org.sormula.operation.SaveOperation;
import org.sormula.operation.SqlOperation;
import org.sormula.operation.UpdateOperation;


/**
 * Operations used to write uncommitted cache rows to database. Used by {@link WritableCache#write()}.
 * The write operations classes are obtained from {@link Cached#insert()}, {@link Cached#update()}, 
 * {@link Cached#delete()}, and {@link Cached#save()}.
 * 
 * @author Jeff Miller
 * @since 3.0
 *
 * @param <R> cached row type
 */
public class WriteOperations<R> implements AutoCloseable
{
    WritableCache<R> writableCache;
    InsertOperation<R> insertOperation; 
    UpdateOperation<R> updateOperation;
    DeleteOperation<R> deleteOperation;
    SaveOperation<R>   saveOperation;

    
    /**
     * Constructs for a cache.
     * 
     * @param writableCache cache that contains uncommitted rows to write
     * @throws CacheException if error
     */
    public WriteOperations(WritableCache<R> writableCache) throws CacheException
    {
        this.writableCache = writableCache;
    }
    
    
    /**
     * Creates new instances of write operations and initializes them to be used by {@link #insert(Object)},
     * {@link #update(Object)}, {@link #delete(Object)}, and {@link #save(Object)}. Caching is turned off
     * for write operations using {@link SqlOperation#setCached(boolean)} to avoid infinite cache recursion. 
     * Cascading is turned off for write operations with {@link SqlOperation#setCascade(boolean)} since 
     * cascades on cached tables are performed by {@link SqlOperation#execute()}.
     * 
     * @throws CacheException if error
     */
    @SuppressWarnings("unchecked")
    public void open() throws CacheException
    {
        Table<R> table = writableCache.getTable();
        
        // operations to use for all rows
        try
        {
            Cached cachedAnnotation = writableCache.getCachedAnnotation();
            insertOperation = (InsertOperation<R>)cachedAnnotation.insert().getConstructor(Table.class).newInstance(table);
            updateOperation = (UpdateOperation<R>)cachedAnnotation.update().getConstructor(Table.class).newInstance(table);
            deleteOperation = (DeleteOperation<R>)cachedAnnotation.delete().getConstructor(Table.class).newInstance(table);
            saveOperation   = (SaveOperation<R>)  cachedAnnotation.save()  .getConstructor(Table.class).newInstance(table);
        }
        catch (NoSuchMethodException e)
        {
            throw new CacheException("write operation class does not have constructor with Table parameter", e);
        }
        catch (Exception e)
        {
            throw new CacheException("error instantiating write operation", e);
        }
        
        // timings for cache
        insertOperation.setTimingId("Cache write " + insertOperation.getClass().getCanonicalName());
        updateOperation.setTimingId("Cache write " + updateOperation.getClass().getCanonicalName());
        deleteOperation.setTimingId("Cache write " + deleteOperation.getClass().getCanonicalName());
        saveOperation  .setTimingId("Cache write " + saveOperation.getClass().getCanonicalName());
        
        // important to turn off caching to avoid infinite cache recursion
        insertOperation.setCached(false);
        updateOperation.setCached(false);
        deleteOperation.setCached(false);
        saveOperation.setCached(false);
        
        // turn off cascades since they occurred when cached
        insertOperation.setCascade(false);
        updateOperation.setCascade(false);
        deleteOperation.setCascade(false);
        saveOperation.setCascade(false);
    }
    
    
    /**
     * Closes write operations.
     * 
     * @throws CacheException if error
     */
    public void close() throws CacheException
    {
        try
        {
            if (insertOperation != null) insertOperation.close();
            if (updateOperation != null) updateOperation.close();
            if (deleteOperation != null) deleteOperation.close();
            if (saveOperation   != null) saveOperation.close();
        }
        catch (OperationException e)
        {
            throw new CacheException("write operations close error", e);
        }
    }
    
    
    /**
     * Inserts row into database with {@link Cached#insert()}.
     * 
     * @param row row to insert
     * @throws CacheException if error
     */
    public void insert(R row) throws CacheException
    {
        try
        {
            insertOperation.setRow(row);
            insertOperation.execute();
        }
        catch (Exception e)
        {
            throw new CacheWriteException(writableCache.getTable().getRowClass(), 
                    writableCache.getPrimaryKeyValues(row), e);
        }
    }
    
    
    /**
     * Updates row in database using primary key(s) with {@link Cached#update()}.
     * 
     * @param row row to update
     * @throws CacheException if error
     */
    public void update(R row) throws CacheException
    {
        try
        {
            updateOperation.setRow(row);
            updateOperation.execute();
        }
        catch (Exception e)
        {
            throw new CacheWriteException(writableCache.getTable().getRowClass(), 
                    writableCache.getPrimaryKeyValues(row), e);
        }
    }
    
    
    /**
     * Deletes row from database using primary key(s) with {@link Cached#delete()}.
     * 
     * @param row row to delete
     * @throws CacheException if error
     */
    public void delete(R row) throws CacheException
    {
        try
        {
            deleteOperation.setRow(row);
            deleteOperation.execute();
        }
        catch (Exception e)
        {
            throw new CacheWriteException(writableCache.getTable().getRowClass(), 
                    writableCache.getPrimaryKeyValues(row), e);
        }
    }
    
    
    /**
     * Saves row into database using primary key(s) with {@link Cached#save()}.
     * 
     * @param row row to save
     * @throws CacheException if error
     */
    public void save(R row) throws CacheException
    {
        try
        {
            saveOperation.setRow(row);
            saveOperation.execute();
        }
        catch (Exception e)
        {
            throw new CacheWriteException(writableCache.getTable().getRowClass(), 
                    writableCache.getPrimaryKeyValues(row), e);
        }
    }
}
