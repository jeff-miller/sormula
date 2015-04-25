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
package org.sormula.annotation.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.sormula.Database;
import org.sormula.Table;
import org.sormula.cache.Cache;
import org.sormula.cache.readonly.ReadOnlyCache;
import org.sormula.cache.readwrite.ReadWriteCache;
import org.sormula.cache.writable.WritableCache;
import org.sormula.cache.writable.WriteOperations;
import org.sormula.operation.DeleteOperation;
import org.sormula.operation.InsertOperation;
import org.sormula.operation.SaveOperation;
import org.sormula.operation.SqlOperation;
import org.sormula.operation.UpdateOperation;


/**
 * Annotation for row class, table class, or database class to specify caching. When used on row
 * or table class, then caching is for a single table. When used on database class, all tables in
 * the database will be cached unless overridden by other annotations or {@link SqlOperation#setCached(boolean)}.
 * <p>
 * Only rows that are read/written with Sormula operations are cached. JDBC operations are not cached.
 * Batch operations are not cached.
 * <p>
 * Each instance of a {@link Table} object contains a cache for all operations that were performed
 * with that instance of the table. For example, for two instances of {@link Table} for the same database
 * table T, there will be two caches for the database table T. To maintain one cache for table T, use
 * {@link Database#getTable(Class)} to insure only one instance of {@link Table} exists at any one time
 * for the database. 
 * <p>
 * In some cases, it may be desirable to have more than once cache per row class or database table. In
 * those situations, create separate instances of {@link Table} with the {@link Table#Table(Database, Class)} 
 * constructor.
 * <p>
 * You can use almost any mixture of cached and non cached tables with one exception. A table
 * that has a foreign key constraint must be cached with a {@link WritableCache} like 
 * {@link ReadWriteCache} if the table that it refers to is also cached with a 
 * a {@link WritableCache} like {@link ReadWriteCache}. The reason is that the foreign keys 
 * written to database may refer to a row that is in cache (not yet written to database) which will 
 * result in a foreign key constraint violation. Therefore tables that are related by
 * foreign key constraints must have compatible caching as follows:
 * <pre>
 *    Cache type for table        Cache type for table 
 *    referenced by foreign key   with foreign key      Compatible
 *    -------------------------------------------------------------
 *    none                        none                  yes
 *    none                        ReadOnlyCache.class   yes
 *    none                        ReadWriteCache.class  yes
 *    ReadOnlyCache.class         none                  yes
 *    ReadOnlyCache.class         ReadOnlyCache.class   yes
 *    ReadOnlyCache.class         ReadWriteCache.class  yes
 *    ReadWriteCache.class        none                  no - possible foreign key constraint violation
 *    ReadWriteCache.class        ReadOnlyCache.class   no - possible foreign key constraint violation
 *    ReadWriteCache.class        ReadWriteCache.class  yes
 * </pre>
 *  
 * @since 3.0 
 * @author Jeff Miller
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Cached
{
    /**
     * Allows to be explicitly disabled for selective tables. Annotate @Cached(enabled=false) for
     * table or row class when caching has been enabled at the database class for all tables. If 
     * enabled is false, then no cache will be created for table and {@link SqlOperation#setCached(boolean)}
     * will have no effect.  
     *  
     * @return true for caching; false for no will be created for table
     */
    boolean enabled() default true;
    
    
    /**
     * Cache implementation.   
     * 
     * @return ReadOnlyCache.class, ReadWriteCache.class, or other implementation of {@link Cache}
     */
    Class <? extends Cache> type() default ReadWriteCache.class;
    
    
    /**
     * Meaning of is dependent upon cache implementation specified with {@link #type()}. For {@link ReadOnlyCache}
     * and {@link ReadWriteCache}, the size is the initial capacity of cache.
     * 
     * @return the cache size constraint
     */
    int size() default 30; 

    
    /**
     * Number of seconds that row will remain in cache. Not used by {@link ReadWriteCache} and
     * {@link ReadOnlyCache}.
     * 
     * @return maximum lifetime in seconds of cached row; 0 for never expire
     */
    int expire() default 0;
    
    
    /**
     * Evicts all rows upon transaction commit or rollback. Setting to true means cache will only be used
     * while database transaction is active. Setting to false means that cached rows may be used in
     * multiple transactions.
     * 
     * @return true to remove all rows from cache when transaction completes
     */
    boolean evictOnTransactionEnd() default false;

    
    /**
     * Operation to use by writable caches when inserting rows into database from cache.
     * 
     * @return insert operation class or subclass of insert operation
     * @see WritableCache
     * @see WriteOperations
     */
    Class <? extends InsertOperation> insert() default InsertOperation.class;

    
    /**
     * Operation to use by writable caches when updating rows in database from cache.
     * 
     * @return update operation class or subclass of update operation
     * @see WritableCache
     * @see WriteOperations
     */
    Class <? extends UpdateOperation> update() default UpdateOperation.class;

    
    /**
     * Operation to use by writable caches when deleting rows from database from cache.
     * 
     * @return delete operation class or subclass of delete operation
     * @see WritableCache
     * @see WriteOperations
     */
    Class <? extends DeleteOperation> delete() default DeleteOperation.class;

    
    /**
     * Operation to use by writable caches when saving rows into database from cache. Save
     * operations are used when cache determines it is ambiguous if insert or update is needed.
     * 
     * @return save operation class or subclass of save operation
     * @see WritableCache
     * @see WriteOperations
     */
    Class <? extends SaveOperation> save() default SaveOperation.class;
}
