package org.sormula.operation.builder;

import org.sormula.SormulaException;
import org.sormula.operation.ScalarSelectOperation;


/**
 * Base class for builders of {@link ScalarSelectOperation} objects.
 * 
 * @author Jeff Miller
 * @since 4.4
 * @param <R> class associated with a row in table
 * @param <B> class of builder
 * @param <T> class of object returned by {@link #build()}
 */
public abstract class ScalarSelectOperationBuilder<R, B extends ScalarSelectOperationBuilder, T extends ScalarSelectOperation<R>>
    extends SqlOperationBuilder<R, B, T>
{
    String orderByName;
    Integer maximumRowsRead;
    R rowParameters;
    
    
    protected void init(T operation) throws SormulaException
    {
        super.init(operation);
        if (orderByName != null) operation.setOrderBy(orderByName);
        if (maximumRowsRead != null) operation.setMaximumRowsRead(maximumRowsRead);
        if (rowParameters != null) operation.setRowParameters(rowParameters);
    }
    
    
    @SuppressWarnings("unchecked")
    public B orderBy(String orderByName)
    {
        this.orderByName = orderByName;
        return (B)this;
    }
    
    
    @SuppressWarnings("unchecked")
    public B maximumRowsRead(int maximumRowsRead)
    {
        this.maximumRowsRead = maximumRowsRead;
        return (B)this;
    }
    
    
    @SuppressWarnings("unchecked")
    public B rowParameters(R rowParameters)
    {
        this.rowParameters = rowParameters;
        return (B)this;
    }
}
