package org.sormula.operation.builder;

import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.operation.InsertOperation;


/**
 * Builder for {@link InsertOperation} objects.
 * 
 * @author Jeff Miller
 * @since 4.4
 * @param <R> type associated with a row in table
 */
public class InsertOperationBuilder<R>
    extends ModifyOperationBuilder<R, InsertOperationBuilder<R>, InsertOperation<R>>
{
    public InsertOperationBuilder(Table<R> table) 
    {
        super(table);
    }


    @Override
    public InsertOperation<R> build() throws SormulaException 
    {
        InsertOperation<R> operation = new InsertOperation<>(getTable());
        init(operation);
        return operation;
    }
}
