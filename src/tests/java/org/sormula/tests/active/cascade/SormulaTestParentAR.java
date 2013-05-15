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

import java.util.ArrayList;
import java.util.List;

import org.sormula.active.ActiveRecord;
import org.sormula.annotation.Row;
import org.sormula.annotation.cascade.OneToManyCascade;
import org.sormula.annotation.cascade.OneToOneCascade;
import org.sormula.annotation.cascade.SelectCascade;


/**
 * Row class where child belongs to only one parent so that cascades are defined for all operations
 * 
 * @author Jeff Miller
 */
@Row(primaryKeyFields="parentId")
public class SormulaTestParentAR extends ActiveRecord<SormulaTestParentAR>
{
    private static final long serialVersionUID = 1L;
    int parentId;
    String description;
    int child1Id;
    
    // tests 1 to many relationship
    @OneToManyCascade(// optional in v1.9 targetClass=SormulaTestChildNAR.class, 
            selects=@SelectCascade(sourceParameterFieldNames="parentId", targetWhereName="byParent"),
            foreignKeyValueFields="#") // tests foreignKeyValueFields for ActiveRecord
    List<SormulaTestChildNAR> childList;
    
    // tests 1 to 1 relationship
    @OneToOneCascade(
            selects=@SelectCascade(sourceParameterFieldNames="child1Id"))
    SormulaTestChild1AR child;

    
    public SormulaTestParentAR()
    {
        childList = new ArrayList<SormulaTestChildNAR>();
    }

    
    public SormulaTestParentAR(int parentId, String description)
    {
        this();
        this.parentId = parentId;
        this.description = description;
    }

    
    public void add(SormulaTestChildNAR child)
    {
        childList.add(child);
        
        // don't need since foreignKeyValueFields="#" was specified
        // child.setParentId(parentId);
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


    public int getChild1Id()
    {
        return child1Id;
    }
    public void setChild1Id(int child1Id)
    {
        this.child1Id = child1Id;
    }


    public SormulaTestChild1AR getChild()
    {
        return child;
    }
    public void setChild(SormulaTestChild1AR child)
    {
        this.child = child;
    }


    public List<SormulaTestChildNAR> getChildList()
    {
        return childList;
    }
    public void setChildList(List<SormulaTestChildNAR> childList)
    {
        this.childList = childList;
    }
}
