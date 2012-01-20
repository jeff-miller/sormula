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
package org.sormula.operation;

import java.util.Collection;

import org.sormula.SormulaException;
import org.sormula.Table;


/**
 * Use {@link DeleteOperation#delete(Object)} or
 * {@link DeleteOperation#deleteAll(Collection)} or
 * {@link DeleteOperation#delete(Object...)} instead of this class.
 * 
 * {@link DeleteOperation} performed as prepare, execute, and close in one method.
 * 
 * @since 1.0
 * @author Jeff Miller
 * @param <R> class type which contains members for columns of a row in a table
 */
@Deprecated
public class FullDelete<R> extends FullModify<R>
{
    /**
     * Constructs for a delete operation. Use this constructor when delete operation 
     * is already created.
     * <p>
     * Example: 
     * <blockquote><pre>
     * DeleteOperation&lt;Student&gt; someDeleteOperation = ...
     * List&lt;Student&gt; studentList = ...
     * new FullDelete&lt;Student&gt;(someDeleteOperation).executeAll(studentList);
     * </pre></blockquote>
     * @param deleteOperation perform for this delete operation
     */
    public FullDelete(DeleteOperation<R> deleteOperation)
    {
        super(deleteOperation);
    }
    
    
    /**
     * Constructs for a {@link Table} to delete by primary key.
     * <p>
     * Example:
     * <blockquote><pre>
     * Database database = ...
     * Table&lt;Student&gt; table = database.getTable(Student.class);
     * Student student = ...
     * new FullDelete&lt;Student&gt;(table).execute(student);
     * </pre></blockquote>
     * A simpler alternative is to use {@link Table#delete(Object)} or {@link Table#deleteAll(java.util.Collection)}.
     * @param table delete from this table
     */
    public FullDelete(Table<R> table) throws SormulaException
    {
        this(new DeleteOperation<R>(table));
    }
    
    
    /**
     * Constructs for a {@link Table}. Deletes a table based upon a where condition
     * and the values in a row object. This constructor is not typically used.
     * 
     * @param table delete from this table
     * @param whereConditionName name of where condition to use; see {@link SqlOperation#setWhere(String)}
     */
    public FullDelete(Table<R> table, String whereConditionName) throws SormulaException
    {
        super(new DeleteOperation<R>(table, whereConditionName));
    }
    
    
    /**
     * Gets delect operation supplied in constructor.
     * 
     * @return operation to delete rows
     */
    public DeleteOperation<R> getDeleteOperation()
    {
        return (DeleteOperation<R>)getModifyOperation();
    }
}
