package org.sormula.selector;

import java.sql.ResultSet;

import org.sormula.operation.OperationException;
import org.sormula.operation.SelectOperation;


/**
 * TODO 
 * 
 * @author Jeff Miller
 * @since 4.3
 * @param <R> Class associated with a row in table
 * @param <C> collection type for one page
 */
public class PaginatedSelector<R, C>
{
    int pageSize;
    boolean scrollSensitive;
    int resultSetType;
    int pageNumber;
    SelectOperation<R, C> selectOperation;

    
    public PaginatedSelector(int pageSize, SelectOperation<R, C> selectOperation)
    {
        this(pageSize, selectOperation, false);
    }

    
    public PaginatedSelector(int pageSize, SelectOperation<R, C> selectOperation, boolean scrollSensitive)
    {
        this(pageSize, scrollSensitive);
        init(selectOperation);
    }
    
    
    protected PaginatedSelector(int pageSize, boolean scrollSensitive)
    {
        this.pageSize = pageSize;
        this.scrollSensitive = scrollSensitive;
        if (scrollSensitive) resultSetType = ResultSet.TYPE_SCROLL_SENSITIVE;
        else                 resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE;
    }
    
    
    // TODO name?
    protected void init(SelectOperation<R, C> selectOperation)
    {
        this.selectOperation = selectOperation;
    }
    
    
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
        
        setPageNumber(1);
    }
    
    
    protected void confirmExecuted() throws SelectorException
    {
        if (!selectOperation.isExecuted()) throw new SelectorException("execute method must be invoked prior to page access");
    }
    
    
    public int getPageSize()
    {
        return pageSize;
    }


    public boolean isScrollSensitive()
    {
        return scrollSensitive;
    }


    public int getPageNumber()
    {
        return pageNumber;
    }


    public void setPageNumber(int pageNumber) throws SelectorException 
    {
        if (pageNumber > 0)
        {
            confirmExecuted();
            try
            {
                selectOperation.positionAbsolute(pageSize * (pageNumber - 1)); // selectOperation#readNext points to first row of page
            }
            catch (OperationException e)
            {
                throw new SelectorException("position to row error", e);
            }
                
            selectOperation.resetRowsReadCount();
            this.pageNumber = pageNumber;
        }
        else
        {
            throw new SelectorException("page number must be greater than zero");
        }
    }
    
    
    public void nextPage() throws SelectorException
    {
        setPageNumber(pageNumber + 1);
    }
    
    
    public void previousPage() throws SelectorException
    {
        setPageNumber(pageNumber - 1);
    }
    
    
    // TODO stream method?
    
    /**
     * Note: if {@link #selectRow()} has been used since the most recent start of page, then {@link #selectPage()}
     * return will contain remaining rows in the page and will not include rows read with {@link #selectRow()}.
     * 
     * @return
     * @throws SelectorException
     */
    public C selectPage() throws SelectorException
    {
        confirmExecuted();
        try
        {
            return selectOperation.readAll();
        }
        catch (OperationException e)
        {
            throw new SelectorException("select page error", e);
        }
    }
    
    
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
}
