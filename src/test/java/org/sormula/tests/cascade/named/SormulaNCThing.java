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

import org.sormula.annotation.Row;
import org.sormula.annotation.Where;


/**
 * Row class for testing multi-level named cascades. This used as one-to-one relationship with
 * {@link SormulaNCTestLevel1}.
 * 
 * @author Jeff Miller
 */
@Row(primaryKeyFields="id")
@Where(name="byParent", fieldNames="parentId")
public class SormulaNCThing
{
    int id;
    String description;
    
    
    public SormulaNCThing()
    {
    }

    
    public SormulaNCThing(int id, String description)
    {
        this.id = id;
        this.description = description;
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
}
