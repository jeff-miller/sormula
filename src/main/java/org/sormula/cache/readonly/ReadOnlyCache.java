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
package org.sormula.cache.readonly;

import java.util.Arrays;

import org.sormula.Database;
import org.sormula.Table;
import org.sormula.Transaction;
import org.sormula.annotation.Column;
import org.sormula.annotation.Row;
import org.sormula.annotation.cache.Cached;
import org.sormula.cache.AbstractCache;
import org.sormula.cache.Cache;
import org.sormula.cache.CacheException;
import org.sormula.cache.CacheKey;
import org.sormula.cache.DuplicateCacheException;
import org.sormula.cache.UncommittedRow;
import org.sormula.log.ClassLogger;
import org.sormula.operation.SqlOperation;


/**
 * A cache that retains rows that selected, inserted, updated, and deleted. Rows are never written to the 
 * database. The selected and modified rows are retained in cache for life of {@link Table} object or until 
 * explicitly flushed with {@link Table#flush()}, or {@link Database#flush()} or evicted with
 * {@link Cache#evict(Object)} or {@link Cache#evictAll()}.
 * <p>
 * TODO can ReadOnlyCache be modified so that transaction is optional?
 * This is a transactional cache which means that caching is performed relative to database transactional
 * boundaries of begin, commit, and rollback. Tables that are cached may not read/write unless a transaction 
 * is active. Tables that are cached must use {@link Transaction} obtained from {@link Database#getTransaction()}
 * or must use a subclass of {@link Transaction} that is set with {@link Database#setTransaction(Transaction)}.
 * <p>
 * Cached rows are stored in maps with primary keys as the map key. The primary key is defined by 
 * {@link Column#primaryKey()}, {@link Column#identity()}, or {@link Row#primaryKeyFields()}.
 * Cache will be searched when selecting by primary key. Non primary key selects will not use cache.
 * 
 * @author Jeff Miller
 * @since 3.0
 *
 * @param <R> cached row type
 */
public class ReadOnlyCache<R> extends AbstractCache<R>
{
    private static final ClassLogger log = new ClassLogger();
    
    
    /**
     * Constructs for a table and cache annotation.
     * 
     * @param table cache rows for this table
     * @param cachedAnnotation cache configuration
     * @throws CacheException if error
     */
    public ReadOnlyCache(Table<R> table, Cached cachedAnnotation) throws CacheException
    {
        super(table, cachedAnnotation);
    }
    
    
    /**
     * Prepares cache for use.
     * 
     * @param sqlOperation ignored
     * @throws CacheException if cache has not been initialized (no transaction is active)
     */
    public void execute(SqlOperation<R> sqlOperation) throws CacheException
    {
        // if no transaction then fail now rather than later 
    	check();
    }


    /**
     * Does nothing.
     * 
     * @param sqlOperation ignored
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
        
        if (row != null) hit(); else miss(); // hit/miss count only have meaning for selects with readonly cache
        if (log.isDebugEnabled()) log.debug("select() row found = " + (row != null));
        return row;
    }

    
    /**
     * Returns false since read only cache never writes to database and is never authority
     * for row.
     * 
     * @param row ignored
     * @return false
     */
    public boolean insert(R row) throws CacheException
    {
        return false;
    }

    
    /**
     * Returns false since read only cache never writes to database and is never authority
     * for row.
     * 
     * @param row ignored
     * @return false
     */
    public boolean update(R row) throws CacheException
    {
        return false;
    }

    
    /**
     * Returns false since read only cache never writes to database and is never authority
     * for row.
     * 
     * @param row ignored
     * @return false
     */
    public boolean delete(R row) throws CacheException
    {
        return false;
    }

    
    /**
     * {@inheritDoc}
     */
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
                putUncommitted(new UncommittedSelect<R>(cacheKey, row));
                cachedRow = row;
            }
        }
        
        return cachedRow;
    }
    

    /**
     * {@inheritDoc}
     */
    public void inserted(R row) throws CacheException
    {
        check();
        CacheKey cacheKey = new CacheKey(getPrimaryKeyValues(row));
        UncommittedRow<R> uncommittedRow = getUncommitted(cacheKey);
        
        if (uncommittedRow != null)
        {
            // key exists in uncommitted
            transition(uncommittedRow, uncommittedRow.inserted(row));
        }
        else
        {
            // check committed
            if (getCommitted(cacheKey) != null)
            {
                // row is already in cache, insert will be error
                // this is not likely since duplicate insert should have already been reported by database
                throw new DuplicateCacheException(cacheKey.getPrimaryKeys());
            }
            else
            {
                // row is not in cache, ok to insert
                putUncommitted(new UncommittedInsert<R>(cacheKey, row));
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    public void updated(R row) throws CacheException
    {
        check();
        CacheKey cacheKey = new CacheKey(getPrimaryKeyValues(row));
        UncommittedRow<R> uncommittedRow = getUncommitted(cacheKey);
        
        if (uncommittedRow != null)
        {
            // key exists in uncommitted
            transition(uncommittedRow, uncommittedRow.updated(row));
        }
        else
        {
            // row is not in cache, remember update
            putUncommitted(new UncommittedUpdate<R>(cacheKey, row));
        }
    }


    /**
     * {@inheritDoc}
     */
    public void deleted(R row) throws CacheException
    {
        check();
        CacheKey cacheKey = new CacheKey(getPrimaryKeyValues(row));
        UncommittedRow<R> uncommittedRow = getUncommitted(cacheKey);
        
        if (uncommittedRow != null)
        {
            // key exists in uncommitted
            transition(uncommittedRow, uncommittedRow.deleted(row));
        }
        else
        {
            // row is not in cache, remember delete
            putUncommitted(new UncommittedDelete<R>(cacheKey, row));
        }
    }
    
    
    /**
     * Does nothing.
     */
    public void write() throws CacheException
    {
    }
}
