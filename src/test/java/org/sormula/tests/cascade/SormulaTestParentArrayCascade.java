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

import org.sormula.annotation.Row;
import org.sormula.annotation.cascade.OneToManyCascade;
import org.sormula.annotation.cascade.SelectCascade;
import org.sormula.operation.HashMapSelectOperation;


/**
 * Row class where child belongs to only one parent and member of parent as an array.
 * 
 * @author Jeff Miller
 */
@Row(tableName="SormulaTestParentAC", primaryKeyFields="parentId")
public class SormulaTestParentArrayCascade
{
    int parentId;
    String description;
    
    // tests 1 to many relationship where selected list gets converted to array
    @OneToManyCascade
    SormulaTestChildNArrayCascade[] children;
    
    // tests 1 to many relationship where selected map gets converted to array
    @OneToManyCascade(selects=@SelectCascade(targetWhereName="byParent", 
                operation=HashMapSelectOperation.class, targetKeyMethodName="getId"))
    SormulaTestChildNArrayCascade[] children2;
    
    
    public SormulaTestParentArrayCascade()
    {
    }

    
    public SormulaTestParentArrayCascade(int parentId, String description)
    {
        this.parentId = parentId;
        this.description = description;
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


    public SormulaTestChildNArrayCascade[] getChildren()
    {
        return children;
    }
    public void setChildren(SormulaTestChildNArrayCascade[] children)
    {
        if (children != null)
        {
            for (SormulaTestChildNArrayCascade child: children) child.setParentId(parentId);
        }
        
        this.children = children;
    }


    public SormulaTestChildNArrayCascade[] getChildren2()
    {
        return children2;
    }
    public void setChildren2(SormulaTestChildNArrayCascade[] children2)
    {
        if (children2 != null)
        {
            for (SormulaTestChildNArrayCascade child: children2) child.setParentId(parentId);
        }
        
        this.children2 = children2;
    }
}
