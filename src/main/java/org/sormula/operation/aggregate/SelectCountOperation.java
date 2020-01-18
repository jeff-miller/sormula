/* sormula - Simple object relational mapping
 * Copyright (C) 2011-2012 Jeff Miller
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.sormula.operation.aggregate;

import org.sormula.Table;
import org.sormula.operation.OperationException;
import org.sormula.operation.aggregate.builder.SelectCountOperationBuilder;


/**
 * SQL COUNT aggregate operation. See base class for details about use.
 * <p>
 * The data type returned from database is the same type as the expression. For example,
 * if expression is a column, then the returned type is the same type as column. If
 * expression is "*", then databases may return a long instead of an int.
 * 
 * @since 1.1
 * @author Jeff Miller
 * @param <R> class type which contains members for columns of a row in a table
 * @param <T> class type of aggregate result
 */
public class SelectCountOperation<R, T> extends SelectAggregateOperation<R, T>
{
    /**
     * Creates a builder.
     * 
     * @param <R> type of row in table
     * @param <T> type of aggregate result
     * @param table select from this table
     * @param expression expression to use as parameter to function
     * @return builder
     * @since 4.4
     */
    public static <R, T> SelectCountOperationBuilder<R, T> builder(Table<R> table, String expression)
    {
        return new SelectCountOperationBuilder<R, T>(table, expression);  
    }

    
    /**
     * Constructs for standard sql select statement as:<br>
     * SELECT COUNT(e), ... FROM table<br>
     * where e is a SQL expression (typically a column name).
     * 
     * @param table select from this table
     * @param expression expression to use as parameter to function; typically it is the
     * name of a column to that aggregate function operates upon (example: COUNT(amount) amount is expression)  
     * @throws OperationException if error
     */
    public SelectCountOperation(Table<R> table, String expression) throws OperationException
    {
        super(table, "COUNT", expression);
    }
}
