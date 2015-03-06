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

import org.sormula.Table;
import org.sormula.Transaction;
import org.sormula.annotation.cache.Cached;
import org.sormula.operation.SqlOperation;


/**
 * Interface for a cache implementation. Cache is for one table. Implementations are specified for
 * a table with {@link Cached#type()}.
 * <p>
 * Implementations have the option to cache all rows for the table or only subset of rows. The row
 * parameter in the interface methods, allow cache implementations to cache a subset based upon
 * a row's values.
 * <p>
 * A design goal was to keep this interface as simple as possible and to allow optimal performance
 * with the sormula api. Therefore, only local transactions are supported and there is no attempt to support 
 * JTA, nor generic caches like JCache.
 * <p>
 * Cache implementations require a two parameter constructor of types {@link Table} and {@link Cached}.
 *  
 * @author Jeff Miller
 * @since 3.0
 * @param <R> class of row to cache
 */
public interface Cache<R>
{
	/**
	 * Notification of database transaction start. Typically invoked by {@link Table#begin(Transaction)}.
	 * 
	 * @param transaction database transaction
	 * @throws CacheException if error
	 */
	public void begin(Transaction transaction) throws CacheException;
	
	
    /**
     * Notification of database transaction normal end. Typically invoked by {@link Table#commit(Transaction)}.
     * Some cache implementations write rows to the database when this method is invoked.
     * 
     * @param transaction database transaction
     * @throws CacheException if error
     */
	public void commit(Transaction transaction) throws CacheException;
	
	
	/**
     * Notification of database transaction abnormal end. Typically invoked by {@link Table#rollback(Transaction)}.
     * Typical cache implementations discard uncommitted rows from cache when this method is invoked.
     * 
     * @param transaction database transaction
     * @throws CacheException if error
     */
	public void rollback(Transaction transaction) throws CacheException;
	
	
	/**
	 * Notification that a {@link SqlOperation} for the cached table is about to execute.
	 *  
	 * @param sqlOperation operation that will use this cache
	 * @throws CacheException if error
	 */
	public void execute(SqlOperation<R> sqlOperation) throws CacheException;
	
	
    /**
     * Notification that a {@link SqlOperation} for the cached table is about to close.
     *  
     * @param sqlOperation operation that will used this cache
     * @throws CacheException if error
     */
	public void close(SqlOperation<R> sqlOperation) throws CacheException;
	
	
	/**
	 * Tests if cache contains a row with the primary key(s). True is returned if cache has 
	 * knowledge of any kind of row including one that has been deleted. For deleted rows, true
	 * is returned meaning that row is in cache but will be deleted when transaction completes.
	 * 
	 * @param primaryKeys primary key(s) for one row 
	 * @return true if cache contains a row for the primary key(s); false if not
	 * @throws CacheException if error
	 */
	public boolean contains(Object[] primaryKeys) throws CacheException;
	
	
	/**
	 * Performs an equivalent to SQL select on cache for a row with primary key(s). If false is
	 * returned, then invoke {@link #selected(Object)} when the row is selected from database. If true
	 * is returned, then {@link #selected(Object)} must not be invoked.
	 * 
	 * @param primaryKeys primary key(s) for one row 
	 * @return row for primary key(s) if row exists in cache; null if row is not in cache or row
	 * will be deleted for primary key(s)
	 * @throws CacheException if error
	 */
	public R select(Object[] primaryKeys) throws CacheException;
	
	
	/**
	 * Performs an equivalent to SQL insert on cache for row.  If false is returned, then 
	 * invoke {@link #inserted(Object)} when the row is inserted into database. If true
     * is returned, then {@link #inserted(Object)} must not be invoked.
	 * 
	 * @param row row to insert
	 * @return true if cache is authority for row (cache will insert row into database);
	 * false if cache does not insert row into database (row should be inserted by the operation that invoked this method)
	 *  
	 * @throws CacheException if error
	 */
	public boolean insert(R row) throws CacheException;
	
    
    /**
     * Performs an equivalent to SQL update on cache for row. If false is returned, then 
     * invoke {@link #updated(Object)} when the row is updated in database. If true
     * is returned, then {@link #updated(Object)} must not be invoked.
     * 
     * @param row row to update
     * @return true if cache is authority for row (cache will update row in database);
     * false if cache does not update row in database (row should be updated by the operation that invoked this method)
     *  
     * @throws CacheException if error
     */
	public boolean update(R row) throws CacheException;
	
    
    /**
     * Performs an equivalent to SQL delete on cache for row. If false is returned, then 
     * invoke {@link #deleted(Object)} when the row is deleted from database. If true
     * is returned, then {@link #deleted(Object)} must not be invoked.
     * 
     * @param row row to delete
     * @return true if cache is authority for row (cache will delete row from database);
     * false if cache does not delete row from database (row should be delete by the operation that invoked this method)
     *  
     * @throws CacheException if error
     */
	public boolean delete(R row) throws CacheException;
	
	
    /**
     * Indicates row was selected from database. Always use returned row since it will be
     * consistent with what is stored in cache. Row is added to cache if appropriate.
     * 
     * @param row that was selected
     * @return cached row if row parameter if row already exists in cache; null if row
     * has been deleted 
     * @throws CacheException if error
     */
	public R selected(R row) throws CacheException;
    
	
	/**
	 * Indicates that a row was inserted into database. Row is added to cache if appropriate.
	 *  
	 * @param row that was inserted
	 * @throws CacheException if error
	 */
	public void inserted(R row) throws CacheException;
    
    
    /**
     * Indicates that a row was updated in database. Row is added to cache if appropriate.
     *  
     * @param row that was updated
     * @throws CacheException if error
     */
    public void updated(R row) throws CacheException;
    
    
    /**
     * Indicates that a row was deleted from database. Row is added to cache if appropriate.
     *  
     * @param row that was deleted
     * @throws CacheException if error
     */
    public void deleted(R row) throws CacheException;

    
    /**
     * Writes uncommitted changes to database.
     * 
     * @throws CacheException if error
     */
    public void write() throws CacheException;
    
    
	/**
	 * Removes row from cache. Does not write it to database.
	 * 
	 * @param row row to remove from cache
	 * @throws CacheException if error
	 */
	public void evict(R row) throws CacheException;
	
	
    /**
     * Removes all rows from cache. Does not write any rows to database.
     * 
     * @throws CacheException if error
     */
	public void evictAll() throws CacheException;
	

	/**
	 * Writes state of cache to log for debugging.
	 */
	public void log();
}
