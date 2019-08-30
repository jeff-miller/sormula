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
    R row;
    Collection<R> rowCollection;
    
    
    public ModifyOperationBuilder(Table<R> table) 
    {
        super(table);
    }


    protected void init(T operation) throws SormulaException
    {
        if (row != null) operation.setRow(row);
        else if (rowCollection != null) operation.setRows(rowCollection);
    }
    
    
    @SuppressWarnings("unchecked")
    public B batch(boolean batch)
    {
        // TODO
        return (B)this;
    }
    
    
    @SuppressWarnings("unchecked")
    public B parameters(Object... parameters)
    {
        // TODO
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
        // TODO
        return (B)this;
    }
    
    
    @SuppressWarnings("unchecked")
    public B rows(R[] rows)
    {
        // TODO
        return (B)this;
    }
}
