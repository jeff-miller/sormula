/* sormula - Simple object relational mapping
 * Copyright (C) 2011 Jeff Miller
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


/**
 * SQL COUNT aggregate operation. See base class for details about use.
 * 
 * @since 1.1
 * @author Jeff Miller
 * @param <R> class type which contains members for columns of a row in a table
 * @param <T> class type of aggregate result
 */
public class SelectCountOperation<R, T> extends SelectAggregateOperation<R, T>
{
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
