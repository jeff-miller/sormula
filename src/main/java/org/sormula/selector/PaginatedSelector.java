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
    int pageSize; // TODO name rowsPerPage?
    int pageNumber;
    SelectOperation<R, C> selectOperation;

    
    public PaginatedSelector(SelectOperation<R, C> selectOperation, int pageSize) throws SelectorException
    {
        this(selectOperation, pageSize, false);
    }
    
    
    public PaginatedSelector(SelectOperation<R, C> selectOperation, int pageSize, boolean scrollSensitive) throws SelectorException
    {
        int resultSetType;
        if (scrollSensitive) resultSetType = ResultSet.TYPE_SCROLL_SENSITIVE;
        else                 resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE;
        init(selectOperation, pageSize, resultSetType);
    }
    
    
    protected void init(SelectOperation<R, C> selectOperation, int pageSize, int resultSetType) throws SelectorException
    {
        this.selectOperation = selectOperation;
        this.pageSize = pageSize;
        selectOperation.setMaximumRowsRead(pageSize);
        selectOperation.setResultSetType(resultSetType);
        
        try
        {
            selectOperation.execute();
        }
        catch (OperationException e)
        {
            throw new SelectorException("initialization error", e);
        }
        
        setPageNumber(1);
    }
    
    
    public int getPageSize()
    {
        return pageSize;
    }


    public int getPageNumber()
    {
        return pageNumber;
    }


    public void setPageNumber(int pageNumber) throws SelectorException 
    {
        if (pageNumber > 0)
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
