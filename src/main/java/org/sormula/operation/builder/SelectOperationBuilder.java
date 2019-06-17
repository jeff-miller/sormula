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
    Integer defaultReadAllSize;
    Integer fetchSize;
    Integer resultSetType;
    String whereConditionName;
    
    
    protected void init(T operation) throws SormulaException
    {
        super.init(operation);
        
        if (defaultReadAllSize != null) operation.setDefaultReadAllSize(defaultReadAllSize);
        if (fetchSize != null) operation.setFetchSize(fetchSize);
        if (resultSetType != null) operation.setResultSetType(resultSetType);
        if (whereConditionName != null) operation.setWhere(whereConditionName);
    }
    
    
    @SuppressWarnings("unchecked")
    public B defaultReadAllSize(int defaultReadAllSize)
    {
        this.defaultReadAllSize = defaultReadAllSize;
        return (B)this;
    }
    
    
    @SuppressWarnings("unchecked")
    public B fetchSize(int fetchSize)
    {
        this.fetchSize = fetchSize;
        return (B)this;
    }
    
    
    @SuppressWarnings("unchecked")
    public B resultSetType(int resultSetType)
    {
        this.resultSetType = resultSetType;
        return (B)this;
    }
    
    
    @SuppressWarnings("unchecked")
    public B where(String whereConditionName)
    {
        this.whereConditionName = whereConditionName;
        return (B)this;
    }
}
