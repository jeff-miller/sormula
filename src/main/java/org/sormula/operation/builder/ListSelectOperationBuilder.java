package org.sormula.operation.builder;

import java.util.List;

import org.sormula.Table;
import org.sormula.operation.ListSelectOperation;


/**
 * Base class for builders of {@link ListSelectOperation} objects.
 * 
 * @author Jeff Miller
 * @since 4.4
 * @param <R> type of row in table
 * @param <B> type of builder
 * @param <T> type returned by {@link #build()}
 */
public abstract class ListSelectOperationBuilder<R, B extends ListSelectOperationBuilder, T extends ListSelectOperation<R>>
    extends SelectOperationBuilder<R, List<R>, B, T>
{
    public ListSelectOperationBuilder(Table<R> table) 
    {
        super(table);
    }
}
