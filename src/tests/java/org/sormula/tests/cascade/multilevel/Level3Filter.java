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
import org.sormula.operation.filter.SelectCascadeFilter;


/**
 * Basic {@link SelectCascadeFilter} to test filtering.
 * 
 * @author Jeff Miller
 */
public class Level3Filter implements SelectCascadeFilter<SormulaTestLevel3>
{
    public boolean accept(ScalarSelectOperation<SormulaTestLevel3> source, SormulaTestLevel3 row, boolean cascaded)
    {
        boolean keep = true;
        
        if (!cascaded)
        {
            keep = row.getLevel3Id() <= 3222; // some of 102 and none of 103
        }
        
        return keep;
    }
    
    
    public Class<SormulaTestLevel3> getRowClass()
    {
        return SormulaTestLevel3.class;
    }
}