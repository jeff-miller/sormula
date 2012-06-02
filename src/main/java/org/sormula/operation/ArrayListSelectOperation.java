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

import java.util.ArrayList;

import org.sormula.Table;


/**
 * {@link ListSelectOperation} that uses {@link ArrayList} for {@link #readAll()} results.
 * 
 * @since 1.0
 * @author Jeff Miller
 * @param <R> Class associated with a row in table
 */
public class ArrayListSelectOperation<R> extends ListSelectOperation<R>
{
	/**
	 * Constructs for a table.
	 * 
	 * @param table select from this table
	 * @throws OperationException if error
	 */
    public ArrayListSelectOperation(Table<R> table) throws OperationException
    {
        super(table);
    }
    
    
    /**
     * Constructs for a table and where condition.
     * 
     * @param table select from this table
     * @param whereConditionName name of where condition to use ("primaryKey" to select
     * by primary key; empty string to select all rows in table)
     * @throws OperationException if error
     */
    public ArrayListSelectOperation(Table<R> table, String whereConditionName) throws OperationException
    {
        super(table, whereConditionName);
    }
    
    
    /**
     * Creates an {@link ArrayList} to contain selected rows.
     * 
     * @return new {@link ArrayList} 
     */
    @Override
    protected ArrayList<R> createReadAllCollection()
    {
        return new ArrayList<R>(getDefaultReadAllSize());
    }
}
