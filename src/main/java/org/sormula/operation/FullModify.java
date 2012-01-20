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


/**
 * Use {@link ModifyOperation#modify(Object)} or
 * {@link ModifyOperation#modifyAll(Collection)} or
 * {@link ModifyOperation#modify(Object...)} instead of this class.
 * 
 * {@link ModifyOperation} performed as prepare, execute, and close in one method. Base
 * class for {@link FullInsert}, {@link FullUpdate}, and {@link FullDelete}.
 * 
 * @since 1.0
 * @author Jeff Miller
 * @param <R> class type which contains members for columns of a row in a table
 */
@Deprecated
public class FullModify<R> 
{
    ModifyOperation<R> modifyOperation;
    

    /**
     * Constructs for a modify operation.
     * 
     * @param modifyOperation perform for this modify operation
     */
    public FullModify(ModifyOperation<R> modifyOperation)
    {
        this.modifyOperation = modifyOperation;
    }
    
    
    /** 
     * Gets modify operation supplied in constructor.
     * 
     * @return operation that will modify rows
     */
    public ModifyOperation<R> getModifyOperation()
    {
        return modifyOperation;
    }


    /**
     * Modifies one row. Set parameters, executes, closes.
     * 
     * @param row row to use for parameters
     * @return {@link ModifyOperation#getRowsAffected()}
     * @throws OperationException if error
     */
    public int execute(R row) throws OperationException
    {
        modifyOperation.setRow(row);
        modifyOperation.execute();
        modifyOperation.close();
        return modifyOperation.getRowsAffected();
    }
    
    
    /**
     * Modifies a collection of rows. Set parameters, executes, closes.
     * 
     * @param rows collection of rows to use as parameters 
     * @return {@link ModifyOperation#getRowsAffected()}
     * @throws OperationException if error
     */
    public int executeAll(Collection<R> rows) throws OperationException
    {
        modifyOperation.setRows(rows);
        modifyOperation.execute();
        modifyOperation.close();
        return modifyOperation.getRowsAffected();
    }


    /**
     * Modifies row(s) with sql parametes as Objects
     * 
     * @param parameters operation parameters as objects (see {@link ModifyOperation#setParameters(Object...)})
     * @return count of rows affected
     * @throws OperationException if error
     */
    public int executeObject(Object... parameters) throws OperationException
    {
        modifyOperation.setParameters(parameters);
        modifyOperation.execute();
        modifyOperation.close();
        return modifyOperation.getRowsAffected();
    }
}
