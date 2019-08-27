package org.sormula.operation.builder;

import java.util.Map;
import java.util.function.Function;

import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.operation.MapSelectOperation;


/**
 * Base class for builders of {@link MapSelectOperation} objects.
 * 
 * @author Jeff Miller
 * @since 4.4
 * @param <K> type of key in map
 * @param <R> type of row in table
 * @param <B> type of builder
 * @param <T> type returned by {@link #build()}
 */
public abstract class MapSelectOperationBuilder<K, R, B extends MapSelectOperationBuilder, T extends MapSelectOperation<K, R>>
    extends SelectOperationBuilder<R, Map<K, R>, B, T>
{
    String getKeyMethodName;
    Function<R, K> keyFunction;
    
    
    public MapSelectOperationBuilder(Table<R> table) 
    {
        super(table);
    }
    
    
    @Override
    protected void init(T operation) throws SormulaException
    {
        super.init(operation);
        if (getKeyMethodName != null) operation.setGetKeyMethodName(getKeyMethodName);
        if (keyFunction != null) operation.setKeyFunction(keyFunction);
    }
    
    
    @SuppressWarnings("unchecked")
    public B getKeyMethodName(String getKeyMethodName)
    {
        this.getKeyMethodName = getKeyMethodName;
        return (B)this;
    }
    
    
    @SuppressWarnings("unchecked")
    public B keyFunction(Function<R, K> keyFunction)
    {
        this.keyFunction = keyFunction;
        return (B)this;
    }
}
