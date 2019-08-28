package org.sormula.operation.builder;

import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.operation.LinkedHashMapSelectOperation;


/**
 * Builder for {@link LinkedHashMapSelectOperation} objects.
 * 
 * @author Jeff Miller
 * @since 4.4
 * @param <K> type of key in map
 * @param <R> type of row in table
 */
public class LinkedHashMapSelectOperationBuilder<K, R>
    extends MapSelectOperationBuilder<K, R, LinkedHashMapSelectOperationBuilder<K, R>, LinkedHashMapSelectOperation<K, R>>
{
    public LinkedHashMapSelectOperationBuilder(Table<R> table) 
    {
        super(table);
    }


    @Override
    public LinkedHashMapSelectOperation<K, R> build() throws SormulaException 
    {
        LinkedHashMapSelectOperation<K, R> operation = new LinkedHashMapSelectOperation<>(getTable(), "");
        init(operation);
        return operation;
    }
}
