package org.sormula.operation.builder;

import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.operation.ScalarSelectOperation;


/**
 * Base class for builders of {@link ScalarSelectOperation} objects.
 * 
 * @author Jeff Miller
 * @since 4.4
 * @param <R> type of row in table
 */
public class ScalarSelectOperationBuilder<R>
    extends AbstractScalarSelectOperationBuilder<R, ScalarSelectOperationBuilder<R>, ScalarSelectOperation<R>>
{
    public ScalarSelectOperationBuilder(Table<R> table) 
    {
        super(table);
    }
    
    
    @Override
    public ScalarSelectOperation<R> build() throws SormulaException 
    {
        ScalarSelectOperation<R> operation = new ScalarSelectOperation<>(getTable());
        init(operation);
        return operation;
    }
}
