package org.sormula.operation.builder;

import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.operation.HashMapSelectOperation;


/**
 * Builder for {@link HashMapSelectOperation} objects.
 * 
 * @author Jeff Miller
 * @since 4.4
 * @param <K> type of key in map
 * @param <R> type of row in table
 * @param <B> type of builder
 * @param <T> type of object returned by {@link #build()}
 */
public class HashMapSelectOperationBuilder<K, R>
    extends MapSelectOperationBuilder<K, R, HashMapSelectOperationBuilder<K, R>, HashMapSelectOperation<K, R>>
{
    public HashMapSelectOperationBuilder(Table<R> table) 
    {
        super(table);
    }


    @Override
    public HashMapSelectOperation<K, R> build() throws SormulaException 
    {
        HashMapSelectOperation<K, R> operation = new HashMapSelectOperation<>(getTable(), "");
        init(operation);
        return operation;
    }
}
