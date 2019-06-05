package org.sormula.operation.builder;

import org.sormula.SormulaException;
import org.sormula.operation.SelectOperation;


/**
 * Base class for builders of {@link SelectOperation} objects.
 * 
 * @author Jeff Miller
 * @since 4.4
 * @param <R> class associated with a row in table
 * @param <C> collection type returned 
 * @param <B> class of builder
 * @param <T> class of object returned by {@link #build()}
 */
public abstract class SelectOperationBuilder<R, C, B extends SelectOperationBuilder, T extends SelectOperation<R, C>>
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
