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
package org.sormula.tests.zeroannotation;


/**
 * Child of {@link ZeroAnnotationTest}.
 *  
 * @author Jeff Miller
 */
public class ZeroAnnotationChild
{
    int childId;
    int zatId; // parent id, named same as in ZeroAnnotationTest so that default foreign key relationship can be determined
    
    
    public ZeroAnnotationChild()
    {
    }
    
    
    public ZeroAnnotationChild(int childId, int zatId)
    {
        this.childId = childId;
        this.zatId = zatId;
    }

    
    public int getChildId()
    {
        return childId;
    }
    public void setChildId(int childId)
    {
        this.childId = childId;
    }
    
    
    public int getZatId()
    {
        return zatId;
    }
    public void setZatId(int zatId)
    {
        this.zatId = zatId;
    }


    /**
     * Default Eclipse implementation. Required for use in {@link ZeroAnnotationTest#testMap}.
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + childId;
        result = prime * result + zatId;
        return result;
    }


    /**
     * Default Eclipse implementation. Required for use in {@link ZeroAnnotationTest#testMap}.
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ZeroAnnotationChild other = (ZeroAnnotationChild) obj;
        if (childId != other.childId)
            return false;
        if (zatId != other.zatId)
            return false;
        return true;
    }
}
