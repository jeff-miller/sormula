package org.sormula.operation.aggregate.builder;

import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.operation.HashMapSelectOperation;
import org.sormula.operation.aggregate.SelectAvgOperation;


/**
 * Builder for {@link HashMapSelectOperation} objects.
 * 
 * @author Jeff Miller
 * @since 4.4
 * @param <R> type of row in table
 * @param <T> class type of aggregate result
 */
public class SelectAvgOperationBuilder<R, T>
    extends SelectAggregateOperationBuilder<R, T, SelectAvgOperationBuilder<R, T>, SelectAvgOperation<R, T>>
{
    public SelectAvgOperationBuilder(Table<R> table, String expression) 
    {
        super(table, expression);
    }


    @Override
    public SelectAvgOperation<R, T> build() throws SormulaException 
    {
        SelectAvgOperation<R, T> operation = new SelectAvgOperation<>(getTable(), getExpression());
        init(operation);
        return operation;
    }
}
