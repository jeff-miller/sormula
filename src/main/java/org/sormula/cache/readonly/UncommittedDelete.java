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

import org.sormula.cache.Cache;
import org.sormula.cache.CacheException;
import org.sormula.cache.CacheKey;
import org.sormula.cache.UncommittedRow;
import org.sormula.cache.readwrite.UncommittedSave;


/**
 * Row in {@link ReadOnlyCache} that has been deleted.
 * 
 * @author Jeff Miller
 * @since 3.0
 *
 * @param <R> type of cached row
 */
public class UncommittedDelete<R> extends UncommittedReadOnlyRow<R>
{
    /**
     * Constructs for a key and corresponding row.
     * 
     * @param cacheKey cache key based upon primary keys of row
     * @param row row corresponding to cache key
     */
    public UncommittedDelete(CacheKey cacheKey, R row)
    {
        super(cacheKey, row);
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public R select() throws CacheException
    {
        // delete means no row is available
        return null;
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public UncommittedRow<R> inserted(R row) throws CacheException
    {
        // delete r1 followed by insert r2 is insert r2
        return new UncommittedInsert<>(getCacheKey(), row);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public UncommittedRow<R> updated(R row) throws CacheException
    {
        // this should never occur since java.sql.Statement.executeUpdate() on a deleted row will return 0 
        // and so this method will not be invoked
        return null;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public UncommittedRow<R> saved(R row) throws CacheException
    {
        // delete r1 followed by save r2 is save r2 
        return new UncommittedSave<>(getCacheKey(), row);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public UncommittedRow<R> deleted(R row) throws CacheException
    {
        // this should never occur since java.sql.Statement.executeUpdate() on a deleted row will return 0 
        // and so this method will not be invoked
        return null;
    }
    

    /**
     * {@inheritDoc}
     */
    @Override
    public R selected(R row) throws CacheException
    {
        // this should never occur since java.sql.Statement.executeQuery() will not select row 
        // and so this method will not be invoked
        return null;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void updateCommitted(Cache<R> cache) throws CacheException
    {
        // deleted from database so remove from committed (if exists)
        ((ReadOnlyCache<R>)cache).removeCommited(getCacheKey());
    }
}
