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
package org.sormula.cache;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.sormula.Table;
import org.sormula.Transaction;
import org.sormula.TransactionListener;
import org.sormula.annotation.cache.Cached;
import org.sormula.log.SormulaLogger;
import org.sormula.log.SormulaLoggerFactory;
import org.sormula.operation.ScalarSelectOperation;
import org.sormula.reflect.FieldExtractor;
import org.sormula.reflect.ReflectException;


/**
 * Abstract base class for implementing caches. The class contains two maps of rows keyed by 
 * primary key(s) of row. One map contains uncommitted rows that are in cache until the database
 * transaction commits. The other map contains rows that have been cached and retained after the
 * database transaction has been committed.
 *  
 * @author Jeff Miller
 * @since 3.0
 *
 * @param <R> cache row type
 */
public abstract class AbstractCache<R> implements Cache<R>
{
    private static final SormulaLogger log = SormulaLoggerFactory.getClassLogger();
    Table<R> table;
    Cached cachedAnnotation;
    FieldExtractor<R> primaryKeyExtractor;
    Map<CacheKey, R> committedCache; 
    Map<CacheKey, UncommittedRow<R>> uncommittedCache;
    int hits;
    int misses;
    
    
    /**
     * Constructs for a table and annotation.
     * 
     * @param table cache rows for operations on this table
     * @param cachedAnnotation annotation that defines cache configuration
     * @throws CacheException if error
     */
    public AbstractCache(Table<R> table, Cached cachedAnnotation) throws CacheException
    {
        this.table = table;
        this.cachedAnnotation = cachedAnnotation;
        
        try
        {
            primaryKeyExtractor = new FieldExtractor<>(table.getRowTranslator().getPrimaryKeyWhereTranslator());
        }
        catch (ReflectException e)
        {
            throw new CacheException("error creating primary key extractor", e);
        }
        
        initCommittedCache();
    }

    
    /**
     * Indicates start of database transaction. Usually invoked by {@link Table#begin(Transaction)} which
     * is a listener method of {@link TransactionListener}. Initializes uncommitted map with {@link #initCommittedCache()}.
     * 
     * @param transaction database transaction
     * throws CacheException if error
     */
    public void begin(Transaction transaction) throws CacheException
    {
        if (log.isDebugEnabled()) log.debug("begin()");
        initUncommittedCache();
    }

    
    /**
     * Indicates database transaction commit. Usually invoked by {@link Table#commit(Transaction)} which
     * is a listener method of {@link TransactionListener}. If {@link Cached#evictOnTransactionEnd()} is 
     * false, then updates committed map with {@link UncommittedRow#updateCommitted(Cache)} for all rows 
     * in uncommitted cache. If {@link Cached#evictOnTransactionEnd()} is true, then removes all
     * rows from cache with {@link #evictAll()}.
     * 
     * @param transaction database transaction
     * throws CacheException if error
     */
    public void commit(Transaction transaction) throws CacheException
    {
        if (log.isDebugEnabled()) log.debug("commit()");
        
        if (getCachedAnnotation().evictOnTransactionEnd())
        {
            evictAll();
        }
        else
        {
            // update committed cache here so rollbacks prior to this spot will keep committed in correct state
            Collection<UncommittedRow<R>> uncommitted = getUncommittedCache().values();
            if (log.isDebugEnabled()) log.debug("update committed count=" + uncommitted.size());
            for (UncommittedRow<R> uncommittedRow : uncommitted)
            {
                uncommittedRow.updateCommitted(this);
            }
        }
        
        // uncommitted is no longer needed since now synchronized with committed
        setUncommittedCache(null);
    }

    
    /**
     * Indicates database transaction rollback. Usually invoked by {@link Table#rollback(Transaction)} which
     * is a listener method of {@link TransactionListener}. Removes all uncommitted rows. If 
     * {@link Cached#evictOnTransactionEnd()} is true, then removes all committed rows from cache 
     * with {@link #evictAll()}.
     * 
     * @param transaction database transaction
     * throws CacheException if error
     */
    public void rollback(Transaction transaction) throws CacheException
    {
        if (log.isDebugEnabled()) log.debug("rollback()");

        if (getCachedAnnotation().evictOnTransactionEnd())
        {
            evictAll();
        }
        
        // forget uncommitted 
        setUncommittedCache(null);
    }
    
    
    /**
     * Tests if row is in cache. Returns true if row exists as committed or uncommitted. Note that
     * {@link #select(Object[])} will return null but {@link #contains(Object[])} will true for the
     * same primary key(s) if row is an uncommitted delete. {@link #select(Object[])} indicates 
     * committed visibility while {@link #contains(Object[])} simply indicates if row is known to
     * the cache. This method is used by {@link ScalarSelectOperation#execute()} to know if
     * cache is authority for row or if database must be queried to get row.
     * 
     * @param primaryKeys primary key(s) for row
     * @return true if row for primary key(s) exists in cache; false if primary key(s) do not
     * exist in committed or uncommitted map
     * @throws CacheException if error
     */
    public boolean contains(Object[] primaryKeys) throws CacheException
    {
        CacheKey cacheKey = new CacheKey(primaryKeys);
        return getUncommitted(cacheKey) != null || getCommitted(cacheKey) != null;
    }


