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


/**
 * Used for one of several error conditions that can occur when
 * {@link ModifyOperation#setBatch(boolean)} is set to true. 
 * 
 * @since 4.1
 * @author Jeff Miller
 * @see ModifyOperation#execute()
 * @see SaveOperation#execute()
 */
public class BatchException extends OperationException
{
    private static final long serialVersionUID = 1L;

    public BatchException(String message)
    {
        super(message);
    }
}
