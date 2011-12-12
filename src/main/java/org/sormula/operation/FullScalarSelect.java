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

import org.sormula.SormulaException;
import org.sormula.Table;


/**
 * Use {@link ScalarSelectOperation#fullExecute(Object)} or
 * {@link ScalarSelectOperation#fullExecute(Object...)} instead of this class.
 * 
 * {@link ScalarSelectOperation} performed as prepare, execute, read, and close in one method.
 * 
 * @since 1.0
 * @author Jeff Miller
 * @param <R> class type which contains members for columns of a row in a table
 */
@Deprecated
public class FullScalarSelect<R> 
{
    ScalarSelectOperation<R> scalarSelectOperation;
    

    /**
     * Constructs for a scalar select operation. Use this constructor if
     * select operation is already created.
     * <p>
     * Example: Select a student 1234:
     * <blockquote><pre>
     * ScalarSelectOperation&lt;Student&gt; someScalarSelectOperation = ... // operation contains where condition for student id
     * Student student = new FullScalarSelect&lt;Student&gt;(someScalarSelectOperation).execute(1234);
     * </pre></blockquote>
     * @param scalarSelectOperation perform for this select operation
     */
    public FullScalarSelect(ScalarSelectOperation<R> scalarSelectOperation)
    {
        this.scalarSelectOperation = scalarSelectOperation;
    }
    
    
    /**
     * Constructs for a {@link Table} to select by primary key.
     * <p>
     * Example: Select a student 1234 (primary key):
     * <blockquote><pre>
     * Database database = ...
     * Table&lt;Student&gt; table = database.getTable(Student.class);
     * Student student = new FullScalarSelect&lt;Student&gt;(table).execute(1234);
     * </pre></blockquote>
     * @param table select from this table
     */
    public FullScalarSelect(Table<R> table) throws SormulaException
    {
        this(new ScalarSelectOperation<R>(table));
    }
    
    
    /**
     * Constructs for a {@link Table} to select by a where condition.
     * <p>
     * Example: Select a student by last name ("byLastName" is name of Where annotation on Student):
     * <blockquote><pre>
     * Database database = ...
     * Table&lt;Student&gt; table = database.getTable(Student.class);
     * Student student = new FullScalarSelect&lt;Student&gt;(table, "byLastName").execute("miller");
     * </pre></blockquote>
     * @param table select from this table
     * @param whereConditionName name of where condition to use; see {@link SqlOperation#setWhere(String)}
     */
    public FullScalarSelect(Table<R> table, String whereConditionName) throws SormulaException
    {
        this(new ScalarSelectOperation<R>(table, whereConditionName));
    }
    
    
    /**
     * Gets select operation supplied in constructor.
     * 
     * @return operation that will select row
     */
    public ScalarSelectOperation<R> getSelectOperation()
    {
        return scalarSelectOperation;
    }


    /**
     * Set parameters, executes, reads one row, closes.
     * 
     * @param parameters query parameters as objects (see {@linkplain SelectOperation#setParameters(Object...)})
     * @return {@linkplain SelectOperation#readNext()}
     * @throws OperationException if error
     */
    public R execute(Object... parameters) throws OperationException
    {
        scalarSelectOperation.setParameters(parameters);
        scalarSelectOperation.execute();
        R row = scalarSelectOperation.readNext();
        scalarSelectOperation.close();
        return row;
    }
    
    
    /**
     * Set parameters, executes, reads one row, closes.
     * 
     * @param whereParameters query parameters are read from an existing row object 
     * (see {@linkplain SelectOperation#setRowParameters(Object)})
     * @return {@linkplain SelectOperation#readNext()}
     * @throws OperationException if error
     */
    public R execute(R whereParameters) throws OperationException
    {
        scalarSelectOperation.setRowParameters(whereParameters);
        scalarSelectOperation.execute();
        R row = scalarSelectOperation.readNext();
        scalarSelectOperation.close();
        return row;
    }
}
