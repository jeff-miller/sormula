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
 * Scalar select operation performed as prepare, execute, read, and close in one method.
 * 
 * @since 1.0
 * @author Jeff Miller
 * @param <R> class type which contains members for columns of a row in a table
 */
public class FullScalarSelect<R> 
{
    ScalarSelectOperation<R> scalarSelectOperation;
    

    /**
     * Constructs for a scalar select operation.
     * 
     * @param scalarSelectOperation perform for this select operation
     */
    public FullScalarSelect(ScalarSelectOperation<R> scalarSelectOperation)
    {
        this.scalarSelectOperation = scalarSelectOperation;
    }
    
    
    /**
     * @return scalar select operation provided in constructor
     */
    public ScalarSelectOperation<R> getSelectOperation()
    {
        return scalarSelectOperation;
    }


    /**
     * Set parameters, executes, reads rows, closes.
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
     * Set parameters, executes, reads rows, closes.
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
