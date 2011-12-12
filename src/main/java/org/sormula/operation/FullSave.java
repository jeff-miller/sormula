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
 * {@link SaveOperation} performed as prepare, execute, and close in one method.
 * 
 * @since 1.0
 * @author Jeff Miller
 * @param <R> class type which contains members for columns of a row in a table
 */
@Deprecated
public class FullSave<R> extends FullModify<R>
{
    /**
     * Constructs for a save operation. Use this constructor when save operation 
     * is already created.
     * <p>
     * Example: 
     * <blockquote><pre>
     * SaveOperation&lt;Student&gt; someSaveOperation = ...
     * List&lt;Student&gt; studentList = ...
     * new FullSave&lt;Student&gt;(someSaveOperation).executeAll(studentList);
     * </pre></blockquote>
     * @param saveOperation perform for this operation
     */
    public FullSave(SaveOperation<R> saveOperation)
    {
        super(saveOperation);
    }
    
    
    /**
     * Constructs for a {@link Table} to insert new rows and update exitsting rows by primary key. 
     * <p>
     * Example:
     * <blockquote><pre>
     * Database database = ...
     * Table&lt;Student&gt; table = database.getTable(Student.class);
     * Student student = ...
     * student.setGraduationDate(...);
     * new FullSave&lt;Student&gt;(table).execute(student);
     * </pre></blockquote>
     * A simpler alternative is to use {@link Table#save(Object)} or {@link Table#saveAll(java.util.Collection)}.
     * @param table save to this table
     */
    public FullSave(Table<R> table) throws SormulaException
    {
        super(new SaveOperation<R>(table));
    }
    
    
    /**
     * Constructs for a {@link Table} to insert new rows and update exitsting rows by 
     * using where condition. This constructor is not typically used.
     * 
     * @param table save to this table
     * @param whereConditionName name of where condition to use; see {@link SqlOperation#setWhere(String)}
     */
    public FullSave(Table<R> table, String whereConditionName) throws SormulaException
    {
        super(new SaveOperation<R>(table, whereConditionName));
    }
    
    
    /**
     * Gets the save operation supplied in constructor.
     * 
     * @return operation that will save rows 
     */
    public SaveOperation<R> getSaveOperation()
    {
        return (SaveOperation<R>)getModifyOperation();
    }
}
