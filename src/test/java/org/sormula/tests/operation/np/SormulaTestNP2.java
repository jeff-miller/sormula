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
package org.sormula.tests.operation.np;

import org.sormula.annotation.Row;
import org.sormula.annotation.Where;
import org.sormula.annotation.WhereField;


/**
 * Row class for testing named parameters. This is the 2nd level of a 2 level graph.
 * 
 * @author Jeff Miller
 */
@Row(primaryKeyFields="level2Id")
@Where(name="byParent", fieldNames="parentId")
        
// where parentId=? AND level2Id>=?
@Where(name="byParentLevel2Id", whereFields={@WhereField(name="parentId"), @WhereField(name="level2Id", comparisonOperator=">=")})

public class SormulaTestNP2
{
    int level2Id;
    int parentId;
    String description;
    
    
    public SormulaTestNP2()
    {
    }

    
    public SormulaTestNP2(int level2Id, String description)
    {
        this();
        this.level2Id = level2Id;
        this.description = description;
    }
    
    
    public int getLevel2Id()
    {
        return level2Id;
    }
    public void setLevel2Id(int level2Id)
    {
        this.level2Id = level2Id;
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
