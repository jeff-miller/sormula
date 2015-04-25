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
import org.sormula.cache.DuplicateCacheException;
import org.sormula.cache.UncommittedRow;
import org.sormula.cache.readwrite.UncommittedSave;


/**
 * A row that has been inserted into a {@link ReadOnlyCache} but not yet written to database.
 *  
 * @author Jeff Miller
 * @since 3.0
 *
 * @param <R> cache row type
 */
public class UncommittedInsert<R> extends UncommittedReadOnlyRow<R>
{
    public UncommittedInsert(CacheKey cacheKey, R row)
    {
        super(cacheKey, row);
    }
    
    
    @Override
    public R select() throws CacheException
    {
        return getRow();
    }
    
    
    @Override
    public UncommittedRow<R> inserted(R row) throws CacheException
    {
        // should never get here since database would throw exception prior to getting here
        throw new DuplicateCacheException(getCacheKey().getPrimaryKeys());
    }


    @Override
    public UncommittedRow<R> updated(R row) throws CacheException
    {
        // insert has occurred so update row
        return new UncommittedUpdate<R>(getCacheKey(), row);
    }


    @Override
    public UncommittedRow<R> saved(R row) throws CacheException
    {
        // insert has occurred so save row
        return new UncommittedSave<R>(getCacheKey(), row);
    }


    @Override
    public UncommittedRow<R> deleted(R row) throws CacheException
    {
        // remember that it is now deleted
        return new UncommittedDelete<R>(getCacheKey(), row);
    }
    

    @Override
    public R selected(R row) throws CacheException
    {
        return getRow(); // cached has authority over arbitrary row
    }


    @Override
    public void updateCommitted(Cache<R> cache) throws CacheException
    {
        ((ReadOnlyCache<R>)cache).putCommitted(getCacheKey(), getRow());
    }
}
