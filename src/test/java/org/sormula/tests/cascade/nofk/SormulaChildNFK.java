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
package org.sormula.tests.cascade.nofk;

import org.sormula.annotation.Column;
import org.sormula.annotation.Where;


/**
 * Child of {@link SormulaParentNFK}. Child may occur 0 to n times for 1 parent.
 * 
 * @author Jeff Miller
 */
@Where(name="byParent", fieldNames="parentId")
public class SormulaChildNFK
{
    @Column(primaryKey=true)
    int childId;
    int parentId;
    String description;
    
    
    public SormulaChildNFK()
    {
    }

    
    public SormulaChildNFK(int childId, String description)
    {
        this.childId = childId;
        this.description = description;
    }
    
    
    public int getChildId()
    {
        return childId;
    }
    public void setChildId(int id)
    {
        this.childId = id;
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
}
