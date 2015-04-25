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

import org.sormula.cache.CacheException;
import org.sormula.cache.CacheKey;
import org.sormula.cache.IllegalCacheOperationException;
import org.sormula.cache.UncommittedRow;


/**
 * {@link UncommittedRow} for {@link ReadOnlyCache}.
 *  
 * @author Jeff Miller
 * @since 3.0
 *
 * @param <R> cache row type
 */
public abstract class UncommittedReadOnlyRow<R> extends UncommittedRow<R>
{
    public UncommittedReadOnlyRow(CacheKey cacheKey, R row)
    {
        super(cacheKey, row);
    }
    
    
    /**
     * Throws {@link IllegalCacheOperationException} since this method should not be used
     * for {@link ReadOnlyCache}.
     */
    @Override
    public UncommittedRow<R> insert(R row) throws CacheException
    {
        throw new IllegalCacheOperationException();
    }

    
    /**
     * Throws {@link IllegalCacheOperationException} since this method should not be used
     * for {@link ReadOnlyCache}.
     */
    @Override
    public UncommittedRow<R> update(R row) throws CacheException
    {
        throw new IllegalCacheOperationException();
    }

    
    /**
     * Throws {@link IllegalCacheOperationException} since this method should not be used
     * for {@link ReadOnlyCache}.
     */
    @Override
    public UncommittedRow<R> save(R row) throws CacheException
    {
        throw new IllegalCacheOperationException();
    }

    
    /**
     * Throws {@link IllegalCacheOperationException} since this method should not be used
     * for {@link ReadOnlyCache}.
     */
    @Override
    public UncommittedRow<R> delete(R row) throws CacheException
    {
        throw new IllegalCacheOperationException();
    }
}
