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


/**
 * Use {@link SelectOperation#fullExecuteAll(Object)} or
 * {@link SelectOperation#fullExecute(Object...)} or
 * {@link ScalarSelectOperation#fullExecute(Object)} or
 * {@link ScalarSelectOperation#fullExecute(Object...)} instead of this class.
 * 
 * {@link SelectOperation} performed as prepare, execute, read, and close in one method.
 * 
 * @since 1.0
 * @author Jeff Miller
 * @param <R> class type which contains members for columns of a row in a table
 * @param <C> collection type returned
 */
@Deprecated
public class FullSelect<R, C> 
{
    SelectOperation<R, C> selectOperation;
    

    /**
     * Constructs for a select operation.
     * 
     * @param selectOperation perform for this select operation
     */
    public FullSelect(SelectOperation<R, C> selectOperation)
    {
        this.selectOperation = selectOperation;
    }
    
    
    /**
     * Gets the select operation supplied in the constructor.
     * 
     * @return operation that will select rows
     */
    public SelectOperation<R, C> getSelectOperation()
    {
        return selectOperation;
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
        selectOperation.setParameters(parameters);
        selectOperation.execute();
        R row = selectOperation.readNext();
        selectOperation.close();
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
        selectOperation.setRowParameters(whereParameters);
        selectOperation.execute();
        R row = selectOperation.readNext();
        selectOperation.close();
        return row;
    }
    
    
    /**
     * Set parameters, executes, reads all rows in result set, closes.
     * 
     * @param parameters query parameters as objects (see {@linkplain SelectOperation#setParameters(Object...)})
     * @return {@linkplain SelectOperation#readAll()}
     * @throws OperationException if error
     */
    public C executeAll(Object... parameters) throws OperationException
    {
        selectOperation.setParameters(parameters);
        selectOperation.execute();
        C results = selectOperation.readAll(); 
        selectOperation.close();
        return results;
    }
    
    
    /**
     * Set parameters, executes, reads all rows in result set, closes.
     * 
     * @param whereParameters query parameters are read from an existing row object 
     * (see {@linkplain SelectOperation#setParameters(Object...)})
     * @return {@linkplain SelectOperation#readAll()}
     * @throws OperationException if error
     */
    public C executeAll(R whereParameters) throws OperationException
    {
        selectOperation.setRowParameters(whereParameters);
        selectOperation.execute();
        C results = selectOperation.readAll(); 
        selectOperation.close();
        return results;
    }
}
