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

import org.sormula.cache.Cache;
import org.sormula.cache.CacheException;
import org.sormula.cache.CacheKey;
import org.sormula.cache.UncommittedRow;
import org.sormula.cache.writable.UncommittedWritableRow;
import org.sormula.cache.writable.WriteOperations;


/**
 * Row in {@link ReadWriteCache} that has been deleted.
 * 
 * @author Jeff Miller
 * @since 3.0
 *
 * @param <R> type of cached row
 */
public class UncommittedDelete<R> extends UncommittedWritableRow<R>
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
    public UncommittedRow<R> insert(R row) throws CacheException
    {
        // delete r1 followed by insert r2 is equivalent to save r2
        return new UncommittedSave<>(getCacheKey(), row);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public UncommittedRow<R> update(R row) throws CacheException
    {
        // update on deleted row has no effect
        return this;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public UncommittedRow<R> save(R row) throws CacheException
    {
        // delete r1 followed by save r2 is equivalent to save r2
        return new UncommittedSave<>(getCacheKey(), row);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public UncommittedRow<R> delete(R row) throws CacheException
    {
        // delete on deleted row has no effect
        return this;
    }
    

    /**
     * {@inheritDoc}
     */
    @Override
    public R selected(R row) throws CacheException
    {
        return null; // row has been deleted
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void write(WriteOperations<R> writeOperations) throws CacheException
    {
        if (!isWritten())
        {
            writeOperations.delete(getRow());
            setWritten(true);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void updateCommitted(Cache<R> cache) throws CacheException
    {
        // deleted from database so remove from committed (if exists)
        ((ReadWriteCache<R>)cache).removeCommited(getCacheKey());
    }
}
