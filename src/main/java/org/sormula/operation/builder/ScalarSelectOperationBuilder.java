package org.sormula.operation.builder;

import org.sormula.SormulaException;
import org.sormula.operation.ScalarSelectOperation;


/**
 * Base class for builders of {@link ScalarSelectOperation} objects.
 * 
 * @author Jeff Miller
 * @since 4.4
 * @param <R> Class associated with a row in table
 * @param <B> Class of builder
 * @param <T> Class of object returned by {@link #build()}
 */
public abstract class ScalarSelectOperationBuilder<R, B extends ScalarSelectOperationBuilder, T extends ScalarSelectOperation<R>>
    extends SqlOperationBuilder<R, B, T>
{
    String orderByName;
    
    
    protected void init(T operation) throws SormulaException
    {
        super.init(operation);
        if (orderByName != null) operation.setOrderBy(orderByName);
    }
    
    
    @SuppressWarnings("unchecked")
    public B orderByName(String orderByName)
    {
        this.orderByName = orderByName;
        return (B)this;
    }
}
