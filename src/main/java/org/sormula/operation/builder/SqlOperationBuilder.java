package org.sormula.operation.builder;

import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.operation.SqlOperation;


/**
 * Base class for builders of {@link SqlOperation} objects.
 * 
 * @author Jeff Miller
 * @since 4.4
 * @param <R> type of row in table
 * @param <B> type of builder
 * @param <T> type returned by {@link #build()}
 */
public abstract class SqlOperationBuilder<R, B extends SqlOperationBuilder, T extends SqlOperation<R>>
{
    Table<R> table;
    String whereConditionName;
    Object[] parameters;
    Integer queryTimeout;
    
    
    public SqlOperationBuilder(Table<R> table) 
    {
        this.table = table;
    }


    public abstract T build() throws SormulaException;
    
    
    public Table<R> getTable() 
    {
        return table;
    }


    protected void init(T operation) throws SormulaException
    {
        // setWhere prior to super.init to allow maximumRowsRead from builder to override 
        // maximumRowsRead from where annotation
        if (whereConditionName != null) operation.setWhere(whereConditionName);
        
        if (parameters != null) operation.setParameters(parameters);
        if (queryTimeout != null) operation.setQueryTimeout(queryTimeout);
    }
    
    
    @SuppressWarnings("unchecked")
    public B where(String whereConditionName)
    {
        this.whereConditionName = whereConditionName;
        return (B)this;
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
