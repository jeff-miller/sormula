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

import org.sormula.reflect.FieldExtractor;


/**
 * Key used by {@link AbstractCache} for maps of committed and uncommitted rows. 
 * 
 * @since 3.0
 * @author Jeff Miller
 */
public class CacheKey
{
    Object[] primaryKeys;
    int hashCode;
    
    
    /**
     * Constructs for primary keys of a row. Assumes length is same for all rows of the same type.  
     * Assumes primaryKeys are never null.
     * 
     * @param primaryKeys primary key(s) for a row
     * @see FieldExtractor
     * @see AbstractCache#getPrimaryKeyValues(Object)
     */
    public CacheKey(Object[] primaryKeys)
    {
        this.primaryKeys = primaryKeys;
        
        // assume keys won't change, compute once
        hashCode = 1;
        for (Object key : primaryKeys) hashCode = 31 * hashCode + key.hashCode();
    }


    /**
     * Gets primary key(s) as Object array.
     * 
     * @return primary key(s) supplied in constructor
     */
    public Object[] getPrimaryKeys()
    {
        return primaryKeys;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "CacheKey [primaryKeys=" + Arrays.toString(primaryKeys)
                + ", hashCode=" + hashCode + "]";
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        return hashCode;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof CacheKey)
        {
            CacheKey other = (CacheKey) obj;
            if (primaryKeys == other.primaryKeys) return true;

            for (int i = 0; i < primaryKeys.length; ++i)
            {
                if (!primaryKeys[i].equals(other.primaryKeys[i])) return false;
            }
            
            return true;
        }
        
        return false;
    }
}
