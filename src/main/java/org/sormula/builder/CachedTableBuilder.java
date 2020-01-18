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
package org.sormula.builder;

import org.sormula.CachedTable;
import org.sormula.Database;
import org.sormula.SormulaException;


/**
 * Builder for {@link CachedTable} using a fluent style.
 * <p>
 * Example:
 * <blockquote><pre>
 * CachedTable&lt;SomeTableRow&gt; cachedTable = 
 *     CachedTable.builderCached(getDatabase(), SomeTableRow.class).build();
 * </pre></blockquote>
 *  
 * @author Jeff Miller
 * @since 4.4
 * @param <R> Class associated with a row in table
 */public class CachedTableBuilder<R> extends AbstractTableBuilder<R, CachedTableBuilder<R>, CachedTable<R>>
{
    public CachedTableBuilder(Database database, Class<R> rowClass) 
    {
        super(database, rowClass);
    }
    
    
    public CachedTable<R> build() throws SormulaException
    {
        CachedTable<R> cachedTable = new CachedTable<R>(database, rowClass);
        init(cachedTable);
        return cachedTable;
    }
}
