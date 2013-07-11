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
package org.sormula.tests.cascade.multilevel;

import org.sormula.annotation.Row;
import org.sormula.annotation.Where;



/**
 * Row class for testing multi-level cascade save operation. This is the leaf node of a 3 level graph.
 * 
 * @author Jeff Miller
 */
@Row(primaryKeyFields="level3Id")
@Where(name="byParent", fieldNames="parentId")
public class SormulaTestLevel3
{
    int level3Id;
    int parentId;
    String description;
    
    
    public SormulaTestLevel3()
    {
    }

    
    public SormulaTestLevel3(int level3Id, String description)
    {
        this.level3Id = level3Id;
        this.description = description;
    }

    
    public int getLevel3Id()
    {
        return level3Id;
    }
    public void setLevel3Id(int level3Id)
    {
        this.level3Id = level3Id;
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
