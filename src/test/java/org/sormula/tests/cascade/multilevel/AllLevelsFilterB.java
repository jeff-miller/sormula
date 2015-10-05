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
package org.sormula.tests.cascade.multilevel;

import org.sormula.operation.ScalarSelectOperation;
import org.sormula.operation.filter.AbstractSelectCascadeFilter;
import org.sormula.operation.filter.SelectCascadeFilter;


/**
 * {@link SelectCascadeFilter} that filters all row class types using {@link AbstractSelectCascadeFilter}.
 * Instead of one {@link SelectCascadeFilter} for each row type, there is one method for each row type.
 * 
 * @author Jeff Miller
 */
public class AllLevelsFilterB extends AbstractSelectCascadeFilter
{
    public boolean accept(ScalarSelectOperation<SormulaTestLevel1> source, SormulaTestLevel1 row, boolean cascadesCompleted)
    {
        boolean keep = true;
        
        if (cascadesCompleted)
        {
            keep = row.getChildList().size() > 0; // keep non empty nodes
        }
        else
        {
            keep = row.getLevel1Id() != 104; // none of 104 or descendants
        }
        
        return keep;
    }
    
    
    public boolean accept(ScalarSelectOperation<SormulaTestLevel2> source, SormulaTestLevel2 row, boolean cascadesCompleted)
    {
        boolean keep = true;
        
        if (cascadesCompleted)
        {
            keep = row.getChildList().size() > 0; // keep non empty nodes
        }
        else
        {
            keep = row.getLevel2Id() > 211; // none of 211 or descendants
        }
        
        return keep;
    }
    
    
    public boolean accept(ScalarSelectOperation<SormulaTestLevel3> source, SormulaTestLevel3 row, boolean cascadesCompleted)
    {
        boolean keep = true;
        
        if (!cascadesCompleted)
        {
            keep = row.getLevel3Id() <= 3222; // some of 102 and none of 103
        }
        
        return keep;
    }
}