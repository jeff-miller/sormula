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

import java.util.ArrayList;
import java.util.List;

import org.sormula.annotation.Row;
import org.sormula.annotation.cascade.OneToManyCascade;
import org.sormula.annotation.cascade.SelectCascade;


/**
 * Row class for testing multi-level cascade save operation. This is the top node of a 3 level graph.
 * 
 * @author Jeff Miller
 */
@Row(primaryKeyFields="level1Id")
public class SormulaTestLevel1
{
    int level1Id;
    String description;
    
    // tests 1 to many relationship
    @OneToManyCascade( 
            selects=@SelectCascade(sourceParameterFieldNames="level1Id", targetWhereName="byParent"))
    List<SormulaTestLevel2> childList;
    
    
    public SormulaTestLevel1()
    {
        childList = new ArrayList<SormulaTestLevel2>();
    }

    
    public SormulaTestLevel1(int level1Id, String description)
    {
        this();
        this.level1Id = level1Id;
        this.description = description;
    }

    
    public void add(SormulaTestLevel2 child)
    {
        childList.add(child);
        child.setParentId(level1Id);
    }
    
    
    public int getLevel1Id()
    {
        return level1Id;
    }
    public void setLevel1Id(int level1Id)
    {
        this.level1Id = level1Id;
    }
    
    
    public String getDescription()
    {
        return description;
    }
    public void setDescription(String description)
    {
        this.description = description;
    }


    public List<SormulaTestLevel2> getChildList()
    {
        return childList;
    }
    public void setChildList(List<SormulaTestLevel2> childList)
    {
        this.childList = childList;
    }
}
