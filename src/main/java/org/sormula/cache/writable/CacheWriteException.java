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
package org.sormula.cache.writable;

import java.util.Arrays;

import org.sormula.cache.CacheException;


/**
 * Thrown when cache has error writing to the database.
 * 
 * @since 3.0
 * @author Jeff Miller
 */
public class CacheWriteException extends CacheException
{
    private static final long serialVersionUID = 1L;

    
    /**
     * Constructs for a table, keys, and cause.
     * 
     * @param clazz class type of cached row
     * @param primaryKeys primary keys of cached row
     * @param cause cause of exception
     */
    public CacheWriteException(Class<?> clazz, Object[] primaryKeys, Throwable cause)
    {
        super("error writing cache to database class=" + clazz.getCanonicalName() +
                " keys=" + Arrays.toString(primaryKeys), cause);
    }
}
