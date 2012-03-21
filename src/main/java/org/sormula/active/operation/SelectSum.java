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
 * Delegate for selectSum methods in {@link Table}.
 * 
 * @author Jeff Miller
 * @since 1.7
 */
public class SelectSum<R extends ActiveRecord, T> extends ActiveOperation<R, T>
{
    String expression;
    String whereConditionName;
    Object[] parameters;

    
    public SelectSum(ActiveTable<R> activeTable, String expression)
    {
        this(activeTable, expression, null);
    }
    
    
    public SelectSum(ActiveTable<R> activeTable, String expression, String whereConditionName, Object... parameters)
    {
        super(activeTable, "error selecting sum");
        this.expression = expression;
        this.whereConditionName = whereConditionName;
        this.parameters = parameters;
    }

    
    @Override
    public T operate() throws Exception
    {
        if (whereConditionName == null) return table.<T>selectSum(expression);
        return table.<T>selectSum(expression, whereConditionName, parameters);
    }
}
