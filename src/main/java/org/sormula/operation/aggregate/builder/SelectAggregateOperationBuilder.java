package org.sormula.operation.aggregate.builder;

import org.sormula.Table;
import org.sormula.operation.aggregate.SelectAggregateOperation;
import org.sormula.operation.builder.AbstractScalarSelectOperationBuilder;


/**
 * Base class for builders of {@link SelectAggregateOperation} objects.
 * 
 * @author Jeff Miller
 * @since 4.4
 * @param <R> type of row in table
 * @param <A> type of aggregate result
 * @param <B> type of builder
 * @param <T> type returned by {@link #build()}
 */
public abstract class SelectAggregateOperationBuilder<R, A, B extends SelectAggregateOperationBuilder, T extends SelectAggregateOperation<R, A>>
    extends AbstractScalarSelectOperationBuilder<R, B, T>
{
    String expression;
    
    
    /**
     * @param table build for table
     * @param expression expression to use for aggregate function
     */
    public SelectAggregateOperationBuilder(Table<R> table, String expression) 
    {
        super(table);
        this.expression = expression;
    }


    /**
     * @return expression supplied in the constructor
     */
    public String getExpression() 
    {
        return expression;
    }
}
