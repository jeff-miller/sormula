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
package org.sormula;

import org.sormula.annotation.cache.Cached;
import org.sormula.builder.CachedTableBuilder;


/**
 * A cached table with default cache settings. This class is a subclass of {@link Table} annotated 
 * with {@link Cached} with default values for all {@link Cached} elements. 
 * <p>
 * Use this class to create a cached table when you would like a single instance of a cached
 * table but don't want to all instances of a table to cached. For example:
 * <blockquote><pre>
 * Table&lt;SomeRow&gt; table = ...
 * Table&lt;SomeRow&gt; cachedTable = new CachedTable&lt;&gt;(table);
 * </pre></blockquote>
 * <p>
 * An alternative to using this class is to subclass a table class and annotate it with {@link Cached}
 * supplying any element values that you like:
 * <blockquote><pre>
 *{@literal @}Cached(type=..., size=...) 
 * public class MyCachedTable extends Table&lt;SomeRow&gt;
 * {
 *     ...
 * } 
 * 
 * Table&lt;SomeRow&gt; cachedTable = new MyCachedTable&lt;&gt;(...);
 * </pre></blockquote>
 * 
 * @since 3.4
 * @author Jeff Miller
 *
 * @param <R> type of row objects
 */
@Cached
public class CachedTable<R> extends Table<R> 
{
    /**
     * Creates {@link CachedTableBuilder} that will be used to build a {@link CachedTable}.
     * Set builder parameters and then use {@link CachedTableBuilder#build()} to create an instance
     * of {@link CachedTable}.
     * <p>
     * This method is optional. You can also use the standard constructors and setter methods to create an instance.
     * <p>
     * The name is not "builder" since the signature would conflict with {@link #builder(Database, Class)}.
     * 
     * @param <R> Class associated with a row in table
     * @param database database for this table
     * @param rowClass row objects are of this type
     * @return builder instance
     * @since 4.4
     */
    public static <R> CachedTableBuilder<R> builderCached(Database database, Class<R> rowClass)
    {
        return new CachedTableBuilder<>(database, rowClass);
    }
    
    
    /**
     * Creates {@link CachedTableBuilder} that will be used to build a {@link CachedTable}.
     * Set builder parameters and then use {@link CachedTableBuilder#build()} to create an instance
     * of {@link CachedTable}.
     * <p>
     * This method is optional. You can also use the standard constructors and setter methods to create an instance.
     * <p>
     * The name is not "builder" to be consistent with {@link #builderCached(Database, Class)}.
     * 
     * @param <R> Class associated with a row in table
     * @param table use database and row class from this table
     * @return builder instance
     * @since 4.4
     */
    public static <R> CachedTableBuilder<R> builderCached(Table<R> table)
    {
        return new CachedTableBuilder<>(table.getDatabase(), table.getRowClass());
    }
    
    
    /**
     * Constructs from a {@link Table} that is already associated with a {@link Database}.
     * 
     * @param table use database and row class from this table 
     * @throws SormulaException if error
     */
	public CachedTable(Table<R> table) throws SormulaException 
	{
		super(table.getDatabase(), table.getRowClass());
	}
	
	
    /**
     * Constructs for a database and the class that used for row objects.
     * 
     * @param database database for this table
     * @param rowClass row objects are of this type
     * @throws SormulaException if error
     */
	public CachedTable(Database database, Class<R> rowClass) throws SormulaException 
	{
		super(database, rowClass);
	}
}
