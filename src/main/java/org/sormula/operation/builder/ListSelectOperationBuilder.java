package org.sormula.operation.builder;

import java.util.List;

import org.sormula.operation.ListSelectOperation;


/**
 * Base class for builders of {@link ListSelectOperation} objects.
 * 
 * @author Jeff Miller
 * @since 4.4
 * @param <R> class associated with a row in table
 * @param <B> class of builder
 * @param <T> class of object returned by {@link #build()}
 */
public abstract class ListSelectOperationBuilder<R, B extends ListSelectOperationBuilder, T extends ListSelectOperation<R>>
    extends SelectOperationBuilder<R, List<R>, B, T>
{
}
