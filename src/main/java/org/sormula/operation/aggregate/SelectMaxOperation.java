package org.sormula.operation.aggregate;

import org.sormula.Table;
import org.sormula.operation.OperationException;


/**
 * SQL MAX aggregate operation. See base class for details about use.
 * 
 * @since 1.1
 * @author Jeff Miller
 * @param <R> class type which contains members for columns of a row in a table
 * @param <T> class type of aggregate result
 */
public class SelectMaxOperation<R, T> extends SelectAggregateOperation<R, T>
{
    /**
     * Constructs for standard sql select statement as:<br>
     * SELECT MAX(e), ... FROM table<br>
     * where e is a SQL expression (typically a column name).
     * 
     * @param table select from this table
     * @param expression expression to use as parameter to function; typically it is the
     * name of a column to that aggregate function operates upon (example: MAX(amount) amount is expression)  
     * @throws OperationException if error
     */
    public SelectMaxOperation(Table<R> table, String expression) throws OperationException
    {
        super(table, "MAX", expression);
    }
}
