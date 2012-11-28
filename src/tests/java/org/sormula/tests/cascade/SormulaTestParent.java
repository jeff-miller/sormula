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
import java.util.Map;

import org.sormula.annotation.Column;
import org.sormula.annotation.cascade.Cascade;
import org.sormula.annotation.cascade.DeleteCascade;
import org.sormula.annotation.cascade.InsertCascade;
import org.sormula.annotation.cascade.OneToManyCascade;
import org.sormula.annotation.cascade.OneToOneCascade;
import org.sormula.annotation.cascade.SaveCascade;
import org.sormula.annotation.cascade.SelectCascade;
import org.sormula.annotation.cascade.UpdateCascade;
import org.sormula.operation.HashMapSelectOperation;


/**
 * Row class where child belongs to only one parent so that cascades are defined for all operations
 * 
 * @author Jeff Miller
 */
public class SormulaTestParent
{
    @Column(primaryKey=true)
    int id;
    String description;
    int child1Id;
    
    // tests 1 to many relationship
    @OneToManyCascade(// test when targetClass is omitted targetClass=SormulaTestChildN.class, 
            selects=@SelectCascade(sourceParameterFieldNames="id", targetWhereName="byParent"))
    List<SormulaTestChildN> childList;
    
    // tests 1 to 1 relationship
    @OneToOneCascade(selects=@SelectCascade(sourceParameterFieldNames="child1Id"))
    SormulaTestChild1 child;
    
    // tests general cascade and map type
    @Cascade(targetClass=SormulaTestChildM.class, // targetClass is optional in v1.9 
    		selects=@SelectCascade(operation=HashMapSelectOperation.class, sourceParameterFieldNames="id", targetWhereName="byParent", targetKeyMethodName="getId"),
			inserts=@InsertCascade(),
            updates=@UpdateCascade(),
            saves=@SaveCascade(),
            deletes=@DeleteCascade()
	)
    Map<Integer, SormulaTestChildM> childMap;
    
    
    public SormulaTestParent()
    {
        childList = new ArrayList<SormulaTestChildN>();
    }

    
    public SormulaTestParent(int id, String description)
    {
        this();
        this.id = id;
        this.description = description;
    }

    
    public void add(SormulaTestChildN child)
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


    public List<SormulaTestChildN> getChildList()
    {
        return childList;
    }
    public void setChildList(List<SormulaTestChildN> childList)
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


    public SormulaTestChild1 getChild()
    {
        return child;
    }
    public void setChild(SormulaTestChild1 child)
    {
        this.child = child;
    }


    public Map<Integer, SormulaTestChildM> getChildMap()
    {
		return childMap;
	}
	public void setChildMap(Map<Integer, SormulaTestChildM> childMap) 
	{
		this.childMap = childMap;
	}
}
