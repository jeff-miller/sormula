package org.sormula.operation.builder;

import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.operation.SaveOperation;


/**
 * Builder for {@link SaveOperation} objects.
 * 
 * @author Jeff Miller
 * @since 4.4
 * @param <R> type associated with a row in table
 */
public class SaveOperationBuilder<R>
    extends ModifyOperationBuilder<R, SaveOperationBuilder<R>, SaveOperation<R>>
{
    public SaveOperationBuilder(Table<R> table) 
    {
        super(table);
    }


    @Override
    public SaveOperation<R> build() throws SormulaException 
    {
        SaveOperation<R> operation = new SaveOperation<>(getTable());
        init(operation);
        return operation;
    }
}
