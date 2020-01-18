package org.sormula.operation.aggregate.builder;

import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.operation.aggregate.SelectSumOperation;


/**
 * Builder for {@link SelectSumOperation} objects.
 * 
 * @author Jeff Miller
 * @since 4.4
 * @param <R> type of row in table
 * @param <T> type of aggregate result
 */
public class SelectSumOperationBuilder<R, T>
    extends SelectAggregateOperationBuilder<R, T, SelectSumOperationBuilder<R, T>, SelectSumOperation<R, T>>
{
    public SelectSumOperationBuilder(Table<R> table, String expression) 
    {
        super(table, expression);
    }


    @Override
    public SelectSumOperation<R, T> build() throws SormulaException 
    {
        SelectSumOperation<R, T> operation = new SelectSumOperation<>(getTable(), getExpression());
        init(operation);
        return operation;
    }
}
