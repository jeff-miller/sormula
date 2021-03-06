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
 * Indicates a field name was not found in a class.
 * 
 * @since 3.1
 * @author Jeff Miller
 */
public class MissingFieldException extends OperationException
{
    private static final long serialVersionUID = 1L;


    /**
     * Constructs for field and class where field is expected.
     * 
     * @param fieldName name of field not found
     * @param rowClass class where field name was searched
     */
    public MissingFieldException(String fieldName, Class<?> rowClass)
    {
        super("field named, " + fieldName + ", does not exist in " + rowClass);
    }
}
