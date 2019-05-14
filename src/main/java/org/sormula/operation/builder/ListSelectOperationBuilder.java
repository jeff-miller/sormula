package org.sormula.operation.builder;

import org.sormula.operation.ListSelectOperation;


/**
 * Base class for builders of {@link ListSelectOperation} objects.
 * 
 * @author Jeff Miller
 * @since 4.4
 * @param <R> Class associated with a row in table
 * @param <B> Class of builder
 * @param <T> Class of object returned by {@link #build()}
 */
public abstract class ListSelectOperationBuilder<R, B extends ListSelectOperationBuilder, T extends ListSelectOperation<R>>
    extends SelectOperationBuilder<R, B, T>
{
}
