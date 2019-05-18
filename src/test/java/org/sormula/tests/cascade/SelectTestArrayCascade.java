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
package org.sormula.tests.cascade;

import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.Test;


/**
 * Tests cascade select annotations for array.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="cascade.ac.select", dependsOnGroups="cascade.ac.insert")
public class SelectTestArrayCascade extends DatabaseTest<SormulaTestParentArrayCascade>
{
    @Override
    protected void open() throws Exception
    {
        super.open();
        createTable(SormulaTestParentArrayCascade.class);
    }

    
    @Test
    public void cascadeSelectArray() throws SormulaException
    {
        begin();
        Table<SormulaTestChildNArrayCascade> childNTable = getDatabase().getTable(SormulaTestChildNArrayCascade.class);
        
        // for each parent 
        for (SormulaTestParentArrayCascade parent : getTable().selectAll())
        {
            // verify 1 to many from list to array
            SormulaTestChildNArrayCascade[] children = parent.getChildren();
            Integer countN = childNTable.<Integer>selectCount("parentId", "byParent", parent.getParentId());
            if (children.length > 0)
            {
                // verify all rows selected
                for (SormulaTestChildNArrayCascade c: children)
                { 
                    assert c.getParentId() == parent.getParentId() : "1:n child parent id != parent id";
                }
                assert countN == children.length : "1:n wrong number of children read from list cascade";
            }
            else 
            {
                // verify no child records
                assert countN == 0 : "1:n child rows were not read";
            }
            
            // verify 1 to many from map to array
            SormulaTestChildNArrayCascade[] children2 = parent.getChildren2();
            if (children2.length > 0)
            {
                // verify all rows selected
                for (SormulaTestChildNArrayCascade c: children2)
                { 
                    assert c.getParentId() == parent.getParentId() : "1:n child parent id != parent id";
                }
                assert countN == children2.length : "1:n wrong number of children read from map cascade";
            }
            else 
            {
                // verify no child records
                assert countN == 0 : "1:n child rows were not read";
            }
        }
        
        commit();
    }
}
