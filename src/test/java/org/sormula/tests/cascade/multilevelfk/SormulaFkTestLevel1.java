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
package org.sormula.tests.cascade.multilevelfk;

import java.util.ArrayList;
import java.util.List;

import org.sormula.annotation.Row;
import org.sormula.annotation.cascade.OneToManyCascade;
import org.sormula.annotation.cascade.SelectCascade;


/**
 * Row class for testing multi-level cascade save operation. This is the top node of a 3 level graph.
 * Uses tables with foreign key ddl and cascades with foreign key annotations.
 * 
 * @author Jeff Miller
 */
@Row(primaryKeyFields="id")
public class SormulaFkTestLevel1
{
    int id;
    String description;
    
    // tests 1 to many relationship
    @OneToManyCascade( 
            selects=@SelectCascade(sourceParameterFieldNames="id", targetWhereName="byParent"),
            foreignKeyValueFields="parentId")
    List<SormulaFkTestLevel2> childList;
    
    
    public SormulaFkTestLevel1()
    {
        childList = new ArrayList<>();
    }

    
    public SormulaFkTestLevel1(int id, String description)
    {
        this();
        this.id = id;
        this.description = description;
    }

    
    public void add(SormulaFkTestLevel2 child)
    {
        childList.add(child);
        //child.setParentId(id); not required since @OneToManyCascade(..., foreignKeyValueFields="parentId"
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


    public List<SormulaFkTestLevel2> getChildList()
    {
        return childList;
    }
    public void setChildList(List<SormulaFkTestLevel2> childList)
    {
        this.childList = childList;
    }
}
