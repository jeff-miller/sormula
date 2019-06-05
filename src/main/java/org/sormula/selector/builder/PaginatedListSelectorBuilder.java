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

    public PaginatedListSelectorBuilder(int pageSize, Table<R> table) 
    {
        this.pageSize = pageSize;
        this.table = table;
    }

    @Override
    public PaginatedListSelector<R> build() throws SelectorException
    {
        PaginatedListSelector<R> paginatedListSelector = new PaginatedListSelector<>(pageSize, table, scrollSensitive);
        init(paginatedListSelector);
        return paginatedListSelector; 
    }
}
