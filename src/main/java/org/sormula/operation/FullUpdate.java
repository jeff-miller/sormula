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
 * {@link UpdateOperation} performed as prepare, execute, and close in one method.
 * 
 * @since 1.0
 * @author Jeff Miller
 * @param <R> class type which contains members for columns of a row in a table
 */
public class FullUpdate<R> extends FullModify<R>
{
    /**
     * Constructs for a update operation.
     * 
     * @param updateOperation perform for this update operation
     */
    public FullUpdate(UpdateOperation<R> updateOperation)
    {
        super(updateOperation);
    }
    
    
    /**
     * Constructs for a {@link Table} to update by primary key.
     * 
     * @param table update this table
     */
    public FullUpdate(Table<R> table) throws SormulaException
    {
        super(table.createUpdateOperation());
    }
    
    
    /**
     * Constructs for a {@link Table}.
     * 
     * @param table update this table
     * @param whereConditionName name of where condition to use; see {@link SqlOperation#setWhere(String)}
     */
    public FullUpdate(Table<R> table, String whereConditionName) throws SormulaException
    {
        super(table.createUpdateOperation(whereConditionName));
    }
    
    
    /**
     * Gets the update operation supplied in constructor.
     * 
     * @return operation that will update rows 
     */
    public UpdateOperation<R> getUpdateOperation()
    {
        return (UpdateOperation<R>)getModifyOperation();
    }
}
