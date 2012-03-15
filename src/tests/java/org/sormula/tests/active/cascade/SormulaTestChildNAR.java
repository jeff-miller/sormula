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
package org.sormula.tests.active.cascade;

import org.sormula.active.ActiveRecord;
import org.sormula.annotation.Where;


/**
 * Child of {@link SormulaTestParentAR}. Child may occur 0 to n times for 1 parent.
 * 
 * @author Jeff Miller
 */
@Where(name="byParent", fieldNames="parentId")
public class SormulaTestChildNAR extends ActiveRecord
{
    private static final long serialVersionUID = 1L;
    int id;
    int parentId;
    String description;
    
    
    public SormulaTestChildNAR()
    {
    }

    
    public SormulaTestChildNAR(int id, String description)
    {
        this.id = id;
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
    
    
    public int getParentId()
    {
        return parentId;
    }
    public void setParentId(int parentId)
    {
        this.parentId = parentId;
    }


    public String getDescription()
    {
        return description;
    }
    public void setDescription(String description)
    {
        this.description = description;
    }


    @Override
    public int hashCode()
    {
        return id;
    }


    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof SormulaTestChildNAR)
        {
            SormulaTestChildNAR other = (SormulaTestChildNAR) obj;
            return id == other.id;
        }
        
        return false;
    }


	@Override
	public String toString() 
	{
		return id + " " + description;
	}
}
