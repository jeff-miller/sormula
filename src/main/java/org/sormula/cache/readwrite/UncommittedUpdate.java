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
 * A row that has been updated in a writable cache but not yet written to database.
 *  
 * @author Jeff Miller
 * @since 3.0
 *
 * @param <R> cache row type
 */
public class UncommittedUpdate<R> extends UncommittedWritableRow<R>
{
    public UncommittedUpdate(CacheKey cacheKey, R row)
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
        // assume update is no-op because row does not yet exist in db, so insert will be ok
        // if assumption is wrong then can't know until commit which will fail  
        return new UncommittedInsert<>(getCacheKey(), row);
    }


    @Override
    public UncommittedRow<R> update(R row) throws CacheException
    {
        // update r1 followed by update r2 is equivalent to update r2
        setRow(row);
        setWritten(false);
        return this;
    }


    @Override
    public UncommittedRow<R> save(R row) throws CacheException
    {
        // update r1 followed by save r2 is equivalent to save r2
        return new UncommittedSave<>(getCacheKey(), row);
    }


    @Override
    public UncommittedRow<R> delete(R row) throws CacheException
    {
        // update r1 followed by delete r2 is equivalent to delete r2
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
        if (!isWritten())
        {
            writeOperations.update(getRow());
            setWritten(true);
        }
    }


    @Override
    public void updateCommitted(Cache<R> cache) throws CacheException
    {
        ((ReadWriteCache<R>)cache).putCommitted(getCacheKey(), getRow());
    }
}
