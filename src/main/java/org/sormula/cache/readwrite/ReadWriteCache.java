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
package org.sormula.cache.readwrite;

import java.util.Arrays;

import org.sormula.Database;
import org.sormula.Table;
import org.sormula.Transaction;
import org.sormula.annotation.Column;
import org.sormula.annotation.Row;
import org.sormula.annotation.cache.Cached;
import org.sormula.cache.Cache;
import org.sormula.cache.CacheException;
import org.sormula.cache.CacheKey;
import org.sormula.cache.DuplicateCacheException;
import org.sormula.cache.IllegalCacheOperationException;
import org.sormula.cache.UncommittedRow;
import org.sormula.cache.writable.WritableCache;
import org.sormula.log.ClassLogger;
import org.sormula.operation.InsertOperation;
import org.sormula.operation.ScalarSelectOperation;
import org.sormula.operation.SqlOperation;


/**
 * A cache that retains rows that selected, inserted, updated, and deleted. Rows modified with
 * insert, update, and delete are written to the database when transaction is committed or when
 * {@link ReadWriteCache#write()}, {@link Table#flush()}, or {@link Database#flush()} is invoked. 
 * The selected and modified rows are retained in cache for life of {@link Table} object or until 
 * explicitly flushed with {@link Table#flush()}, or {@link Database#flush()} or evicted with
 * {@link Cache#evict(Object)} or {@link Cache#evictAll()}.
 * <p>
 * This is a transaction based cache which means that caching is performed relative to database transaction
 * boundaries of begin, commit, and rollback. Tables that are cached may not read/write unless a transaction 
 * is active. Tables that are cached must use {@link Transaction} obtained from {@link Database#getTransaction()}
 * or must use a subclass of {@link Transaction} that is set with {@link Database#setTransaction(Transaction)}.
 * <p>
 * Cached rows are stored in maps with primary keys as the map key. Primary keys are defined by
 * {@link Column#primaryKey()}, {@link Column#identity()}, or {@link Row#primaryKeyFields()}. 
 * Cache will be searched when selecting by primary key. Non primary key 
 * selects will cause cache to write uncommitted rows to database prior to executing query.
 * 
 * @author Jeff Miller
 * @since 3.0
 *
 * @param <R> row type that is cached
 */
public class ReadWriteCache<R> extends WritableCache<R>
{
    private static final ClassLogger log = new ClassLogger();
    boolean autoGeneratedKeys;
    
    
    /**
     * Constructs for a table and cache annotation.
     * 
     * @param table cache rows for this table
     * @param cachedAnnotation cache configuration
     * @throws CacheException if error
     */
    public ReadWriteCache(Table<R> table, Cached cachedAnnotation) throws CacheException
    {
        super(table, cachedAnnotation);
    }

    
    /**
     * Prepares cache for use.
     * 
     * @param sqlOperation operation that will use this cache
     * @throws CacheException if cache has not been initialized (no transaction is active)
     */
    public void execute(SqlOperation<R> sqlOperation) throws CacheException
    {
    	check();
        if (sqlOperation instanceof ScalarSelectOperation)
        {
            // select
            if (!sqlOperation.isPrimaryKey())
            {
                // non primary key select
                // must write cache to database prior to select to allow database to include cached rows in select results
                write();
            }
        }
        else if (sqlOperation instanceof InsertOperation)
        {
            // insert
            autoGeneratedKeys = ((InsertOperation)sqlOperation).isAutoGeneratedKeys(); // rows with identity are cached differently
        }
    }


    /**
     * {@inheritDoc}
     */
    public void close(SqlOperation<R> sqlOperation) throws CacheException
    {
        // no-op
    }


