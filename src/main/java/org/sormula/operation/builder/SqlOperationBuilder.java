package org.sormula.operation.builder;

import org.sormula.SormulaException;
import org.sormula.operation.SqlOperation;


/**
 * Base class for builders of {@link SqlOperation} objects.
 * 
 * @author Jeff Miller
 * @since 4.4
 * @param <R> class associated with a row in table
 * @param <B> class of builder
 * @param <T> class of object returned by {@link #build()}
 */
public abstract class SqlOperationBuilder<R, B extends SqlOperationBuilder, T extends SqlOperation<R>>
{
    Object[] parameters;
    int queryTimeout;
    
    
    public abstract T build() throws SormulaException;
    
    
    protected void init(T operation) throws SormulaException
    {
        if (parameters != null) operation.setParameters(parameters);
        operation.setQueryTimeout(queryTimeout);
    }
    
    
    @SuppressWarnings("unchecked")
    public B parameters(Object... parameters)
    {
        this.parameters = parameters;
        return (B)this;
    }
    
    
    @SuppressWarnings("unchecked")
    public B queryTimeout(int queryTimeout)
    {
        this.queryTimeout = queryTimeout;
        return (B)this;
    }
}
