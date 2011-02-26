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

import java.util.HashMap;
import java.util.Map;

import org.sormula.Table;


/**
 * {@link MapSelectOperation} that uses {@link HashMap} for {@link #readAll()} results. 
 * 
 * @since 1.0
 * @author Jeff Miller
 * @param <K> key class type for row objects
 * @param <R> Class associated with a row in table
 */
public class HashMapSelectOperation<K, R> extends MapSelectOperation<K, R>
{
	/**
	 * Constructs for a table.
	 * 
	 * @param table select from this table
	 * @throws OperationException if error
	 */
    public HashMapSelectOperation(Table<R> table) throws OperationException
    {
        super(table);
    }


    @Override
    protected Map<K, R> createReadAllCollection()
    {
        return new HashMap<K, R>(getDefaultReadAllSize());
    }
}
