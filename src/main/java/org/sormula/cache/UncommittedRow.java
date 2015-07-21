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


/**
 * Abstract base class for all uncommitted rows in an {@link AbstractCache}. Contains a key used to 
 * store uncommitted row in cache and a reference to the corresponding row object for that key.
 *  
 * @author Jeff Miller
 * @since 3.0
 *
 * @param <R> cached row type
 */
public abstract class UncommittedRow<R>
{
    CacheKey cacheKey;
    R row;
    
    
    /**
     * Constructs for a key and correspond row.
     * 
     * @param cacheKey cache key based upon primary keys of row
     * @param row row corresponding to cache key
     */
    public UncommittedRow(CacheKey cacheKey, R row)
    {
        this.cacheKey = cacheKey;
        this.row = row;
    }
    
    
    /**
     * Performs a primary key select on the uncommitted row. Primary key(s) are not needed since
     * uncommitted row knows the primary key(s). 
     * 
     * @return row for primary key(s) or null if uncommitted row represents a deleted row
     * @throws CacheException if error
     */
    public abstract R select() throws CacheException;
    
    
    /**
     * Performs an insert of new row on existing uncommitted row. Typically this will result in a
     * {@link DuplicateCacheException} unless uncommitted row represents a deleted row.
     * 
     * @param row row to insert
     * @return new uncommitted row to replace this or null if no change is required
     * @throws CacheException if error
     */
    public abstract UncommittedRow<R> insert(R row) throws CacheException;

    
    /**
     * Performs an update of new row on existing uncommitted row. 
     * 
     * @param row row to update
     * @return new uncommitted row to replace this or null if no change is required
     * @throws CacheException if error
     */
    public abstract UncommittedRow<R> update(R row) throws CacheException;
    
    
    /**
     * Performs a save of new row on existing uncommitted row. 
     * 
     * @param row row to save
     * @return new uncommitted row to replace this or null if no change is required
     * @throws CacheException if error
     * @since 3.4
     */
    public abstract UncommittedRow<R> save(R row) throws CacheException;

    
    /**
     * Performs a delete of new row on existing uncommitted row. 
     * 
     * @param row row to delete
     * @return new uncommitted row to replace this or null if no change is required
     * @throws CacheException if error
     */
    public abstract UncommittedRow<R> delete(R row) throws CacheException;
    
    
    /**
     * Notifies cache that row has been selected from database.
     * 
     * @param row row that has been selected
     * @return cached row if exists or null if row has been deleted
     * 
     * @throws CacheException if error
     */
    public abstract R selected(R row) throws CacheException; 
    
    
    /**
     * Notifies cache that row has been inserted into database.
     * 
     * @param row row that has been inserted
     * @return new uncommitted row to replace this or null if no change is required
     * @throws CacheException if error
     */
    public abstract UncommittedRow<R> inserted(R row) throws CacheException;
    
    
    /**
     * Notifies cache that row has been updated in database.
     * 
     * @param row row that has been updated
     * @return new uncommitted row to replace this or null if no change is required
     * @throws CacheException if error
     */
    public abstract UncommittedRow<R> updated(R row) throws CacheException;
    
    
    /**
     * Notifies cache that row has been saved in database.
     * 
     * @param row row that has been saved
     * @return new uncommitted row to replace this or null if no change is required
     * @throws CacheException if error
     * @since 3.4
     */
    public abstract UncommittedRow<R> saved(R row) throws CacheException;
    
    
    /**
     * Notifies cache that row has been deleted from database.
     * 
     * @param row row that has been deleted
     * @return new uncommitted row to replace this or null if no change is required
     * @throws CacheException if error
     */
    public abstract UncommittedRow<R> deleted(R row) throws CacheException;

    
    /**
     * Put this uncommitted row in committed map of cache.
     * 
     * @param cache cache to affect
     * @throws CacheException if error
     */
    public abstract void updateCommitted(Cache<R> cache) throws CacheException;
    

    /**
     * Gets the cache key for uncommitted row.
     * 
     * @return key supplied in constructor
     */
    public CacheKey getCacheKey()
    {
        return cacheKey;
    }


    /**
     * Sets a replacement row for the existing row in this uncommitted row. Key must be the same.
     * 
     * @param row new row
     */
    public void setRow(R row)
    {
        this.row = row;
    }


    /**
     * Gets the row associated with this uncommitted row.
     * 
     * @return row that will be written to database 
     */
    public R getRow()
    {
        return row;
    }
}
