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
package org.sormula.active.operation;

import org.sormula.Table;
import org.sormula.active.ActiveRecord;
import org.sormula.active.ActiveTable;


/**
 * Delegate for selectAvg methods in {@link Table}.
 * 
 * @author Jeff Miller
 * @since 1.7 and 2.1
 * @param <R> record type
 * @param <T> aggregate type
 */
public class SelectAvg<R extends ActiveRecord<? super R>, T> extends ActiveOperation<R, T>
{
    String expression;
    String whereConditionName;
    Object[] parameters;


    /**
     * Construct to select an average for all records.
     * 
     * @param activeTable active table to select from
     * @param expression expression to use as parameter to function; typically it is the name of a column
     */
    public SelectAvg(ActiveTable<R> activeTable, String expression)
    {
        this(activeTable, expression, null);
    }
    
    
    /**
     * Construct to select an average for subset of records.
     * 
     * @param activeTable active table to select from
     * @param expression expression to use as parameter to function; typically it is the name of a column
     * @param whereConditionName name of where condition to use; empty string to count all rows in table
     * @param parameters parameter values for where condition
     */
    public SelectAvg(ActiveTable<R> activeTable, String expression, String whereConditionName, Object... parameters)
    {
        super(activeTable, "error selecting avg");
        this.expression = expression;
        this.whereConditionName = whereConditionName;
        this.parameters = parameters;
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public T operate() throws Exception
    {
        if (whereConditionName == null) return getTable().<T>selectAvg(expression);
        return getTable().<T>selectAvg(expression, whereConditionName, parameters);
    }
}
