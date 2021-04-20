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
 * Delegate for {@link Table#deleteBatch(Object)}
 * 
 * @author Jeff Miller
 * @since 4.1
 * @param <R> record type
 */
public class DeleteBatch<R extends ActiveRecord<? super R>> extends ActiveOperation<R, Integer>
{
    R record;
    
    
    /**
     * Constructs to delete a row as a batch operation.
     * 
     * @param activeTable active table to affect
     * @param record delete this record
     */
    public DeleteBatch(ActiveTable<R> activeTable, R record)
    {
        super(activeTable, "error deleting active record in batch");
        this.record = record;
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public Integer operate() throws Exception
    {
        attach(record);
        return getTable().deleteBatch(record);
    }
}
