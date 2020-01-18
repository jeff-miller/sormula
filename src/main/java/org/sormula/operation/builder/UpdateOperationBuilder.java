package org.sormula.operation.builder;

import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.operation.UpdateOperation;


/**
 * Builder for {@link UpdateOperation} objects.
 * 
 * @author Jeff Miller
 * @since 4.4
 * @param <R> type associated with a row in table
 */
public class UpdateOperationBuilder<R>
    extends ModifyOperationBuilder<R, UpdateOperationBuilder<R>, UpdateOperation<R>>
{
    public UpdateOperationBuilder(Table<R> table) 
    {
        super(table);
    }


    @Override
    public UpdateOperation<R> build() throws SormulaException 
    {
        UpdateOperation<R> operation = new UpdateOperation<>(getTable());
        init(operation);
        return operation;
    }
}
