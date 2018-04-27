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
public class PaginatedSelector<R, C> // TODO name Paginator?
{
    int pageSize;
    int pageNumber;
    SelectOperation<R, C> selectOperation;
    
    
    /**
     * TODO
     * @param pageSize
     * @param selectOperation
     * @throws OperationException
     */
    public PaginatedSelector(int pageSize, SelectOperation<R, C> selectOperation) throws OperationException
    {
        this.pageSize = pageSize;
        selectOperation.setMaximumRowsRead(pageSize);
        selectOperation.setResultSetType(ResultSet.TYPE_SCROLL_INSENSITIVE); // TODO allow both types of scroll
        selectOperation.execute();
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


    public void setPageNumber(int pageNumber) throws OperationException 
    {
        selectOperation.positionAbsolute(pageSize * (pageNumber - 1) + 1);
        selectOperation.resetRowsReadCount();
        this.pageNumber = pageNumber;
    }
    
    
    public void nextPage() throws OperationException
    {
        setPageNumber(pageNumber + 1);
    }
    
    
    public void previousPage() throws OperationException
    {
        setPageNumber(pageNumber - 1);
    }
    
    
    // TODO stream method?
    
    /**
     * Note: if {@link #selectRow()} has been used since the most recent start of page, then {@link #selectPage()}
     * return will contain remaining rows in the page and will not include rows read with {@link #selectRow()}.
     * 
     * @return
     * @throws OperationException
     */
    public C selectPage() throws OperationException
    {
        return selectOperation.readAll();
    }
    
    
    public R selectRow() throws OperationException
    {
        return selectOperation.readNext();
    }
}
