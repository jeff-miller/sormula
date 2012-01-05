/* sormula - Simple object relational mapping
 * Copyright (C) 2011 Jeff Miller
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
package org.sormula.tests.operation;



/**
 * Row class for {@link ReadOnlyTest}. Same as {@link SormulaTest4}.
 * A separate table from {@link SormulaTest4} prevents threading
 * problems since some databases are not multithreaded capable.
 * 
 * @author Jeff Miller
 */
public class SormulaTest4RO
{
    int id;
    int type;
    String description;
    
    
    public SormulaTest4RO()
    {
    }

    
    public SormulaTest4RO(int id, int type, String description)
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
        if (obj instanceof SormulaTest4RO)
        {
            SormulaTest4RO other = (SormulaTest4RO) obj;
            return id == other.id;
        }
        
        return false;
    }
}
