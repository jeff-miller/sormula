/* sormula - Simple object relational mapping
 * Copyright (C) 2011-2020 Jeff Miller
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
package org.sormula.selector.builder;

import org.sormula.Table;
import org.sormula.selector.PaginatedListSelector;
import org.sormula.selector.SelectorException;

/**
 * Class for building a {@link PaginatedListSelector} instance using a fluent style.
 * <p>
 * Example:
 * <blockquote><pre>
 *     try (PaginatedListSelector&lt;SormulaPsTest&gt; selector = 
 *         PaginatedListSelector.builder(rowsPerPage, getTable())
 *         .where("selectByType")
 *         .parameter("type", 2)
 *         .orderByName(orderByName)
 *         .pageNumber(3)
 *         .build())
 *     {
 *         List&lt;SormulaPsTest&gt; selectedPageRows = selector.selectPage();
 *     }
 * </pre></blockquote>
 *  
 * @author Jeff Miller
 * @since 4.4
 * @param <R> class associated with a row in table
 */
public class PaginatedListSelectorBuilder<R> 
    extends AbstractPaginatedListSelectorBuilder<R, PaginatedListSelectorBuilder<R>, PaginatedListSelector<R>>
{
    int pageSize;
    Table<R> table;

    
    /**
     * Constructs for a page size and table.
     * 
     * @param pageSize number of rows in a page
     * @param table table to read rows from
     */
    public PaginatedListSelectorBuilder(int pageSize, Table<R> table) 
    {
        this.pageSize = pageSize;
        this.table = table;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public PaginatedListSelector<R> build() throws SelectorException
    {
        PaginatedListSelector<R> paginatedListSelector;
        if (scrollSensitive != null) paginatedListSelector = new PaginatedListSelector<>(pageSize, table, scrollSensitive);
        else                         paginatedListSelector = new PaginatedListSelector<>(pageSize, table);
        init(paginatedListSelector);
        return paginatedListSelector; 
    }
}
