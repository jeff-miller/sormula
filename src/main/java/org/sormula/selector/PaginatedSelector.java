package org.sormula.selector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.sormula.operation.OperationException;
import org.sormula.operation.SelectOperation;


/**
 * The base class for all selectors that return pages of rows using some {@link SelectOperation}. The
 * same select operation is used for all pages so that only one {@link PreparedStatement} is created
 * for all pages.
 * 
 * @author Jeff Miller
 * @since 4.3
 * @param <R> Class associated with a row in table
 * @param <C> collection type for one page
 */
public class PaginatedSelector<R, C> implements AutoCloseable
{
    int pageSize;
    boolean scrollSensitive;
    int resultSetType;
    int pageNumber;
    SelectOperation<R, C> selectOperation;

    
    /**
     * Constructs for a page size and select operation. Scroll sensitivity is false.
     * 
     * @param pageSize rows per page
     * @param selectOperation the operation to select rows
     */
    public PaginatedSelector(int pageSize, SelectOperation<R, C> selectOperation)
    {
        this(pageSize, selectOperation, false);
    }

    
    /**
     * Constructs for a page size, select operation, and scroll sensitivity.
     * 
     * @param pageSize rows per page
     * @param selectOperation the operation to select rows
     * @param scrollSensitive true to use result set type of {@link ResultSet#TYPE_SCROLL_SENSITIVE}, 
     * false to use {@link ResultSet#TYPE_SCROLL_INSENSITIVE}
     */
    public PaginatedSelector(int pageSize, SelectOperation<R, C> selectOperation, boolean scrollSensitive)
    {
        this(pageSize, scrollSensitive);
        init(selectOperation);
    }
    
    
    /**
     * Constructs for a page size and scroll sensitivity. Select operation must be set with
     * {@link #init(SelectOperation)}. This constructor is for subclasses that initialize
     * the select operation in a specialized way.
     * 
     * @param pageSize rows per page
     * @param scrollSensitive true to use result set type of {@link ResultSet#TYPE_SCROLL_SENSITIVE}, 
     * false to use {@link ResultSet#TYPE_SCROLL_INSENSITIVE}
     */
    protected PaginatedSelector(int pageSize, boolean scrollSensitive)
    {
        this.pageSize = pageSize;
        this.scrollSensitive = scrollSensitive;
        if (scrollSensitive) resultSetType = ResultSet.TYPE_SCROLL_SENSITIVE;
        else                 resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE;
        pageNumber = 1;
    }
    
    
    protected void init(SelectOperation<R, C> selectOperation)
    {
        this.selectOperation = selectOperation;
    }
    
    
    /**
     * Executes the select operation. Positions to specific page if {@link #setPageNumber(int)} has been
     * used, otherwise positions to the first page.
     * 
     * @throws SelectorException if error
     * @see SelectOperation#execute()
     */
    public void execute() throws SelectorException
    {
        try
        {
            selectOperation.setResultSetType(resultSetType);
            selectOperation.setMaximumRowsRead(pageSize);
            selectOperation.execute();
        }
        catch (OperationException e)
        {
            throw new SelectorException("initialization error", e);
        }
        
        setPageNumber(pageNumber);
    }
    
    
    protected void confirmExecuted() throws SelectorException
    {
        if (!isExecuted())
        {
            throw new SelectorException("execute method must be invoked prior to page access");
        }
    }
    
    
    protected boolean isExecuted()
    {
    	return selectOperation != null && selectOperation.isExecuted();
    }
    
    
    /**
     * Gets the page size that was specified in the constructor.
     * 
     * @return rows per page
     */
    public int getPageSize()
    {
        return pageSize;
    }


    /**
     * Gets the result set scroll sensitivity that was specified in the constructor.
     * 
     * @return true if scrolling is sensitive to changes in the table; false if not
     * @see Connection#prepareStatement(String, int, int)
     */
    public boolean isScrollSensitive()
    {
        return scrollSensitive;
    }


    /**
     * Gets the page number that was set with {@link #setPageNumber(int)}, {@link #nextPage()}, {@link #previousPage()}.
     * 
     * @return the page number to use for {@link #selectPage()}, {@link #selectRow()}
     */
    public int getPageNumber()
    {
        return pageNumber;
    }


    /**
     * Positions result set cursor to a specific page. If not yet executed, then page number will
     * be the initial page upon {@link #execute()}.
     * <p>
     * If page number is greater than the total number of
     * pages, no exception will occur but {@link #selectPage()} will be empty and {@link #selectRow()} will return null.
     * 
     * @param pageNumber position to specific page, pages less than 1 will cause an exception
     * @throws SelectorException if error
     * @see SelectOperation#positionAbsolute(int)
     */
    public void setPageNumber(int pageNumber) throws SelectorException 
    {
        if (pageNumber > 0)
        {
            this.pageNumber = pageNumber;
            if (isExecuted()) topOfPage();
        }
        else
        {
            throw new SelectorException("page number must be greater than zero");
        }
    }
    
    
    /**
     * Move result set cursor to {@link #getPageNumber()} + 1
     * @throws SelectorException if error
     */
    public void nextPage() throws SelectorException
    {
        setPageNumber(pageNumber + 1);
    }
    
    
    /**
     * Move result set cursor to {@link #getPageNumber()} - 1
     * @throws SelectorException if error
     */
    public void previousPage() throws SelectorException
    {
        setPageNumber(pageNumber - 1);
    }
    
    
    /**
     * Selects rows for current page. This method can be used multiple times to select the same page.
     * 
     * @return collection of rows for the page; empty collection if page number is beyond the maximum page
     * @throws SelectorException if error
     */
    public C selectPage() throws SelectorException
    {
        confirmExecuted();
        try
        {
            if (selectOperation.getRowsReadCount() > 0)
            {
                // cursor is not at top of page
                topOfPage();    
            }
            
            return selectOperation.readAll();
        }
        catch (OperationException e)
        {
            throw new SelectorException("select page error", e);
        }
    }
    
    
    /**
     * Selects next row from the current page. Invoke {@link #setPageNumber(int)} prior 
     * to {@link #selectRow()} to start at top of page. Use this method instead of {@link #selectPage()}
     * if you would like to read one row at-a-time rather than the whole page.
     * 
     * @return row or null if no more rows on the page
     * @throws SelectorException if error
     */
    public R selectRow() throws SelectorException
    {
        confirmExecuted();
        try
        {
            return selectOperation.readNext();
        }
        catch (OperationException e)
        {
            throw new SelectorException("select page row error", e);
        }
    }


    /**
     * Closes the select operation with {@link SelectOperation#close()}.
     */
    @Override
    public void close() throws SelectorException
    {
        if (selectOperation != null)
        {
            try
            {
                selectOperation.close();
            }
            catch (OperationException e)
            {
                throw new SelectorException("close error", e);
            }
        }
    }
    
    
    /**
     * Positions result set cursor to the top of the page.
     * 
     * @throws SelectorException if error
     */
    protected void topOfPage() throws SelectorException
    {
        try
        {
            selectOperation.positionAbsolute(pageSize * (pageNumber - 1)); // selectOperation#readNext points to first row of page
        }
        catch (OperationException e)
        {
            throw new SelectorException("position to row error", e);
        }
            
        selectOperation.resetRowsReadCount();
    }
}
