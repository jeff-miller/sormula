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
 * Delegate for {@link Table#saveBatch(Object)}
 * 
 * @author Jeff Miller
 * @since 4.1
 * @param <R> record type
 */
public class SaveBatch<R extends ActiveRecord<? super R>> extends ActiveOperation<R, Integer>
{
    R record;
    
    
    /**
     * Construct to save a record as a batch operation.
     * 
     * @param activeTable active table to affect
     * @param record record to save
     */
    public SaveBatch(ActiveTable<R> activeTable, R record)
    {
        super(activeTable, "error saving active record in batch");
        this.record = record;
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public Integer operate() throws Exception
    {
        attach(record);
        return getTable().saveBatch(record);
    }
}
