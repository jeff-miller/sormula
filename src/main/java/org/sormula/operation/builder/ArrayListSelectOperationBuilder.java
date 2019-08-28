package org.sormula.operation.builder;

import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.operation.ArrayListSelectOperation;


/**
 * Builder for {@link ArrayListSelectOperation} objects.
 * 
 * @author Jeff Miller
 * @since 4.4
 * @param <R> type associated with a row in table
 */
public class ArrayListSelectOperationBuilder<R>
    extends ListSelectOperationBuilder<R, ArrayListSelectOperationBuilder<R>, ArrayListSelectOperation<R>>
{
    public ArrayListSelectOperationBuilder(Table<R> table) 
    {
        super(table);
    }


    @Override
    public ArrayListSelectOperation<R> build() throws SormulaException 
    {
        ArrayListSelectOperation<R> operation = new ArrayListSelectOperation<>(getTable(), "");
        init(operation);
        return operation;
    }
}
