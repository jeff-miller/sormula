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

import java.util.Collection;

import org.sormula.Table;
import org.sormula.active.ActiveRecord;
import org.sormula.active.ActiveTable;


/**
 * Delegate for {@link Table#insertAllBatch(Collection)}.
 * 
 * @author Jeff Miller
 * @since 1.9 and 2.3
 * @param <R> record type
 */
public class InsertAllBatch<R extends ActiveRecord<? super R>> extends ActiveOperation<R, Integer>
{
    Collection<R> records;
    
    
    /**
     * Construct to insert a collection of records as a batch operation.
     * 
     * @param activeTable active table to affect
     * @param records records to insert
     */
    public InsertAllBatch(ActiveTable<R> activeTable, Collection<R> records)
    {
        super(activeTable, "error inserting active record collection in batch");
        this.records = records;
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public Integer operate() throws Exception
    {
        attach(records);
        return getTable().insertAllBatch(records);
    }
}
