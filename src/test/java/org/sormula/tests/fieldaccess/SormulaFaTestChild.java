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

import org.sormula.annotation.Column;
import org.sormula.annotation.Row;
import org.sormula.annotation.Transient;
import org.sormula.annotation.Where;
import org.sormula.reflect.FieldAccessType;


/**
 * Child row class for testing {@link Row#fieldAccess()} and {@link Column#fieldAccess()}.
 * 
 * @author Jeff Miller
 */
// use direct field access unless specified by Column annotation
@Row(fieldAccess=FieldAccessType.Direct)
@Where(name="byParent", fieldNames="parentId")
public class SormulaFaTestChild
{
    @Column(primaryKey=true, fieldAccess=FieldAccessType.Method)
    int childId;
    int parentId;
    
    @Column(fieldAccess=FieldAccessType.Direct) // redundant but make sure no problems
    String description;
    
    @Transient
    boolean setChildIdMethodInvoked; // to confirm method field access
    
    
    
    public SormulaFaTestChild()
    {
    }

    
    public SormulaFaTestChild(int childId, String description)
    {
        this.childId = childId;
        this.description = description;
    }
    
    
    public int getChildId()
    {
        return childId;
    }
    public void setChildId(int childId)
    {
        this.childId = childId;
        setChildIdMethodInvoked = true;
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


    public String getDescription()
    {
        return description;
    }
    public void setDescription(String description)
    {
        this.description = description;
    }
    */
}
