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

import java.util.List;

import org.sormula.Table;
import org.sormula.active.ActiveRecord;
import org.sormula.active.ActiveTable;


/**
 * Delegate for {@link Table#selectAllWhere(String, Object...)}.
 * 
 * @author Jeff Miller
 * @since 1.7
 * @param <R> record type
 */
public class SelectAllWhere<R extends ActiveRecord<R>> extends ActiveOperation<R, List<R>>
{
    String whereConditionName;
    Object[] parameters;
    
    
    public SelectAllWhere(ActiveTable<R> activeTable, String whereConditionName, Object... parameters)
    {
        super(activeTable, "error selecting active record");
        this.whereConditionName = whereConditionName;
        this.parameters = parameters;
    }

    
    @Override
    public List<R> operate() throws Exception
    {
        List<R> records = getTable().selectAllWhere(whereConditionName, parameters);
        attach(records);
        return records;
    }
}