    /**
     * Removes row from the cache. Does nothing if cache is not initialized because no
     * transaction is active.
     * 
     * @param row row to remove
     * @throws CacheException never
     */
    public void evict(R row) throws CacheException
    {
        CacheKey cacheKey = new CacheKey(getPrimaryKeyValues(row));
        if (log.isDebugEnabled()) log.debug("evict() " + cacheKey + " for table " + table.getRowClass());
        if (uncommittedCache != null) uncommittedCache.remove(cacheKey);
        if (committedCache != null)   committedCache.remove(cacheKey);
    }


    /**
     * Removes all rows from the cache. Does nothing if cache is not initialized because no
     * transaction is active.
     * 
     * @throws CacheException never 
     */
    public void evictAll() throws CacheException
    {
        if (log.isDebugEnabled()) log.debug("evictAll() for table " + table.getRowClass());
        if (uncommittedCache != null) uncommittedCache.clear();
        if (committedCache != null)   committedCache.clear(); 
    }


    /**
     * Initializes committed map with initial capacity of {@link Cached#size()}. Subclasses 
     * may override to initialize a custom {@link Map}.
     * 
     * @throws CacheException if error
     */
    protected void initCommittedCache() throws CacheException
    {
        committedCache = new HashMap<>(cachedAnnotation.size());
    }
    
    
    /**
     * Initializes uncommitted map with initial capacity of 1/2 of {@link Cached#size()}. Subclasses 
     * may override to initialize a custom {@link Map}.
     * 
     * @throws CacheException if error
     */
    protected void initUncommittedCache() throws CacheException
    {
        if (uncommittedCache == null)
        {
            uncommittedCache = new HashMap<>(cachedAnnotation.size() / 2);
        }
        else
        {
            throw new IllegalCacheStateException("begin cache transaction without committing old transaction");
        }
    }


    /**
     * Gets table that is cached.
     * 
     * @return table supplied in constructor
     */
    public Table<R> getTable()
    {
        return table;
    }


    /**
     * Gets the annotations used to configure cache.
     * 
     * @return annotation supplied in constructor
     */
    public Cached getCachedAnnotation()
    {
        return cachedAnnotation;
    }


    /**
     * Gets the {@link FieldExtractor} used to get all primary key values from a row.
     * 
     * @return primary key extractor used by {@link #getPrimaryKeyValues(Object)}
     */
    public FieldExtractor<R> getPrimaryKeyExtractor()
    {
        return primaryKeyExtractor;
    }
    
    
    /**
     * Writes information to the log about the cache.
     */
    public void log()
    {
        log.info(getTable().getRowClass().getCanonicalName() + " cache hits=" + hits + 
                " (" + getPercentHits() + "%)" + " misses=" + misses + " size=" + committedCache.size());
        
        if (log.isDebugEnabled())
        {
            log.debug("committed keys   = " + committedCache.keySet());
            log.debug("uncommitted keys = " + (uncommittedCache != null ? uncommittedCache.keySet() : "none"));
        }
    }
    
    
    /** 
     * Gets the map that stores committed rows. Map is not a copy so modifying map will
     * affect cache.
     * 
     * @return the map of committed rows
     */
    protected Map<CacheKey, R> getCommittedCache()
    {
        return committedCache;
    }
    
    
    /**
     * Sets new committed map. Available for subclasses to change the map implementation.
     * 
     * @param committedCache map for storing committed rows
     */
    protected void setCommittedCache(Map<CacheKey, R> committedCache)
    {
        this.committedCache = committedCache;
    }

    
    /** 
     * Gets the map that stores uncommitted rows. Map is not a copy so modifying map will
     * affect cache.
     * 
     * @return the map of uncommitted rows
     */
    protected Map<CacheKey, UncommittedRow<R>> getUncommittedCache()
    {
        return uncommittedCache;
    }

    
    /**
     * Sets new uncommitted map. Available for subclasses to change the map implementation.
     * 
     * @param uncommittedCache map for storing uncommitted rows
     */
    protected void setUncommittedCache(Map<CacheKey, UncommittedRow<R>> uncommittedCache)
    {
        this.uncommittedCache = uncommittedCache;
    }


