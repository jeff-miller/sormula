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
    String orderByName;
    String whereConditionName;
    Object[] whereParameters;
    
    
    /**
     * Constructs for a page size and table. Scroll sensitivity is false.
     * 
     * @param pageSize rows per page
     * @param table select rows from this table
     */
    public PaginatedListSelector(int pageSize, Table<R> table)
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
     */
    public PaginatedListSelector(int pageSize, Table<R> table, boolean scrollSensitive)
    {
        super(pageSize, scrollSensitive);
        this.table = table;
        
        // defaults
        orderByName = ""; // no order
        whereConditionName = ""; // all rows
        whereParameters = new Object[0]; // no parameters
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public void execute() throws SelectorException
    {
        ListSelectOperation<R> selectOperation;
        try
        {
            selectOperation = new ArrayListSelectOperation<>(table);
            selectOperation.setOrderBy(orderByName);
            selectOperation.setWhere(whereConditionName);
            selectOperation.setParameters(whereParameters);
        }
        catch (OperationException e)
        {
            throw new SelectorException("error creating select operation", e);
        }
        
        init(selectOperation);
        super.execute();
    }


    /** 
     * Gets order by name that was set with {@link #setOrderByName(String)}.
     * 
     * @return order by name
     * @see ScalarSelectOperation#getOrderByName() 
     */
    public String getOrderByName()
    {
        return orderByName;
    }
    
    
    /** 
     * Sets the order for the rows returned by {@link #selectPage()} and {@link #selectRow()}.
     * The default is empty string which indicates no ordering.
     * 
     * @param orderByName the name of the {@link OrderBy} to use
     * @see ScalarSelectOperation#setOrderBy(String)
     */
    public void setOrderByName(String orderByName)
    {
        this.orderByName = orderByName;
    }


    /** 
     * Gets where condition name supplied in {@link #setWhereConditionName(String)}
     * 
     * @return where condition name
     * @see SqlOperation#setWhere(String)
     */
    public String getWhereConditionName()
    {
        return whereConditionName;
    }
    
    
    /**
     * Sets the name of the {@link Where} condition to filter the rows that are returned
     * by {@link #selectPage()} and {@link #selectRow()}. The default is empty string which
     * indicates that all rows will be returned.
     * 
     * @param whereConditionName the name of the {@link Where} to use
     * @see SqlOperation#setWhere(String)
     */
    public void setWhereConditionName(String whereConditionName)
    {
        this.whereConditionName = whereConditionName;
    }


    /**
     * Gets the parameters that were set by {@link #setWhereParameters(Object...)}. 
     * @return parameters to use in the select operation (typically the where parameters)
     * @see SqlOperation#setParameters(Object...)
     */
    public Object[] getWhereParameters()
    {
        return whereParameters;
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
        this.whereParameters = whereParameters;
    }


    /**
     * @return table supplied in constructor
     */
    public Table<R> getTable()
    {
        return table;
    }
}
