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
package org.sormula.cache;

import org.sormula.SormulaException;


/**
 * Exception that originals from classes in org.sormula.cache packages.
 * 
 * @since 3.0
 * @author Jeff Miller
 */
public class CacheException extends SormulaException
{
    private static final long serialVersionUID = 1L;


    /**
     * Constructs for a message.
     * 
     * @param message expection message
     */
    public CacheException(String message)
    {
        super(message);
    }

    
    /**
     * Constructs for a message and cause.
     * 
     * @param message expection message
     * @param cause cause of exception
     */
    public CacheException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
