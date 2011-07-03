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

import java.util.List;

import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.annotation.OrderBy;


/**
 * {@link FullSelect} with {@link List} as result type.
 * 
 * @since 1.0
 * @author Jeff Miller
 * @param <R> class type which contains members for columns of a row in a table
 */
public class FullListSelect<R> extends FullSelect<R, List<R>> 
{
	/**
	 * Constructs for a {@link ListSelectOperation}.
	 * 
	 * @param selectOperation select operation that returns {@link List}
	 */
    public FullListSelect(ListSelectOperation<R> selectOperation)
    {
        super(selectOperation);
    }
    
    
    /**
     * Constructs for a {@link Table} to operate on all rows.
     * 
     * @param table select from this table
     */
    public FullListSelect(Table<R> table) throws SormulaException
    {
        super(new ArrayListSelectOperation<R>(table));
    }
    
    
    /**
     * Constructs for a {@link Table} and where condition.
     * 
     * @param table select from this table
     * @param whereConditionName name of where condition to use; see {@link SqlOperation#setWhere(String)}
     */
    public FullListSelect(Table<R> table, String whereConditionName) throws SormulaException
    {
        super(new ArrayListSelectOperation<R>(table));
        getSelectOperation().setWhere(whereConditionName);
    }
    
    
    /**
     * Constructs for a {@link Table}, where condition, and order.
     * 
     * @param table select from this table
     * @param whereConditionName name of where condition to use; see {@link SqlOperation#setWhere(String)}
     * @param orderByName name of order phrase to use as defined in {@linkplain OrderBy#name()}; see
     * {@link ScalarSelectOperation#setOrderBy(String)}
     */
    public FullListSelect(Table<R> table, String whereConditionName, String orderByName) throws SormulaException
    {
        super(new ArrayListSelectOperation<R>(table));
        getSelectOperation().setWhere(whereConditionName);
        getSelectOperation().setOrderBy(orderByName);
    }
}
