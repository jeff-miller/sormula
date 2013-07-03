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
package org.sormula.tests.cascade;

import org.sormula.annotation.Column;



/**
 * Child of {@link SormulaTestParent}. Child may occur 0 or 1 times for 1 parent.
 * 
 * @author Jeff Miller
 */
public class SormulaTestChild1
{
    @Column(primaryKey=true)
    int childId;
    String description;
    
    
    public SormulaTestChild1()
    {
    }

    
    public SormulaTestChild1(int childId, String description)
    {
        this.childId = childId;
        this.description = description;
    }
    
    
    public int getChildId()
    {
        return childId;
    }
    public void setChildId(int childId)
    {
        this.childId = childId;
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
