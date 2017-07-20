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
 * A row that has been selected from database and stored in a writable cache but not yet committed.
 *  
 * @author Jeff Miller
 * @since 3.0
 *
 * @param <R> cache row type
 */
public class UncommittedSelect<R> extends UncommittedWritableRow<R>
{
    public UncommittedSelect(CacheKey cacheKey, R row)
    {
        super(cacheKey, row);
    }

    
    @Override
    public R select() throws CacheException
    {
        return getRow();
    }

    
    @Override
    public UncommittedRow<R> insert(R row) throws CacheException
    {
        throw new DuplicateCacheException(getCacheKey().getPrimaryKeys());
    }

    
    @Override
    public UncommittedRow<R> update(R row) throws CacheException
    {
        // select r1 followed by update r2 for same key is equivalent to update r2
        return new UncommittedUpdate<>(getCacheKey(), row);
    }

    
    @Override
    public UncommittedRow<R> save(R row) throws CacheException
    {
        // select r1 followed by save r2 for same key is equivalent to save r2
        return new UncommittedSave<>(getCacheKey(), row);
    }

    
    @Override
    public UncommittedRow<R> delete(R row) throws CacheException
    {
        // select r1 followed by delete r2 for same key is equivalent to delete r2
        return new UncommittedDelete<>(getCacheKey(), row);
    }
    

    @Override
    public R selected(R row) throws CacheException
    {
        return getRow(); // cached has authority over arbitrary row
    }
    
    
    @Override
    public void write(WriteOperations<R> writeOperations) throws CacheException
    {
        // no writes needed for a select
    }


    @Override
    public void updateCommitted(Cache<R> cache) throws CacheException
    {
        // add to committed (if not already there)
        ((ReadWriteCache<R>)cache).putCommitted(getCacheKey(), getRow());
    }
}
