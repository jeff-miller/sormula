package org.sormula.operation.aggregate.builder;

import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.operation.aggregate.SelectCountOperation;


/**
 * Builder for {@link SelectCountOperation} objects.
 * 
 * @author Jeff Miller
 * @since 4.4
 * @param <R> type of row in table
 * @param <T> type of aggregate result
 */
public class SelectCountOperationBuilder<R, T>
    extends SelectAggregateOperationBuilder<R, T, SelectCountOperationBuilder<R, T>, SelectCountOperation<R, T>>
{
    public SelectCountOperationBuilder(Table<R> table, String expression) 
    {
        super(table, expression);
    }


    @Override
    public SelectCountOperation<R, T> build() throws SormulaException 
    {
        SelectCountOperation<R, T> operation = new SelectCountOperation<>(getTable(), getExpression());
        init(operation);
        return operation;
    }
}
