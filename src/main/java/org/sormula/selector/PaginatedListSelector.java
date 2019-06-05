package org.sormula.selector;

import java.sql.ResultSet;
import java.util.List;

import org.sormula.Table;
import org.sormula.operation.ArrayListSelectOperation;
import org.sormula.operation.ListSelectOperation;
import org.sormula.selector.builder.PaginatedListSelectorBuilder;


/**
 * A selector where each page of rows are a {@link List}. It is implemented with a {@link ArrayListSelectOperation}. 
 * 
 * @author Jeff Miller
 * @since 4.3
 * @param <R> Class associated with a row in table
 */
public class PaginatedListSelector<R> extends AbstractPaginatedListSelector<R>
{
	/**
	 * Creates {@link PaginatedListSelectorBuilder} that will be used to build a {@link PaginatedListSelector}.
	 * Set builder parameters and then use {@link PaginatedListSelectorBuilder#build()} to create an instance
	 * of {@link PaginatedListSelector}.
	 * <p>
	 * This method is optional. You can also use the standard constructors and setter methods to create an instance.
	 * 
	 * @param <R> Class associated with a row in table
     * @param pageSize rows per page
     * @param table select rows from this table
	 * @return builder instance
	 * @since 4.4
	 */
    public static <R> PaginatedListSelectorBuilder<R> builder(int pageSize, Table<R> table)
    {
    	return new PaginatedListSelectorBuilder<R>(pageSize, table);
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
