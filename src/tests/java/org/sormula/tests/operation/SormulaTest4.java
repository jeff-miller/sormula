/* sormula - Simple object relational mapping
 * Copyright (C) 2011 Jeff Miller
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
package org.sormula.tests.operation;

import org.sormula.annotation.Column;
import org.sormula.annotation.OrderBy;
import org.sormula.annotation.OrderBys;
import org.sormula.annotation.Where;
import org.sormula.annotation.WhereField;
import org.sormula.annotation.Wheres;


/**
 * Row class for {@linkplain InsertTest}, {@linkplain SelectTest}, {@linkplain UpdateTest},
 * and {@linkplain DeleteTest}.
 * 
 * @author Jeff Miller
 */
@Wheres(whereConditions={
        @Where(name="byType", fieldNames="type"),
        @Where(name="idIn", whereFields=@WhereField(name="id", comparisonOperator="in"))
})

// note: OrderBys is not needed since only 1 OrderBy, keep Orderbys to test annotation processing
@OrderBys(orderByConditions={
    @OrderBy(name="ob1", ascending="type")
    })
public class SormulaTest4
{
    @Column(primaryKey=true)
    int id;
    int type;
    String description;
    
    
    public SormulaTest4()
    {
    }

    
    public SormulaTest4(int id, int type, String description)
    {
        this.id = id;
        this.type = type;
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


    public int getType()
    {
        return type;
    }
    public void setType(int type)
    {
        this.type = type;
    }


    @Override
    public int hashCode()
    {
        return id;
    }


    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof SormulaTest4)
        {
            SormulaTest4 other = (SormulaTest4) obj;
            return id == other.id;
        }
        
        return false;
    }
}
