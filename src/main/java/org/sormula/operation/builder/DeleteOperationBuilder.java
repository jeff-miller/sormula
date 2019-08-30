package org.sormula.operation.builder;

import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.operation.DeleteOperation;


/**
 * Builder for {@link DeleteOperation} objects.
 * 
 * @author Jeff Miller
 * @since 4.4
 * @param <R> type associated with a row in table
 */
public class DeleteOperationBuilder<R>
    extends ModifyOperationBuilder<R, DeleteOperationBuilder<R>, DeleteOperation<R>>
{
    public DeleteOperationBuilder(Table<R> table) 
    {
        super(table);
    }


    @Override
    public DeleteOperation<R> build() throws SormulaException 
    {
        DeleteOperation<R> operation = new DeleteOperation<>(getTable());
        init(operation);
        return operation;
    }
}
