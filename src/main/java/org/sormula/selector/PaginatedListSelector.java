package org.sormula.selector;

import java.sql.ResultSet;

import org.sormula.Table;
import org.sormula.operation.ArrayListSelectOperation;
import org.sormula.operation.ListSelectOperation;


/**
 * {@link AbstractPaginatedListSelector} that is implemented with {@link ArrayListSelectOperation}. 
 * 
 * @author Jeff Miller
 * @since 4.3
 * @param <R> Class associated with a row in table
 */
public class PaginatedListSelector<R> extends AbstractPaginatedListSelector<R>
{
	/**
	 * Creates {@link Builder} that will be used to build a {@link PaginatedListSelector}.
	 * Set builder parameters and then use {@link Builder#build()} to create an instance
	 * of {@link PaginatedListSelector}.
	 * 
	 * @param <R> Class associated with a row in table
     * @param pageSize rows per page
     * @param table select rows from this table
	 * @return builder instance
	 * @since 4.4
	 */
    public static <R> Builder<R> builder(int pageSize, Table<R> table)
    {
    	return new Builder<R>(pageSize, table);
    }
    
    
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
     * @param <R> Class associated with a row in table
     */
    public static class Builder<R> extends AbstractPaginatedListSelector.Builder<R, Builder<R>, PaginatedListSelector<R>>
    {
        int pageSize;
    	Table<R> table;

		public Builder(int pageSize, Table<R> table) 
		{
		    this.pageSize = pageSize;
			this.table = table;
		}

		@Override
		public PaginatedListSelector<R> build() throws SelectorException
		{
			PaginatedListSelector<R> paginatedListSelector = new PaginatedListSelector<>(pageSize, table, scrollSensitive);
			init(paginatedListSelector);
			paginatedListSelector.execute();
			return paginatedListSelector; 
		}
	}
    
    
    /**
     * Constructs for a page size and table. Scroll sensitivity is false.
     * 
     * @param pageSize rows per page
     * @param table select rows from this table
     * @throws SelectorException if error
     */
    public PaginatedListSelector(int pageSize, Table<R> table) throws SelectorException
    {
        super(pageSize, table, false);
    }
    
    
    /**
     * Constructs for a page size, table and scroll sensitivity.
     * 
     * @param pageSize rows per page
     * @param table select rows from this table
     * @param scrollSensitive true to use result set type of {@link ResultSet#TYPE_SCROLL_SENSITIVE}, 
     * false to use {@link ResultSet#TYPE_SCROLL_INSENSITIVE}
     * @throws SelectorException if error
     */
    public PaginatedListSelector(int pageSize, Table<R> table, boolean scrollSensitive) throws SelectorException
    {
        super(pageSize, table, scrollSensitive);
    }
    
    
    @Override
    protected ListSelectOperation<R> createListSelectOperation() throws Exception
    {
        return new ArrayListSelectOperation<>(table);
    }
}
