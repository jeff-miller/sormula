package org.sormula.selector;

import java.sql.ResultSet;

import org.sormula.operation.OperationException;
import org.sormula.operation.SelectOperation;

// TODO OperationException should be replace with SelectorException

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

    
    public PaginatedSelector(SelectOperation<R, C> selectOperation, int pageSize) throws OperationException
    {
        this(selectOperation, pageSize, false);
    }
    
    
    public PaginatedSelector(SelectOperation<R, C> selectOperation, int pageSize, boolean scrollSensitive) throws OperationException
    {
        int resultSetType;
        if (scrollSensitive) resultSetType = ResultSet.TYPE_SCROLL_SENSITIVE;
        else                 resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE;
        init(selectOperation, pageSize, resultSetType);
    }
    
    
    protected void init(SelectOperation<R, C> selectOperation, int pageSize, int resultSetType) throws OperationException
    {
        this.selectOperation = selectOperation;
        this.pageSize = pageSize;
        selectOperation.setMaximumRowsRead(pageSize);
        selectOperation.setResultSetType(resultSetType);
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
        if (pageNumber > 0)
        {
            selectOperation.positionAbsolute(pageSize * (pageNumber - 1)); // selectOperation#readNext points to first row of page
            selectOperation.resetRowsReadCount();
            this.pageNumber = pageNumber;
        }
        else
        {
            throw new OperationException("page number must be greater than zero");
        }
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
