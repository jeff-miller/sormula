package org.sormula.operation.builder;

import java.util.Collection;
import java.util.Map;

import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.operation.ModifyOperation;


/**
 * Base class for builders of {@link ModifyOperation} objects.
 * 
 * @author Jeff Miller
 * @since 4.4
 * @param <R> type of row in table
 * @param <B> type of builder
 * @param <T> type returned by {@link #build()}
 */
public abstract class ModifyOperationBuilder<R, B extends ModifyOperationBuilder, T extends ModifyOperation<R>>
    extends SqlOperationBuilder<R, B, T>
{
    Boolean batch;
    Object[] parameters;
    R row;
    Collection<R> rowCollection;
    R[] rowArray;
    Map<?, R> rowMap;
    
    
    public ModifyOperationBuilder(Table<R> table) 
    {
        super(table);
    }


    protected void init(T operation) throws SormulaException
    {
        if (batch != null) operation.setBatch(batch);
        
        if (parameters != null) operation.setParameters(parameters);
        else if (row != null) operation.setRow(row);
        else if (rowCollection != null) operation.setRows(rowCollection);
        else if (rowArray != null) operation.setRows(rowArray);
        else if (rowMap != null) operation.setRows(rowMap);
    }
    
    
    @SuppressWarnings("unchecked")
    public B batch(boolean batch)
    {
        this.batch = batch;
        return (B)this;
    }
    
    
    @SuppressWarnings("unchecked")
    public B parameters(Object... parameters)
    {
        this.parameters = parameters;
        return (B)this;
    }
    
    
    @SuppressWarnings("unchecked")
    public B row(R row)
    {
        this.row = row;
        return (B)this;
    }
    
    
    @SuppressWarnings("unchecked")
    public B rows(Collection<R> rows)
    {
        rowCollection = rows;
        return (B)this;
    }
    
    
    @SuppressWarnings("unchecked")
    public B rows(Map<?, R> rows)
    {
        rowMap = rows;
        return (B)this;
    }
    
    
    @SuppressWarnings("unchecked")
    public B rows(R[] rows)
    {
        rowArray = rows;
        return (B)this;
    }
}