    /**
     * Gets a row from committed cache.
     * 
     * @param primaryKeys primary key(s) of a row
     * @return row or null if no row exists in committed cache for key
     */
    public R getCommitted(CacheKey primaryKeys)
    {
        return committedCache.get(primaryKeys);
    }


    /**
     * Puts a row into committed cache.
     * 
     * @param primaryKeys primary key(s) of a row
     * @param row row to put in committed cache
     * @return previous row if one existed for the key or null if no previous row existed in map
     */
    public R putCommitted(CacheKey primaryKeys, R row)
    {
        return committedCache.put(primaryKeys, row);
    }


    /**
     * Removes a row from committed cache.
     * 
     * @param primaryKeys primary key(s) of a row
     * @return row if one existed for the key or null if no row existed in map
     */
    public R removeCommited(CacheKey primaryKeys)
    {
        return committedCache.remove(primaryKeys);
    }

    
    /**
     * Verifies that cache is ready.
     * 
     * @throws CacheException if cache is not ready for use
     */
    protected void check() throws CacheException
    {
        if (uncommittedCache == null) throw new IllegalCacheStateException("cache activity with no transaction");
    }

    
    /**
     * Gets a row from uncommitted cache.
     * 
     * @param primaryKeys primary key(s) of a row
     * @return row or null if no row exists in uncommitted cache for key
     */
    public UncommittedRow<R> getUncommitted(CacheKey primaryKeys)
    {
        return uncommittedCache.get(primaryKeys);
    }
    
    
    /**
     * Puts a row into uncommitted cache.
     * 
     * @param uncommittedRow row to put in uncommitted cache
     * @return previous row if one existed for the key or null if no previous row existed in map
     */
    public UncommittedRow<R> putUncommitted(UncommittedRow<R> uncommittedRow)
    {
        return uncommittedCache.put(uncommittedRow.getCacheKey(), uncommittedRow);
    }
    
    
    /**
     * Transition uncommitted cache state.
     * 
     * @param currentUncommittedRow current uncommitted row in uncommitted cache
     * @param newUncommittedRow null to remove current uncommitted row; otherwise replace current
     * uncommitted row with new uncommitted row (if they are not the same object)
     * @return uncommitted row that was replaced or removed; null if uncommitted cache was not changed
     */
    public UncommittedRow<R> transition(UncommittedRow<R> currentUncommittedRow, UncommittedRow<R> newUncommittedRow)
    {
        if (newUncommittedRow == null)
        {
            // null means to remove current
            return uncommittedCache.remove(currentUncommittedRow.getCacheKey());
        }
        else if (currentUncommittedRow != newUncommittedRow)
        {
            // different uncommitted means to replace current with new
            return uncommittedCache.put(newUncommittedRow.getCacheKey(), newUncommittedRow);
        }
        else
        {
            // current and new are the same object, no change needed
            return null;
        }
    }
    
    
    /**
     * Gets the primary key values from a row as Object array.
     * 
     * @param row get value from this row
     * @return array of primary key values
     * @throws CacheException if error extracting values
     */
    public Object[] getPrimaryKeyValues(R row) throws CacheException
    {
        try
        {
            return primaryKeyExtractor.getFieldValues(row);
        }
        catch (ReflectException e)
        {
            throw new CacheException("can't get primary key(s)", e);
        }
    }


    /**
     * Gets the number of times that requested row was in cache.
     * 
     * @return number of cache hits
     */
    public int getHits()
    {
        return hits;
    }


    /**
     * Increments hit count.
     */
    public void hit()
    {
        ++hits;
    }


    /**
     * Gets the number of times that requested row was not in cache.
     * 
     * @return number of cache misses
     */
    public int getMisses()
    {
        return misses;
    }


    /**
     * Increments miss count.
     */
    public void miss()
    {
        ++misses;
    }
    
    
    /**
     * Gets hits as a percentage of total requests.
     * 
     * @return 100 * hits / (hits + misses) rounded to whole percent
     */
    public int getPercentHits()
    {
        return Math.round(100 * (float)hits / (float)(hits + misses));
    }
}
