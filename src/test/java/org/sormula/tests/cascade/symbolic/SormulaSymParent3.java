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

import java.util.ArrayList;
import java.util.List;

import org.sormula.annotation.Row;
import org.sormula.annotation.cascade.OneToManyCascade;
import org.sormula.annotation.cascade.SelectCascade;


/**
 * Row class for testing cascades that use symbolic field names.
 * 
 * @author Jeff Miller
 */
@Row(tableName="SormulaSymParent", inhertedFields=true)
public class SormulaSymParent3 extends SormulaSymParent
{
    // tests 1 to many relationship
    @OneToManyCascade(foreignKeyValueFields="#", 
            selects=@SelectCascade(sourceParameterFieldNames="#targetFieldNames", targetWhereName="#foreignKeyValueFields"))
    List<SormulaSymChild> childList;
    
    
    public SormulaSymParent3()
    {
    }

    
    public SormulaSymParent3(int parentId, String description)
    {
        super(parentId, description);
        childList = new ArrayList<>();
    }


    @Override
    public void add(SormulaSymChild child)
    {
        childList.add(child);
    }

    @Override
    public List<SormulaSymChild> getChildList()
    {
        return childList;
    }
    
    @Override
    public void setChildList(List<SormulaSymChild> childList)
    {
        this.childList = childList;
    }
}
