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
package org.sormula.tests.active.cascade.lazy;

import java.util.ArrayList;
import java.util.List;

import org.sormula.active.ActiveRecord;
import org.sormula.annotation.cascade.OneToManyCascade;
import org.sormula.annotation.cascade.OneToOneCascade;
import org.sormula.annotation.cascade.SelectCascade;


/**
 * Row class where child belongs to only one parent. Child select cascades
 * are lazy loaded.
 * 
 * @author Jeff Miller
 */
public class SormulaTestParentLazyAR extends ActiveRecord<SormulaTestParentLazyAR>
{
    private static final long serialVersionUID = 1L;
    int id;
    String description;
    int child1Id;
    
    // tests 1 to many relationship
    @OneToManyCascade(targetClass=SormulaTestChildNLazyAR.class, 
            selects=@SelectCascade(sourceParameterFieldNames="id", targetWhereName="byParent", lazy=true))
    List<SormulaTestChildNLazyAR> childList;
    
    // tests 1 to 1 relationship
    @OneToOneCascade(selects=@SelectCascade(sourceParameterFieldNames="child1Id", lazy=true)) 
    SormulaTestChild1LazyAR child;

    
    public SormulaTestParentLazyAR()
    {
        childList = new ArrayList<SormulaTestChildNLazyAR>();
    }

    
    public SormulaTestParentLazyAR(int id, String description)
    {
        this();
        this.id = id;
        this.description = description;
    }

    
    public void add(SormulaTestChildNLazyAR child)
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


    public int getChild1Id()
    {
        return child1Id;
    }
    public void setChild1Id(int child1Id)
    {
        this.child1Id = child1Id;
    }


    public SormulaTestChild1LazyAR getChild()
    {
        lazySelectCascade("child");
        return child;
    }
    public void setChild(SormulaTestChild1LazyAR child)
    {
        this.child = child;
    }


    public List<SormulaTestChildNLazyAR> getChildList()
    {
        lazySelectCascade("childList");
        return childList;
    }
    public void setChildList(List<SormulaTestChildNLazyAR> childList)
    {
        this.childList = childList;
    }
}
