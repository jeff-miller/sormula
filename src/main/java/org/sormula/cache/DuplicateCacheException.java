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

import java.util.Arrays;


/**
 * Indicates that a duplicate set of primary key(s) already exists in the cache.
 * 
 * @since 3.0
 * @author Jeff Miller
 */
public class DuplicateCacheException extends CacheException
{
    private static final long serialVersionUID = 1L;


    /**
     * Constructs for the duplicate keys.
     * 
     * @param primaryKeys primary keys that caused the duplicate error
     */
    public DuplicateCacheException(Object... primaryKeys)
    {
        super("row already exists in cache for keys=" + Arrays.toString(primaryKeys));
    }
}