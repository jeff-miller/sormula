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
package org.sormula.examples.hierarchy;

import java.util.List;

import org.sormula.annotation.Column;
import org.sormula.annotation.Where;
import org.sormula.annotation.cascade.OneToManyCascade;
import org.sormula.annotation.cascade.SelectCascade;


/**
 * Row class for hierarchy example.
 */
@Where(name="forParent", fieldNames="parentNodeId") // not needed if using cascade 2
public class HierarchyNode
{
    @Column(primaryKey=true)
    int nodeId;
    int parentNodeId;
    String description;
    
    // must define OneToManyCascade explicitly
    
    // cascade 1
    @OneToManyCascade(selects=@SelectCascade(
    		sourceParameterFieldNames="nodeId", // defines that parameter from parent is nodeId 
    		targetWhereName="forParent"))		// defines that "forParent" where condition is used which selects where parentNodeId=?
    		
    /* cascade 2: an alternate way to define the cascade
    @OneToManyCascade(selects=@SelectCascade(
    		sourceParameterFieldNames="nodeId", 		// defines that parameter from parent is nodeId
    		targetWhereName="#foreignKeyValueFields"), 	// defines where condition is used which selects where parentNodeId=?
    		foreignKeyValueFields="parentNodeId")		// defines parentNodeId is foreign key
    */
    List<HierarchyNode> children;

    
	public int getNodeId()
	{
		return nodeId;
	}
	public void setNodeId(int nodeId)
	{
		this.nodeId = nodeId;
	}

	
	public int getParentNodeId()
	{
		return parentNodeId;
	}
	public void setParentNodeId(int parentNodeId)
	{
		this.parentNodeId = parentNodeId;
	}

	
	public String getDescription()
	{
		return description;
	}
	public void setDescription(String description)
	{
		this.description = description;
	}

	public List<HierarchyNode> getChildren()
	{
		return children;
	}

	public void setChildren(List<HierarchyNode> children)
	{
		this.children = children;
	}
	
	
	public void print() { print(""); }
	public void print(String indent)
	{
		System.out.println(indent + this);
		indent += "  ";
		if (children != null) for (HierarchyNode n : children) n.print(indent);
	}
	
	
	@Override
	public String toString()
	{
		return nodeId + " " + parentNodeId + " " + description;
	}
}
