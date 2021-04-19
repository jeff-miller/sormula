/* sormula - Simple object relational mapping
 * Copyright (C) 2011-2020 Jeff Miller
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
package org.sormula.operation.builder;

import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.operation.SaveOperation;


/**
 * Builder for {@link SaveOperation} objects.
 * 
 * @author Jeff Miller
 * @since 4.4
 * @param <R> type associated with a row in table
 */
public class SaveOperationBuilder<R>
    extends ModifyOperationBuilder<R, SaveOperationBuilder<R>, SaveOperation<R>>
{
    /**
     * Constructs for a table.
     * 
     * @param table build for table
     */
    public SaveOperationBuilder(Table<R> table) 
    {
        super(table);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public SaveOperation<R> build() throws SormulaException 
    {
        SaveOperation<R> operation = new SaveOperation<>(getTable());
        init(operation);
        return operation;
    }
}
