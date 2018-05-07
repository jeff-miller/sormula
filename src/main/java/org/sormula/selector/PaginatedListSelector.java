package org.sormula.selector;

import java.sql.ResultSet;
import java.util.List;

import org.sormula.Table;
import org.sormula.annotation.OrderBy;
import org.sormula.annotation.Where;
import org.sormula.annotation.WhereField;
import org.sormula.operation.ArrayListSelectOperation;
import org.sormula.operation.ListSelectOperation;
import org.sormula.operation.OperationException;
import org.sormula.operation.ScalarSelectOperation;
import org.sormula.operation.SqlOperation;


/**
 * {@link PaginatedSelector} the is implemented with {@link ArrayListSelectOperation}. 
 * 
 * @author Jeff Miller
 * @since 4.3
 * @param <R> Class associated with a row in table
 */
public class PaginatedListSelector<R> extends PaginatedSelector<R, List<R>>
{
    Table<R> table;
    ListSelectOperation<R> selectOperation;
    
    
    /**
     * Constructs for a page size and table. Scroll sensitivity is false.
     * 
     * @param pageSize rows per page
     * @param table select rows from this table
     * @throws SelectorException if error
     */
    public PaginatedListSelector(int pageSize, Table<R> table) throws SelectorException
    {
        this(pageSize, table, false);
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
        super(pageSize, scrollSensitive);
        this.table = table;
        
        try
        {
            selectOperation = new ArrayListSelectOperation<>(table);
            init(selectOperation);
        }
        catch (OperationException e)
        {
            throw new SelectorException("error creating select operation", e);
        }
    }

    
    /** 
     * Gets order by name that was set with {@link #setOrderByName(String)}.
     * 
     * @return order by name
     * @see ScalarSelectOperation#getOrderByName() 
     */
    public String getOrderByName()
    {
        return selectOperation.getOrderByName();
    }
    
    
    /** 
     * Sets the order for the rows returned by {@link #selectPage()} and {@link #selectRow()}.
     * The default is empty string which indicates no ordering.
     * 
     * @param orderByName the name of the {@link OrderBy} to use
     * @see ScalarSelectOperation#setOrderBy(String)
     * @throws SelectorException if error
     */
    public void setOrderByName(String orderByName) throws SelectorException
    {
        try
        {
            selectOperation.setOrderBy(orderByName);
        }
        catch (OperationException e)
        {
            throw new SelectorException("error initializing order by", e);
        }
    }


    /** 
     * Gets where condition name supplied in {@link #setWhereConditionName(String)}
     * 
     * @return where condition name
     * @see SqlOperation#setWhere(String)
     */
    public String getWhereConditionName()
    {
        return selectOperation.getWhereConditionName();
    }
    
    
    /**
     * Sets the name of the {@link Where} condition to filter the rows that are returned
     * by {@link #selectPage()} and {@link #selectRow()}. The default is empty string which
     * indicates that all rows will be returned.
     * 
     * @param whereConditionName the name of the {@link Where} to use
     * @see SqlOperation#setWhere(String)
     * @throws SelectorException if error
     */
    public void setWhereConditionName(String whereConditionName) throws SelectorException
    {
        try
        {
            selectOperation.setWhere(whereConditionName);
        }
        catch (OperationException e)
        {
            throw new SelectorException("error initializing where condition", e);
        }
    }


    /**
     * Gets the parameters that were set by {@link #setWhereParameters(Object...)}. 
     * @return parameters to use in the select operation (typically the where parameters)
     * @see SqlOperation#setParameters(Object...)
     */
    public Object[] getWhereParameters()
    {
        return selectOperation.getParameters();
    }
    
    
    /**
     * Sets the parameters that correspond to {@link WhereField#operand()} that are "?". The
     * default is empty array which indicates no parameters.
     * 
     * @param whereParameters values to use for {@link Where} that was specified by {@link #setWhereConditionName(String)}
     * @see SqlOperation#setWhere(String)
     */
    public void setWhereParameters(Object... whereParameters)
    {
        selectOperation.setParameters(whereParameters);
    }


    /**
     * @return table supplied in constructor
     */
    public Table<R> getTable()
    {
        return table;
    }
}
