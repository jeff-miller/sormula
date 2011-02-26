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
 * {@link InsertOperation} performed as prepare, execute, and close in one method.
 * 
 * @since 1.0
 * @author Jeff Miller
 * @param <R> class type which contains members for columns of a row in a table
 */
public class FullInsert<R> extends FullModify<R>
{
    /**
     * Constructs for a insert operation.
     * 
     * @param insertOperation perform for this insert operation
     */
    public FullInsert(InsertOperation<R> insertOperation)
    {
        super(insertOperation);
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
