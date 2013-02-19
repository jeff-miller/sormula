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
package org.sormula.tests.cascade.identity;

import java.util.ArrayList;
import java.util.List;

import org.sormula.annotation.Column;
import org.sormula.annotation.cascade.InsertCascade;
import org.sormula.annotation.cascade.OneToManyCascade;
import org.sormula.annotation.cascade.SelectCascade;


/**
 * Row class for testing insert cascades when parent uses identity column.
 * 
 * @author Jeff Miller
 */
public class SormulaIdentityParent
{
    @Column(identity=true)
    int parentId;
    String description;
    
    // tests 1 to many relationship
    @OneToManyCascade( 
            selects=@SelectCascade(sourceParameterFieldNames="id", targetWhereName="byParent"),
            inserts=@InsertCascade(targetForeignKeyFields="*"))
    List<SormulaIdentityChildN> childList;
    
    
    public SormulaIdentityParent()
    {
        childList = new ArrayList<SormulaIdentityChildN>();
    }

    
    public SormulaIdentityParent(String description)
    {
        this();
        this.description = description;
    }

    
    public void add(SormulaIdentityChildN child)
    {
        childList.add(child);
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


    public List<SormulaIdentityChildN> getChildList()
    {
        return childList;
    }
    public void setChildList(List<SormulaIdentityChildN> childList)
    {
        this.childList = childList;
    }
}
