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
package org.sormula.operation;

import java.util.List;

import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.annotation.OrderBy;


/**
 * Use {@link SelectOperation#selectAll(Object)} or
 * {@link SelectOperation#select(Object...)} instead of this class.
 * 
 * {@link FullSelect} with {@link List} as result type.
 * 
 * @since 1.0
 * @author Jeff Miller
 * @param <R> class type which contains members for columns of a row in a table
 */
@Deprecated
public class FullListSelect<R> extends FullSelect<R, List<R>> 
{
	/**
	 * Constructs for a {@link ListSelectOperation}. Use this constructor if
	 * select operation is already created.
     * <p>
     * Example: Select some students:
     * <blockquote><pre>
     * ListSelectOperation&lt;Student&gt; someListSelectOperation = ...
     * List&lt;Student&gt; selectedList = new FullListSelect&lt;Student&gt;(someListSelectOperation).executeAll();
     * </pre></blockquote>
	 * @param selectOperation select operation that returns {@link List}
	 */
    public FullListSelect(ListSelectOperation<R> selectOperation)
    {
        super(selectOperation);
    }
    
    
    /**
     * Constructs for a {@link Table} to select all rows in table.
     * <p>
     * Example: Select all students:
     * <blockquote><pre>
     * Database database = ...
     * Table&lt;Student&gt; table = database.getTable(Student.class);
     * List&lt;Student&gt; selectedList = new FullListSelect&lt;Student&gt;(table).executeAll();
     * </pre></blockquote>
     * @param table select from this table
     */
    public FullListSelect(Table<R> table) throws SormulaException
    {
        super(new ArrayListSelectOperation<R>(table, ""));
    }
    
    
    /**
     * Constructs for a {@link Table} to select by a where condition.
     * <p>
     * Example: Select all students by type 3 ("byType" is name of Where annotation on Student):
     * <blockquote><pre>
     * Database database = ...
     * Table&lt;Student&gt; table = database.getTable(Student.class);
     * List&lt;Student&gt; selectedList = new FullListSelect&lt;Student&gt;(table, "byType").executeAll(3);
     * </pre></blockquote>
     * @param table select from this table
     * @param whereConditionName name of where condition to use; see {@link SqlOperation#setWhere(String)}
     */
    public FullListSelect(Table<R> table, String whereConditionName) throws SormulaException
    {
        super(new ArrayListSelectOperation<R>(table, whereConditionName));
    }
    
    
    /**
     * Constructs for a {@link Table} to select by a where condition, and order by order condition.
     * <p>
     * Example: Select all students by type 3 ("byType" is name of Where annotation on Student, "byName"
     * is name of OrderBy annotation on Student):
     * <blockquote><pre>
     * Database database = ...
     * Table&lt;Student&gt; table = database.getTable(Student.class);
     * List&lt;Student&gt; selectedList = new FullListSelect&lt;Student&gt;(table, "byType", "byName").executeAll(3);
     * </pre></blockquote>
     * @param table select from this table
     * @param whereConditionName name of where condition to use; see {@link SqlOperation#setWhere(String)}
     * @param orderByName name of order phrase to use as defined in {@linkplain OrderBy#name()}; see
     * {@link ScalarSelectOperation#setOrderBy(String)}
     */
    public FullListSelect(Table<R> table, String whereConditionName, String orderByName) throws SormulaException
    {
        super(new ArrayListSelectOperation<R>(table, whereConditionName));
        getSelectOperation().setOrderBy(orderByName);
    }
}
