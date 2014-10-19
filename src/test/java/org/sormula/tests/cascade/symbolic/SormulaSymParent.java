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
package org.sormula.tests.cascade.symbolic;

import java.util.List;

import org.sormula.annotation.Column;


/**
 * Abstract base class for testing cascades that use symbolic field names. Each subclass
 * uses a different symbolic field name for defining the cascade to {@link SormulaSymChild}.
 * 
 * @author Jeff Miller
 */
public abstract class SormulaSymParent
{
    @Column(primaryKey=true)
    int parentId;
    String description;
    
    
    public SormulaSymParent()
    {
    }

    
    public SormulaSymParent(int parentId, String description)
    {
        this.parentId = parentId;
        this.description = description;
    }
    
    
    public int getParentId()
    {
        return parentId;
    }
    public void setParentId(int id)
    {
        this.parentId = id;
    }
    
    
    public String getDescription()
    {
        return description;
    }
    public void setDescription(String description)
    {
        this.description = description;
    }

    
    public abstract void add(SormulaSymChild child);
    public abstract List<SormulaSymChild> getChildList();
    public abstract void setChildList(List<SormulaSymChild> childList);
}
