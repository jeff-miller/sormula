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

import java.util.List;
import java.util.Map;

import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests cascade select annotations.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="cascade.select", dependsOnGroups="cascade.insert")
public class SelectTest extends DatabaseTest<SormulaTestParent>
{
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        createTable(SormulaTestParent.class, null);
    }
    
    
    @AfterClass
    public void tearDown() throws Exception
    {
        closeDatabase();
    }

    
    @Test
    public void cascadeSelect() throws SormulaException
    {
        begin();
        Table<SormulaTestChild1> child1Table = getDatabase().getTable(SormulaTestChild1.class);
        Table<SormulaTestChildN> childNTable = getDatabase().getTable(SormulaTestChildN.class);
        Table<SormulaTestChildM> childMTable = getDatabase().getTable(SormulaTestChildM.class);
        
        // for each parent 
        for (SormulaTestParent parent : getTable().selectAll())
        {
            // verify 1 to 1
            SormulaTestChild1 child = parent.getChild();
            
            if (child != null)
            {
                // verify child was selected
                assert child.getId() == parent.getChild1Id() : "1:1 child id != parent child1 id";
                assert child1Table.select(parent.getChild1Id()) != null : "1:1 child cacasade error";
            }
            else
            {
                // verify no child in db
                assert child1Table.select(parent.getChild1Id()) == null : "1:1 child row not read";
            }
            
            
            // verify 1 to many
            List<SormulaTestChildN> children = parent.getChildList();
            int countN = childNTable.selectCount("byParent", parent.getId());
            if (children.size() > 0)
            {
                // verify all rows selected
                for (SormulaTestChildN c: children)
                { 
                    assert c.getParentId() == parent.getId() : "1:n child parent id != parent id";
                }
                assert countN == children.size() : "1:n wrong number of children read from cascade";
            }
            else 
            {
                // verify no child records
                assert countN == 0 : "1:n child rows were not read";
            }
            

            // verify map select
            Map<Integer, SormulaTestChildM> map = parent.getChildMap();
            int countM = childMTable.selectCount("byParent", parent.getId());
            
            if (map.size() > 0)
            {
                // verify all rows selected
                for (SormulaTestChildM c: map.values())
                { 
                    c = map.get(c.getId()); // verifies map key is correct
                    assert c != null : "map: get child from map failed";
                    assert c.getParentId() == parent.getId() : "map: child parent id != parent id";
                }
                assert countM == map.size() : "map: wrong number of children read from cascade";
            }
            else 
            {
                // verify no child records
                assert countM == 0 : "map: child rows were not read";
            }
            
            
            // verify 1 to many using aggregate package
            countN = childNTable.<Integer>selectCount("parentId", "byParent", parent.getId());
            if (children.size() > 0)
            {
                // verify all rows selected
                for (SormulaTestChildN c: children)
                { 
                    assert c.getParentId() == parent.getId() : "1:n child parent id != parent id";
                }
                assert countN == children.size() : "1:n wrong number of children read from cascade";
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
