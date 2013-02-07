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
package org.sormula.cache.writable;

import org.sormula.cache.CacheException;
import org.sormula.cache.CacheKey;
import org.sormula.cache.IllegalCacheOperationException;
import org.sormula.cache.UncommittedRow;


/**
 * {@link UncommittedRow} that can be written to database with {@link #write(WriteOperations)}.
 *  
 * @author Jeff Miller
 * @since 3.0
 *
 * @param <R> cache row type
 */
public abstract class UncommittedWritableRow<R> extends UncommittedRow<R>
{
    boolean written;
    
    
    public UncommittedWritableRow(CacheKey cacheKey, R row)
    {
        super(cacheKey, row);
    }
    
    
    /**
     * Writes row to database.
     * 
     * @param writeOperations modify operations to use for writing
     * @throws CacheException if error
     */
    public abstract void write(WriteOperations<R> writeOperations) throws CacheException;


    /**
     * Indicates that row has been written to database.
     *  
     * @return true if row has been written to database
     */
    public boolean isWritten()
    {
        return written;
    }


    /**
     * Sets database write status. Typically false when row is added to cache and then set to
     * true when row is written by {@link #write(WriteOperations)}.
     * 
     * @param written true means row has been written to database; false if not written
     */
    public void setWritten(boolean written)
    {
        this.written = written;
    }


    /**
     * Throws {@link IllegalCacheOperationException} since this method should not be used.
     */
    @Override
    public UncommittedRow<R> inserted(R row) throws CacheException
    {
        throw new IllegalCacheOperationException();
    }

    
    /**
     * Throws {@link IllegalCacheOperationException} since this method should not be used.
     */
    @Override
    public UncommittedRow<R> updated(R row) throws CacheException
    {
        throw new IllegalCacheOperationException();
    }

    
    /**
     * Throws {@link IllegalCacheOperationException} since this method should not be used.
     */
    @Override
    public UncommittedRow<R> deleted(R row) throws CacheException
    {
        throw new IllegalCacheOperationException();
    }
}
