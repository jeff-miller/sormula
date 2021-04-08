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

/**
 * Invalid cache state caused by cache activity outside of a transaction or failure to 
 * commit or rollback transaction.
 * 
 * @since 3.0
 * @author Jeff Miller
 */
public class IllegalCacheStateException extends CacheException
{
    private static final long serialVersionUID = 1L;


    /**
     * Constructs for a message.
     * 
     * @param message exception message
     */
    public IllegalCacheStateException(String message)
    {
        super(message);
    }

    
    /**
     * Constructs for a message and cause.
     * 
     * @param message exception message
     * @param cause cause of exception
     */
    public IllegalCacheStateException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
