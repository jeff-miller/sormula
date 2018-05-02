package org.sormula.selector;

import java.util.List;

import org.sormula.Table;
import org.sormula.operation.ArrayListSelectOperation;
import org.sormula.operation.ListSelectOperation;
import org.sormula.operation.OperationException;


/**
 * TODO 
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
    
    
    public PaginatedListSelector(int pageSize, Table<R> table) throws SelectorException
    {
        super(pageSize, null); // operation created in #execute()
        this.table = table;
        
        // defaults
        orderByName = ""; // no order
        whereConditionName = ""; // all rows
        whereParameters = new Object[0];
    }

    
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


    public String getOrderByName()
    {
        return orderByName;
    }
    public void setOrderByName(String orderByName)
    {
        this.orderByName = orderByName;
    }


    public String getWhereConditionName()
    {
        return whereConditionName;
    }
    public void setWhereConditionName(String whereConditionName)
    {
        this.whereConditionName = whereConditionName;
    }


    public Object[] getWhereParameters()
    {
        return whereParameters;
    }
    public void setWhereParameters(Object[] whereParameters)
    {
        this.whereParameters = whereParameters;
    }


    public Table<R> getTable()
    {
        return table;
    }
}
