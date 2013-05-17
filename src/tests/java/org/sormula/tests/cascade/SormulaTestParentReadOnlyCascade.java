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

import java.util.ArrayList;
import java.util.List;

import org.sormula.annotation.Row;
import org.sormula.annotation.cascade.OneToManyCascade;
import org.sormula.annotation.cascade.OneToOneCascade;
import org.sormula.annotation.cascade.SelectCascade;


/**
 * Row class where child belongs to only one parent. Cascades are defined as readonly.
 * Uses same test data and tables as {@link SormulaTestParent}.
 * 
 * @author Jeff Miller
 */
@Row(tableName="SormulaTestParentROC", primaryKeyFields="id")
public class SormulaTestParentReadOnlyCascade
{
    int id;
    String description;
    int child1Id;
    
    // tests 1 to many relationship
    @OneToManyCascade(readOnly=true, // optional in v1.9 targetClass=SormulaTestChildN.class,
          selects=@SelectCascade(sourceParameterFieldNames="id", targetWhereName="byParent"))
    // equivalent
    //@OneToManyCascade(targetClass=SormulaTestChildNReadOnlyCascade.class,
    //        selects=@SelectCascade(sourceParameterFieldNames="id", targetWhereName="byParent"), updates={}, inserts={}, deletes={})
            
    List<SormulaTestChildNReadOnlyCascade> childList;
    
    // tests 1 to 1 relationship
    @OneToOneCascade(readOnly=true, selects=@SelectCascade(sourceParameterFieldNames="child1Id"))
    // equivalent @OneToOneCascade(selects=@SelectCascade(sourceParameterFieldNames="child1Id"), updates={}, inserts={}, deletes={})
    SormulaTestChild1ReadOnlyCascade child;
    
    
    public SormulaTestParentReadOnlyCascade()
    {
        childList = new ArrayList<>();
    }

    
    public SormulaTestParentReadOnlyCascade(int id, String description)
    {
        this();
        this.id = id;
        this.description = description;
    }

    
    public void add(SormulaTestChildNReadOnlyCascade child)
    {
        childList.add(child);
        child.setParentId(id);
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


    public List<SormulaTestChildNReadOnlyCascade> getChildList()
    {
        return childList;
    }
    public void setChildList(List<SormulaTestChildNReadOnlyCascade> childList)
    {
        this.childList = childList;
    }


    public int getChild1Id()
    {
        return child1Id;
    }
    public void setChild1Id(int child1Id)
    {
        this.child1Id = child1Id;
    }


    public SormulaTestChild1ReadOnlyCascade getChild()
    {
        return child;
    }
    public void setChild(SormulaTestChild1ReadOnlyCascade child)
    {
        this.child = child;
    }
}
