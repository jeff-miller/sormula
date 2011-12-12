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

import java.util.Collection;

import org.sormula.SormulaException;
import org.sormula.Table;


/**
 * Use {@link ModifyOperation#fullExecute(Object)} or
 * {@link ModifyOperation#fullExecuteAll(Collection)} or
 * {@link ModifyOperation#fullExecuteObject(Object...)} instead of this class.
 * 
 * {@link InsertOperation} performed as prepare, execute, and close in one method.
 * 
 * @since 1.0
 * @author Jeff Miller
 * @param <R> class type which contains members for columns of a row in a table
 */
@Deprecated
public class FullInsert<R> extends FullModify<R>
{
    /**
     * Constructs for a insert operation. Use this constructor when insert operation 
     * is already created.
     * <p>
     * Example: 
     * <blockquote><pre>
     * InsertOperation&lt;Student&gt; someInsertOperation = ...
     * List&lt;Student&gt; studentList = ...
     * new FullInsert&lt;Student&gt;(someInsertOperation).executeAll(studentList);
     * </pre></blockquote>
     * @param insertOperation perform for this insert operation
     */
    public FullInsert(InsertOperation<R> insertOperation)
    {
        super(insertOperation);
    }
    
    
    /**
     * Constructs for a {@link Table} to insert into table.
     * <p>
     * Example:
     * <blockquote><pre>
     * Database database = ...
     * Table&lt;Student&gt; table = database.getTable(Student.class);
     * Student student = ...
     * new FullInsert&lt;Student&gt;(table).execute(student);
     * </pre></blockquote>
     * A simpler alternative is to use {@link Table#insert(Object)} or {@link Table#insertAll(java.util.Collection)}.
     * @param table insert into this table
     */
    public FullInsert(Table<R> table) throws SormulaException
    {
        super(new InsertOperation<R>(table));
    }
    
    
    /**
     * Gets insert operation supplied in constructor.
     * 
     * @return operation to insert rows
     */
    public InsertOperation<R> getInsertOperation()
    {
        return (InsertOperation<R>)getModifyOperation();
    }
}
