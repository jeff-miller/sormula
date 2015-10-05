/* sormula - Simple object relational mapping
 * Copyright (C) 2015 Jeff Miller
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

import java.util.function.BiPredicate;


/**
 * {@link BiPredicate} that filters all row class types. Instead of a {@link BiPredicate} for each
 * row type, there is one method for each row type. The filtering conditions are arbitrary and useful
 * only as an example. This class is used by {@link SelectTestLambdaFilter}.
 * 
 * @author Jeff Miller
 */
public class AllLevelsFilterPredicate implements BiPredicate<Object, Boolean>
{
    public boolean test(Object row, Boolean cascadesCompleted)
    {
        if (row instanceof SormulaTestLevel1) return test1((SormulaTestLevel1)row, cascadesCompleted);
        if (row instanceof SormulaTestLevel2) return test2((SormulaTestLevel2)row, cascadesCompleted);
        if (row instanceof SormulaTestLevel3) return test3((SormulaTestLevel3)row, cascadesCompleted);
        
        // unknown type, always accept
        return true;
    }


    public boolean test1(SormulaTestLevel1 row, boolean cascadesCompleted)
    {
        boolean keep = true;
        if (cascadesCompleted) keep = row.getChildList().size() > 0; // keep non empty nodes
        else                   keep = row.getLevel1Id() != 104; // none of 104 or descendants
        return keep;
    }
    
    
    public boolean test2(SormulaTestLevel2 row, boolean cascadesCompleted)
    {
        boolean keep = true;
        if (cascadesCompleted) keep = row.getChildList().size() > 0; // keep non empty nodes
        else                   keep = row.getLevel2Id() > 211; // none of 211 or descendants
        return keep;
    }
    
    
    public boolean test3(SormulaTestLevel3 row, boolean cascadesCompleted)
    {
        boolean keep = true;
        if (!cascadesCompleted) keep = row.getLevel3Id() <= 3222; // some of 102 and none of 103
        return keep;
    }
}