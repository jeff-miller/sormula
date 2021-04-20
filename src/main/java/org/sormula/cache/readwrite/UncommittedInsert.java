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
import org.sormula.cache.DuplicateCacheException;
import org.sormula.cache.UncommittedRow;
import org.sormula.cache.writable.UncommittedWritableRow;
import org.sormula.cache.writable.WriteOperations;


/**
 * A row that has been inserted into a writable cache but not yet written to database.
 *  
 * @author Jeff Miller
 * @since 3.0
 *
 * @param <R> cache row type
 */
public class UncommittedInsert<R> extends UncommittedWritableRow<R>
{
    /**
     * Constructs for a key and corresponding row.
     * 
     * @param cacheKey cache key based upon primary keys of row
     * @param row row corresponding to cache key
     */
    public UncommittedInsert(CacheKey cacheKey, R row)
    {
        super(cacheKey, row);
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public R select() throws CacheException
    {
        return getRow();
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public UncommittedRow<R> insert(R row) throws CacheException
    {
        throw new DuplicateCacheException(getCacheKey().getPrimaryKeys());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public UncommittedRow<R> update(R row) throws CacheException
    {
        if (isWritten())
        {
            // insert has occurred so update row
            return new UncommittedUpdate<>(getCacheKey(), row);
        }
        else
        {
            // insert r1 followed by update r2 is same as insert r2
            setRow(row); 
            return this;
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public UncommittedRow<R> save(R row) throws CacheException
    {
        if (isWritten())
        {
            // insert has occurred so update row
            return new UncommittedUpdate<>(getCacheKey(), row);
        }
        else
        {
            // insert r1 followed by save r2 is same as insert r2
            setRow(row); 
            return this;
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public UncommittedRow<R> delete(R row) throws CacheException
    {
        return null; // insert r1 followed by delete r2 is same as no change so remove uncommitted insert 
    }
    

    /**
     * {@inheritDoc}
     */
    @Override
    public R selected(R row) throws CacheException
    {
        return getRow(); // cached has authority over arbitrary row
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void write(WriteOperations<R> writeOperations) throws CacheException
    {
        if (!isWritten())
        {
            writeOperations.insert(getRow());
            setWritten(true);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void updateCommitted(Cache<R> cache) throws CacheException
    {
        ((ReadWriteCache<R>)cache).putCommitted(getCacheKey(), getRow());
    }
}
