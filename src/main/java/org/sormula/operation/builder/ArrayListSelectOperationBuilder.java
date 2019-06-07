package org.sormula.operation.builder;

import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.operation.ArrayListSelectOperation;


/**
 * Builder for {@link ArrayListSelectOperation} objects.
 * 
 * @author Jeff Miller
 * @since 4.4
 * @param <R> type associated with a row in table
 * @param <B> type of builder
 * @param <T> type of object returned by {@link #build()}
 */
public class ArrayListSelectOperationBuilder<R>
    extends ListSelectOperationBuilder<R, ArrayListSelectOperationBuilder<R>, ArrayListSelectOperation<R>>
{
    Table<R> table;
    String whereConditionName;
    
    
    public ArrayListSelectOperationBuilder(Table<R> table, String whereConditionName) 
    {
        this.table = table;
        this.whereConditionName = whereConditionName;
    }


    @Override
    public ArrayListSelectOperation<R> build() throws SormulaException 
    {
        ArrayListSelectOperation<R> operation = new ArrayListSelectOperation<>(table, whereConditionName);
        init(operation);
        return operation;
    }
}
