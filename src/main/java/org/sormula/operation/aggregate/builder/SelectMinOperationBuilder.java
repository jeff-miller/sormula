package org.sormula.operation.aggregate.builder;

import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.operation.aggregate.SelectMinOperation;


/**
 * Builder for {@link SelectMinOperation} objects.
 * 
 * @author Jeff Miller
 * @since 4.4
 * @param <R> type of row in table
 * @param <T> type of aggregate result
 */
public class SelectMinOperationBuilder<R, T>
    extends SelectAggregateOperationBuilder<R, T, SelectMinOperationBuilder<R, T>, SelectMinOperation<R, T>>
{
    public SelectMinOperationBuilder(Table<R> table, String expression) 
    {
        super(table, expression);
    }


    @Override
    public SelectMinOperation<R, T> build() throws SormulaException 
    {
        SelectMinOperation<R, T> operation = new SelectMinOperation<>(getTable(), getExpression());
        init(operation);
        return operation;
    }
}
