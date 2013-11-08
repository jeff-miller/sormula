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
package org.sormula.tests.cascade.named;

import java.util.ArrayList;
import java.util.List;

import org.sormula.annotation.Row;
import org.sormula.annotation.Where;
import org.sormula.annotation.cascade.OneToManyCascade;
import org.sormula.annotation.cascade.SelectCascade;


/**
 * Row class for testing multi-level named cascades. This is the 2nd level of a 3 level graph.
 * 
 * @author Jeff Miller
 */
@Row(primaryKeyFields="id")
@Where(name="byParent", fieldNames="parentId")
public class SormulaNCTestLevel2
{
    int id;
    int parentId;
    String description;
    
    // tests that named cascades are used at more than one level
    @OneToManyCascade(name="2-to-3", 
            selects=@SelectCascade(sourceParameterFieldNames="id", targetWhereName="byParent"),
            foreignKeyValueFields="parentId")
    List<SormulaNCTestLevel3> childList;
    
    
    public SormulaNCTestLevel2()
    {
        childList = new ArrayList<>();
    }

    
    public SormulaNCTestLevel2(int id, String description)
    {
        this();
        this.id = id;
        this.description = description;
    }

    
    public void add(SormulaNCTestLevel3 child)
    {
        childList.add(child);
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


    public List<SormulaNCTestLevel3> getChildList()
    {
        return childList;
    }
    public void setChildList(List<SormulaNCTestLevel3> childList)
    {
        this.childList = childList;
    }
}
