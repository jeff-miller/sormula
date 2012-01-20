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
package org.sormula.tests.zeroannotation;


/**
 * Row class with no annotations. First column is primary key when no annotation is specified.
 * Used by {@linkplain InsertTest}, {@linkplain SelectTest}, {@linkplain UpdateTest},
 * and {@linkplain DeleteTest}.
 * <p>
 * No annotations are needed for insert, update, delete, and select by primary key if row
 * class conforms to the following:
 * <ul>
 * <li>Table name and class name are the same</li>
 * <li>Column names and class field names are the same</li>
 * <li>First field corresponds to primary column</li>
 * <li>All fields in class are columns in table</li>
 * </ul>
 * 
 * @author Jeff Miller
 */
public class ZeroAnnotationTest
{
    int id;
    int type;
    String description;
    
    
    public ZeroAnnotationTest()
    {
    }

    
    public ZeroAnnotationTest(int id, int type, String description)
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


    @Override
    public int hashCode()
    {
        return id;
    }


    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof ZeroAnnotationTest)
        {
            ZeroAnnotationTest other = (ZeroAnnotationTest) obj;
            return id == other.id;
        }
        
        return false;
    }
}
