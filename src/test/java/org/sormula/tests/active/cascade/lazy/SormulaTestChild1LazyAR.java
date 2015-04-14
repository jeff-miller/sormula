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

import org.sormula.active.ActiveRecord;
import org.sormula.annotation.Column;
import org.sormula.reflect.FieldAccessType;


/**
 * Child of {@link SormulaTestParentLazyAR}. Child may occur 0 or 1 times for 1 parent.
 * 
 * @author Jeff Miller
 */
public class SormulaTestChild1LazyAR extends ActiveRecord<SormulaTestChild1LazyAR>
{
    private static final long serialVersionUID = 1L;
    
    @Column(primaryKey=true, fieldAccess=FieldAccessType.Direct)// TODO
    int id;
    String description;
    
    
    public SormulaTestChild1LazyAR()
    {
    }

    
    public SormulaTestChild1LazyAR(int id, String description)
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
