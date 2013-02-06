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

import org.sormula.Table;
import org.sormula.Transaction;
import org.sormula.annotation.cache.Cached;
import org.sormula.cache.AbstractCache;
import org.sormula.cache.CacheException;
import org.sormula.log.ClassLogger;
import org.sormula.operation.SqlOperation;


/**
 * TODO
 * This is a transactional cache
 * 
 * @author Jeff Miller
 * @since 3.0
 *
 * @param <R>
 */
public class ReadOnlyCache<R> extends AbstractCache<R>
{
    private static final ClassLogger log = new ClassLogger();
    
    
    public ReadOnlyCache(Table<R> table, Cached cachedAnnotation) throws CacheException
    {
        super(table, cachedAnnotation);
    }


    public void begin(Transaction transaction)
    {
        if (log.isDebugEnabled()) log.debug("begin()");
    }

    
    public void commit(Transaction transaction)
    {
        if (log.isDebugEnabled()) log.debug("commit()");
    }

    
    public void rollback(Transaction transaction)
    {
        if (log.isDebugEnabled()) log.debug("rollback()");
    }
    
    
    public void execute(SqlOperation<R> sqlOperation) throws CacheException
    {
        // TODO 
    }


    public void close(SqlOperation<R> sqlOperation) throws CacheException
    {
        // TODO 
    }


    public R select(Object[] primaryKeys) throws CacheException
    {
        return null;
    }

    
    public boolean insert(R row) throws CacheException
    {
        return false;
    }

    
    public boolean update(R row) throws CacheException
    {
        return false;
    }

    
    public boolean delete(R row) throws CacheException
    {
        return false;
    }

    
    public R selected(R row) throws CacheException
    {
        // TODO
        return null;
    }
    

    public void inserted(R row) throws CacheException
    {
        // TODO update uncommitted cache
    }


    public void updated(R row) throws CacheException
    {
        // TODO update uncommitted cache
    }


    public void deleted(R row) throws CacheException
    {
        // TODO update uncommitted cache
    }
    
    
    public void write() throws CacheException
    {
    }
}
