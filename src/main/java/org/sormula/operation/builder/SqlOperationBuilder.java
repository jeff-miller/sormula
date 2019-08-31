package org.sormula.operation.builder;

import java.util.Map;

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
    
    
    @SuppressWarnings("unchecked")
    public B cached(boolean cached)
    {
        // TODO
        return (B)this;
    }
    
    
    @SuppressWarnings("unchecked")
    public B cascade(boolean cascade)
    {
        // TODO
        return (B)this;
    }
    
    
    @SuppressWarnings("unchecked")
    public B customSql(String customSql)
    {
        // TODO
        return (B)this;
    }
    
    
    @SuppressWarnings("unchecked")
    public B includeIdentityColumns(boolean includeIdentityColumns)
    {
        // TODO
        return (B)this;
    }
    
    
    @SuppressWarnings("unchecked")
    public B namedParameterMap(Map<String, Object> namedParameterMap)
    {
        // TODO
        return (B)this;
    }
    
    
    @SuppressWarnings("unchecked")
    public B parameter(String name, Object value)
    {
        // TODO
        return (B)this;
    }
    
    
    @SuppressWarnings("unchecked")
    public B readOnly(boolean readOnly)
    {
        // TODO
        return (B)this;
    }
    

    @SuppressWarnings("unchecked")
    public B requiredCascades(String... requiredCascades)
    {
        // TODO
        return (B)this;
    }
    

    @SuppressWarnings("unchecked")
    public B timingId(String timingId)
    {
        // TODO
        return (B)this;
    }
    
    
    @SuppressWarnings("unchecked")
    public B timings(boolean timings)
    {
        // TODO
        return (B)this;
    }
}
