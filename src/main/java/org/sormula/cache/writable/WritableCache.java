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

import org.sormula.Table;
import org.sormula.Transaction;
import org.sormula.annotation.cache.Cached;
import org.sormula.cache.AbstractCache;
import org.sormula.cache.CacheException;
import org.sormula.cache.UncommittedRow;
import org.sormula.log.SormulaLogger;
import org.sormula.log.SormulaLoggerFactory;


/**
 * Abstract base class for implementing a cache that writes to the database upon transaction commit. 
 * 
 * @author Jeff Miller
 * @since 3.0
 *
 * @param <R> cache row type
 */
public abstract class WritableCache<R> extends AbstractCache<R>
{
    private static final SormulaLogger log = SormulaLoggerFactory.getClassLogger();
    
    
    /**
     * Constructs for a table and annotation.
     * 
     * @param table cache rows for this table
     * @param cachedAnnotation cache annotation that defines cache parameters
     * @throws CacheException if error
     */
    public WritableCache(Table<R> table, Cached cachedAnnotation) throws CacheException
    {
        super(table, cachedAnnotation);
    }


    /**
     * Writes any uncommitted rows to database with {@link #wait()} and then commits them 
     * by invoking {@link AbstractCache#commit(Transaction)}.
     * 
     * @param transaction database transaction
     * @throws CacheException if error
     */
    @Override
    public void commit(Transaction transaction) throws CacheException
    {
        if (log.isDebugEnabled()) log.debug("commit()");
        write();
        super.commit(transaction);
    }
    
    
    /**
     * Writes uncommitted rows to database by invoking {@link UncommittedWritableRow#write(WriteOperations)}
     * on all rows in {@link #getUncommittedCache()}.
     * 
     * @throws CacheException if error
     */
    public void write() throws CacheException
    {
        if (log.isDebugEnabled()) log.debug("write()");

        try (WriteOperations<R> writeOperations = new WriteOperations<>(this))
        {
            // init
            writeOperations.open();
            
            // write to database
            for (UncommittedRow<R> uncommittedRow : getUncommittedCache().values())
            {
                ((UncommittedWritableRow<R>)uncommittedRow).write(writeOperations);
            }
        }
    }
}
