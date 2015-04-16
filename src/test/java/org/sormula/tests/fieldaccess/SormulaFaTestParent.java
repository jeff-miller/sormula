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
package org.sormula.tests.fieldaccess;

import java.util.ArrayList;
import java.util.List;

import org.sormula.annotation.Column;
import org.sormula.annotation.Row;
import org.sormula.annotation.cascade.OneToManyCascade;
import org.sormula.reflect.FieldAccessType;


/**
 * Parent row class for testing {@link Row#fieldAccess()} and {@link Column#fieldAccess()}.
 * 
 * @author Jeff Miller
 */

// use cache to test that cache FieldExtractor can work with direct access
//@Cached can't mix cached cascades

// use method access for all fields unless specified by Column annotation
@Row(fieldAccess=FieldAccessType.Method) 

public class SormulaFaTestParent
{
    @Column(primaryKey=true, fieldAccess=FieldAccessType.Direct) 
    int parentId;
    String description;
    
    // tests cascade operations
    @OneToManyCascade(foreignKeyValueFields="#")
    List<SormulaFaTestChild> childList;
    
    
    public SormulaFaTestParent()
    {
    }

    
    public SormulaFaTestParent(int parentId, String description)
    {
        childList = new ArrayList<>();
        this.parentId = parentId;
        this.description = description;
    }

    
    public void add(SormulaFaTestChild child)
    {
        childList.add(child);
        //child.parentId = parentId; // not needed since foreignKeyValueFields="#"
    }
    
    
    /* omit getters/setters to verify that direct field access is used
    public int getParentId()
    {
        return parentId;
    }
    public void setParentId(int parentId)
    {
        this.parentId = parentId;
    }
    */
    
    
    public String getDescription()
    {
        return description;
    }
    public void setDescription(String description)
    {
        this.description = description;
    }


    public List<SormulaFaTestChild> getChildList()
    {
        return childList;
    }
    public void setChildList(List<SormulaFaTestChild> childList)
    {
        this.childList = childList;
    }
}
