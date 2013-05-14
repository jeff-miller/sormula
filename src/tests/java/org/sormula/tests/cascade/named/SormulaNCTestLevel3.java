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

import org.sormula.annotation.Where;


/**
 * Row class for testing multi-level named cascades. This is the leaf node of a 3 level graph.
 * 
 * @author Jeff Miller
 */
@Where(name="byParent", fieldNames="parentId")
public class SormulaNCTestLevel3
{
    int id;
    int parentId;
    String description;
    
    
    public SormulaNCTestLevel3()
    {
    }

    
    public SormulaNCTestLevel3(int id, String description)
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
}
