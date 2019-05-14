package org.sormula.operation.builder;

import org.sormula.SormulaException;
import org.sormula.operation.SelectOperation;


/**
 * Base class for builders of {@link SelectOperation} objects.
 * 
 * @author Jeff Miller
 * @since 4.4
 * @param <R> Class associated with a row in table
 * @param <B> Class of builder
 * @param <T> Class of object returned by {@link #build()}
 */
// TODO <R, C, B extends...
public abstract class SelectOperationBuilder<R, B extends SelectOperationBuilder, T extends SelectOperation<R, ?>>
    extends ScalarSelectOperationBuilder<R, B, T>
{
    int fetchSize;
    
    
    protected void init(T operation) throws SormulaException
    {
        super.init(operation);
        operation.setFetchSize(fetchSize);
    }
    
    
    @SuppressWarnings("unchecked")
    public B orderByName(int fetchSize)
    {
        this.fetchSize = fetchSize;
        return (B)this;
    }
}
