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


/**
 * Implementation of {@link org.sormula.cache.readonly.ReadOnlyCache}. It is
 * specified with {@link org.sormula.annotation.cache.Cached#type()} by annotating a 
 * row class, {@link org.sormula.Table}, or {@link org.sormula.Database}.
 * <p>
 * Specify cache on class that is used for row:
 * <blockquote><pre>
 * &#64;Cached(type=ReadOnlyCache.class)
 * public class SomeRow 
 * {
 *     ...
 * }
 * </pre></blockquote>
 * <p>
 * Specify cache on Table class:
 * <blockquote><pre>
 * &#64;Cached(type=ReadOnlyCache.class)
 * public class SomeTable extends Table&lt;SomeRow&gt; 
 * {
 *     ...
 * }
 * </pre></blockquote>
 * <p>
 * Specify cache on Database class (all tables will be cached):
 * <blockquote><pre>
 * &#64;Cached(type=ReadOnlyCache.class)
 * public class SomeDatabase extends Database 
 * {
 *     ...
 * }
 * 
 * SomeDatabase db = new SomeDatabase(...);
 * Table&lt;SomeRow&gt; table = db.getTable(SomeRow.class);
 * </pre></blockquote>
 * 
 * @since 3.0
 */
package org.sormula.cache.readonly;