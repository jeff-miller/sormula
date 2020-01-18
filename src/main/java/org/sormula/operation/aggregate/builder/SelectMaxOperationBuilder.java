package org.sormula.operation.aggregate.builder;

import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.operation.aggregate.SelectMaxOperation;


/**
 * Builder for {@link SelectMaxOperation} objects.
 * 
 * @author Jeff Miller
 * @since 4.4
 * @param <R> type of row in table
 * @param <T> type of aggregate result
 */
public class SelectMaxOperationBuilder<R, T>
    extends SelectAggregateOperationBuilder<R, T, SelectMaxOperationBuilder<R, T>, SelectMaxOperation<R, T>>
{
    public SelectMaxOperationBuilder(Table<R> table, String expression) 
    {
        super(table, expression);
    }


    @Override
    public SelectMaxOperation<R, T> build() throws SormulaException 
    {
        SelectMaxOperation<R, T> operation = new SelectMaxOperation<>(getTable(), getExpression());
        init(operation);
        return operation;
    }
}
