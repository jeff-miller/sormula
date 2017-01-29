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
package org.sormula.operation;

import org.sormula.Database;
import org.sormula.Table;
import org.sormula.annotation.Row;


/**
 * Indicates an insert, update, or delete was attempted for a table that is
 * read only.
 * 
 * @since 4.2
 * @author Jeff Miller
 * @see ModifyOperation#execute()
 * @see Database#isReadOnly()
 * @see Table#isReadOnly()
 * @see SqlOperation#isReadOnly()
 * @see Row#readOnly()
 */
public class ReadOnlyException extends OperationException
{
    private static final long serialVersionUID = 1L;

    public ReadOnlyException(String message)
    {
        super(message);
    }
}
