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
package org.sormula.tests.cascade.fk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sormula.annotation.Column;
import org.sormula.annotation.cascade.Cascade;
import org.sormula.annotation.cascade.DeleteCascade;
import org.sormula.annotation.cascade.InsertCascade;
import org.sormula.annotation.cascade.OneToManyCascade;
import org.sormula.annotation.cascade.SaveCascade;
import org.sormula.annotation.cascade.SelectCascade;
import org.sormula.annotation.cascade.UpdateCascade;
import org.sormula.operation.HashMapSelectOperation;


/**
 * Row class for testing foreign key updates by casacades.
 * 
 * @author Jeff Miller
 */
public class SormulaFKTestParent
{
    @Column(primaryKey=true)
    int parentId;
    String description;
    
    // tests 1 to many relationship
    @OneToManyCascade( 
            selects=@SelectCascade(sourceParameterFieldNames="parentId", targetWhereName="byParent"),
            foreignKeyValueFields="#",          // foreign key fields are same name as parent foreign key (parentId)
            foreignKeyReferenceField="parent"   // foreign key reference field is named "parent"
    )
    List<SormulaFKTestChildN> childList;
    
    // tests general cascade and map type
    @Cascade( 
    		selects=@SelectCascade(operation=HashMapSelectOperation.class, sourceParameterFieldNames="parentId", targetWhereName="byParent", targetKeyMethodName="getId"),
			inserts=@InsertCascade(),
            updates=@UpdateCascade(),
            saves=@SaveCascade(),
            deletes=@DeleteCascade(),
            foreignKeyValueFields="parentId",   // foreign key field in child is named "parentId" 
            foreignKeyReferenceField="class"    // foreign key reference field has same name as parent foreign key class (sormulaFKTestParent)
	)
    Map<Integer, SormulaFKTestChildM> childMap;
    
    
    public SormulaFKTestParent()
    {
        childList = new ArrayList<>();
    }

    
    public SormulaFKTestParent(int parentId, String description)
    {
        this();
        this.parentId = parentId;
        this.description = description;
    }

    
    public void add(SormulaFKTestChildN child)
    {
        childList.add(child);
        
        // not needed when foreign key annotated 
        // child.setParentId(parentId);
    }
    
    
    public int getParentId()
    {
        return parentId;
    }
    public void setParentId(int id)
    {
        this.parentId = id;
    }
    
    
    public String getDescription()
    {
        return description;
    }
    public void setDescription(String description)
    {
        this.description = description;
    }


    public List<SormulaFKTestChildN> getChildList()
    {
        return childList;
    }
    public void setChildList(List<SormulaFKTestChildN> childList)
    {
        this.childList = childList;
    }


    public Map<Integer, SormulaFKTestChildM> getChildMap()
    {
		return childMap;
	}
	public void setChildMap(Map<Integer, SormulaFKTestChildM> childMap) 
	{
		this.childMap = childMap;
	}
}
