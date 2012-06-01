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
package org.sormula.operation.cascade.lazy;



/**
 * Exception that originates with lazy cascade operations. Since it is a runtime
 * exception, try/catch/finally blocks are optional.
 * 
 * @since 1.8
 * @author Jeff Miller
 */
public class LazyCascadeException extends RuntimeException
{
    private static final long serialVersionUID = 1L;


    /**
     * Constructs for a message.
     * 
     * @param message expection message
     */
    public LazyCascadeException(String message)
    {
        super(message);
    }

    
    /**
     * Constructs for a message and cause.
     * 
     * @param message expection message
     * @param cause cause of exception
     */
    public LazyCascadeException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