    /**
     * {@inheritDoc}
     */
    public R select(Object[] primaryKeys) throws CacheException
    {
        if (log.isDebugEnabled()) log.debug("select() keys=" + Arrays.asList(primaryKeys));
        check();
        R row;
        CacheKey cacheKey = new CacheKey(primaryKeys);
        UncommittedRow<R> uncommittedRow = getUncommitted(cacheKey);
        
        if (uncommittedRow != null)
        {
            // key exists in uncommitted
            if (log.isDebugEnabled()) log.debug("select() from uncommitted cache");
            row = uncommittedRow.select();
        }
        else
        {
            // check committed
            if (log.isDebugEnabled()) log.debug("select() from committed cache");
            row = getCommitted(cacheKey);
        }
        
        if (row != null) hit(); else miss();
        if (log.isDebugEnabled()) log.debug("select() row found = " + (row != null));
        return row;
    }

    
    /**
     * {@inheritDoc}
     */
    public boolean insert(R row) throws CacheException
    {
        if (autoGeneratedKeys)
        {
            // always insert rows with auto generated keys so key values are known 
            // immediately instead of when cache writes to database
            return false;
        }
        else
        {
            check();
            CacheKey cacheKey = new CacheKey(getPrimaryKeyValues(row));
            UncommittedRow<R> uncommittedRow = getUncommitted(cacheKey);
            
            if (uncommittedRow != null)
            {
                // key exists in uncommitted
                hit();
                transition(uncommittedRow, uncommittedRow.insert(row));
            }
            else
            {
                // check committed
                if (getCommitted(cacheKey) != null)
                {
                    // row is already in cache, insert will be error
                    throw new DuplicateCacheException(cacheKey.getPrimaryKeys());
                }
                else
                {
                    // row is not in cache, ok to insert
                    miss();
                    putUncommitted(new UncommittedInsert<>(cacheKey, row));
                }
            }
    
            return true;
        }
    }

    
    /**
     * {@inheritDoc}
     */
    public boolean update(R row) throws CacheException
    {
        check();
        CacheKey cacheKey = new CacheKey(getPrimaryKeyValues(row));
        UncommittedRow<R> uncommittedRow = getUncommitted(cacheKey);
        
        if (uncommittedRow != null)
        {
            // key exists in uncommitted
            hit();
            transition(uncommittedRow, uncommittedRow.update(row));
        }
        else
        {
            // row is not in cache, remember update
            miss();
            putUncommitted(new UncommittedUpdate<>(cacheKey, row));
        }

        return true;
    }

    
    /**
     * {@inheritDoc}
     */
    public boolean save(R row) throws CacheException
    {
        check();
        CacheKey cacheKey = new CacheKey(getPrimaryKeyValues(row));
        UncommittedRow<R> uncommittedRow = getUncommitted(cacheKey);
        
        if (uncommittedRow != null)
        {
            // key exists in uncommitted
            hit();
            transition(uncommittedRow, uncommittedRow.save(row));
        }
        else
        {
            // row is not in cache, remember save
            miss();
            putUncommitted(new UncommittedSave<>(cacheKey, row));
        }

        return true;
    }

    
    /**
     * {@inheritDoc}
     */
    public boolean delete(R row) throws CacheException
    {
        check();
        CacheKey cacheKey = new CacheKey(getPrimaryKeyValues(row));
        UncommittedRow<R> uncommittedRow = getUncommitted(cacheKey);
        
        if (uncommittedRow != null)
        {
            // key exists in uncommitted
            hit();
            transition(uncommittedRow, uncommittedRow.delete(row));
        }
        else
        {
            // row is not in cache, remember delete
            miss();
            putUncommitted(new UncommittedDelete<>(cacheKey, row));
        }

        return true;
    }

    
    /**
     * {@inheritDoc}
     */
    // note if return is null then row is deleted so don't use
    public R selected(R row) throws CacheException
    {
        check();
        R cachedRow;
        CacheKey cacheKey = new CacheKey(getPrimaryKeyValues(row));
        if (log.isDebugEnabled()) log.debug("selected() keys=" + Arrays.asList(cacheKey.getPrimaryKeys()));
        UncommittedRow<R> uncommittedRow = getUncommitted(cacheKey);
        
        if (uncommittedRow != null)
        {
            // key exists in uncommitted
            if (log.isDebugEnabled()) log.debug("selected() from uncommitted cache");
            cachedRow = uncommittedRow.selected(row);
        }
        else
        {
            // check committed
            if (log.isDebugEnabled()) log.debug("selected() from committed cache");
            cachedRow = getCommitted(cacheKey);

            if (cachedRow == null)
            {
                // not in cache yet, new cache entry
                if (log.isDebugEnabled()) log.debug("selected() new uncommitted select");
                putUncommitted(new UncommittedSelect<>(cacheKey, row));
                cachedRow = row;
            }
        }
        
        return cachedRow;
    }


    /**
     * If table does not have an identity column, then throws IllegalCacheOperationException 
     * since writable {@link ReadWriteCache} should never be notified of an insert of non identity
     * row since {@link ReadWriteCache} will be inserting the database. 
     * <p>
     * If table has an identity column ({@link Column#identity()} was set true on a field),
     * then row is added to cache. Row is not written to database since this method
     * indicates that it has already been written. 
     * 
     * @param row row that has been inserted
     * @throws IllegalCacheOperationException if table does not have an identity column
     */
    public void inserted(R row) throws CacheException
    {
        if (autoGeneratedKeys)
        {
            // assume keys are not in cache since they are generated uniquely upon insert
            UncommittedInsert<R> uncommittedInsert = new UncommittedInsert<>(new CacheKey(getPrimaryKeyValues(row)), row);
            uncommittedInsert.setWritten(true); // already inserted, don't insert again
            putUncommitted(uncommittedInsert);
        }
        else
        {
            // cache inserts all non identity rows 
            // this method should not be invoked for row without identity column
            throw new IllegalCacheOperationException();
        }
    }


    /**
     * Throws IllegalCacheOperationException since writable {@link ReadWriteCache} should never be 
     * notified of an update since {@link ReadWriteCache} will be updating the database. 
     * 
     * @param row ignored
     * @throws IllegalCacheOperationException if error
     */
    public void updated(R row) throws CacheException
    {
        throw new IllegalCacheOperationException();
    }


    /**
     * Throws IllegalCacheOperationException since writable {@link ReadWriteCache} should never be 
     * notified of an save since {@link ReadWriteCache} will be saving to the database. 
     * 
     * @param row ignored
     * @throws IllegalCacheOperationException if error
     * @since 3.4
     */
    public void saved(R row) throws CacheException
    {
        throw new IllegalCacheOperationException();
    }


    /**
     * Throws IllegalCacheOperationException since writable {@link ReadWriteCache} should never be
     * notified of a delete since {@link ReadWriteCache} will be deleting from the database. 
     * 
     * @param row ignored
     * @throws IllegalCacheOperationException if error
     */
    public void deleted(R row) throws CacheException
    {
        throw new IllegalCacheOperationException();
    }
}
