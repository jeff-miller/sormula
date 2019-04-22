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
	 * TODO
	 * @param <R>
	 * @param pageSize
	 * @param table
	 * @return
	 * @since 4.4
	 */
    public static <R> Builder<R> builder(int pageSize, Table<R> table)
    {
    	return new Builder<R>(pageSize, table);
    }
    
    
    public static class Builder<R> extends AbstractPaginatedListSelector.Builder<R>
    {
		public Builder(int pageSize, Table<R> table) {
			super(pageSize, table);
		}

		@Override
		public PaginatedListSelector<R> build() throws SelectorException
		{
			PaginatedListSelector<R> paginatedListSelector = new PaginatedListSelector<>(pageSize, table);
			paginatedListSelector.setPageNumber(pageNumber);
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
