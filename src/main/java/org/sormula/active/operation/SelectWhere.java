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
 * Delegate for {@link Table#selectWhere(String, Object...)}.
 * 
 * @author Jeff Miller
 * @since 1.7 and 2.1
 * @param <R> record type
 */
public class SelectWhere<R extends ActiveRecord<? super R>> extends ActiveOperation<R, R>
{
    String whereConditionName;
    Object[] parameters;
    
    
    /**
     * Construct to select for a where condition.
     * 
     * @param activeTable active table to select from 
     * @param whereConditionName name of where condition to use
     * @param parameters parameter values for where condition
     */
    public SelectWhere(ActiveTable<R> activeTable, String whereConditionName, Object... parameters)
    {
        super(activeTable, "error selecting active record");
        this.whereConditionName = whereConditionName;
        this.parameters = parameters;
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public R operate() throws Exception
    {
        R record = getTable().selectWhere(whereConditionName, parameters);
        if (record != null) attach(record);
        return record;
    }
}
