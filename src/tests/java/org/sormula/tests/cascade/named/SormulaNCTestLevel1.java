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
import org.sormula.annotation.cascade.OneToManyCascade;
import org.sormula.annotation.cascade.OneToOneCascade;
import org.sormula.annotation.cascade.SelectCascade;
import org.sormula.operation.ScalarSelectOperation;


/**
 * Row class for testing multi-level named cascades. This is the top node of a 3 level graph.
 * 
 * @author Jeff Miller
 */
@Row(primaryKeyFields="id")
public class SormulaNCTestLevel1
{
    int id;
    String description;
    int thing1Id;
    int thing2Id;
    int thing3Id;
    
    // tests specific name
    @OneToManyCascade(name="1-to-2",
            selects=@SelectCascade(sourceParameterFieldNames="id", targetWhereName="byParent"),
            foreignKeyValueFields="parentId")
    List<SormulaNCTestLevel2> childList;
    
    // tests unused name
    @OneToOneCascade(name="other",
            selects=@SelectCascade(sourceParameterFieldNames="thing1Id", operation=ScalarSelectOperation.class))
    SormulaNCThing thing1;
    
    // tests blank name (default)
    @OneToOneCascade(
            selects=@SelectCascade(sourceParameterFieldNames="thing2Id", operation=ScalarSelectOperation.class))
    SormulaNCThing thing2;
    
    // tests wildcard name
    @OneToOneCascade(name="*", // always cascades
            selects=@SelectCascade(sourceParameterFieldNames="thing3Id", operation=ScalarSelectOperation.class))
    SormulaNCThing thing3;
    
    
    public SormulaNCTestLevel1()
    {
        childList = new ArrayList<>();
    }

    
    public SormulaNCTestLevel1(int id, String description)
    {
        this();
        this.id = id;
        this.description = description;
    }

    
    public void add(SormulaNCTestLevel2 child)
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
    
    
    public String getDescription()
    {
        return description;
    }
    public void setDescription(String description)
    {
        this.description = description;
    }


    public List<SormulaNCTestLevel2> getChildList()
    {
        return childList;
    }
    public void setChildList(List<SormulaNCTestLevel2> childList)
    {
        this.childList = childList;
    }


    public int getThing1Id()
    {
        return thing1Id;
    }
    public void setThing1Id(int thing1Id)
    {
        this.thing1Id = thing1Id;
    }


    public int getThing2Id()
    {
        return thing2Id;
    }
    public void setThing2Id(int thing2Id)
    {
        this.thing2Id = thing2Id;
    }


    public int getThing3Id()
    {
        return thing3Id;
    }
    public void setThing3Id(int thing3Id)
    {
        this.thing3Id = thing3Id;
    }


    public SormulaNCThing getThing1()
    {
        return thing1;
    }
    public void setThing1(SormulaNCThing thing1)
    {
        this.thing1 = thing1;
        thing1Id = thing1.getId();
    }


    public SormulaNCThing getThing2()
    {
        return thing2;
    }
    public void setThing2(SormulaNCThing thing2)
    {
        this.thing2 = thing2;
        thing2Id = thing2.getId();
    }


    public SormulaNCThing getThing3()
    {
        return thing3;
    }
    public void setThing3(SormulaNCThing thing3)
    {
        this.thing3 = thing3;
        thing3Id = thing3.getId();
    }
}
