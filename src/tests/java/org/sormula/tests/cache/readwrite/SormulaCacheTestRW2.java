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
package org.sormula.tests.cache.readwrite;

import org.sormula.annotation.Row;
import org.sormula.annotation.cache.Cached;
import org.sormula.cache.readwrite.ReadWriteCache;


/**
 * Tests {@link Cached#enabled()} set false.
 * 
 * @author Jeff Miller
 */
@Cached(type=ReadWriteCache.class, enabled=false)
@Row(tableName="SormulaCacheTestRW", inhertedFields=true)
public class SormulaCacheTestRW2 // TODO extends SormulaCacheTestRW
{
    // TODO remove members and methods when inheritFields is implemented
    int id;
    int type;
    String description;
    
    
    public SormulaCacheTestRW2()
    {
    }

    
    public SormulaCacheTestRW2(int id, int type, String description)
    {
        this.id = id;
        this.type = type;
        this.description = description;
    }
    
    
    public int getId()
    {
        return id;
    }
    public void setId(int id)
    {
        this.id = id;
    }
    
    
    public String getDescription()
    {
        return description;
    }
    public void setDescription(String description)
    {
        this.description = description;
    }


    public int getType()
    {
        return type;
    }
    public void setType(int type)
    {
        this.type = type;
    }
    
}
