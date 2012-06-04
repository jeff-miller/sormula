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
package org.sormula.tests.cascade.lazy;

import java.util.Map;

import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests lazy cascade select annotations for {@link SormulaTestParentLazy1}.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="cascade.select", dependsOnGroups="cascade.insert")
public class SelectTest1 extends DatabaseTest<SormulaTestParentLazy1>
{
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        createTable(SormulaTestParentLazy1.class);
    }
    
    
    @AfterClass
    public void tearDown() throws Exception
    {
        closeDatabase();
    }

    
    @Test
    public void cascadeSelectLazy1() throws SormulaException
    {
        begin();
        Table<SormulaTestChildLazy> childTable = getDatabase().getTable(SormulaTestChildLazy.class);
        
        // for each parent 
        for (SormulaTestParentLazy1 parent : getTable().selectAll())
        {
            // verify map select
            assert parent.childMap == null : "Lazy map: children were selected prematurely";
            Map<Integer, SormulaTestChildLazy> map = parent.getChildMap(); // child map is selected here since it is lazy
            assert map != null : "Lazy map: children were not selected";
            assert map == parent.getChildMap() : "Lazy map: children were selected twice";
            int count = childTable.selectCount("byParent", parent.getId());
            
            if (map.size() > 0)
            {
                // verify all rows selected
                for (SormulaTestChildLazy c: map.values())
                { 
                    c = map.get(c.getId()); // verifies map key is correct
                    assert c != null : "Lazy map: get child from map failed";
                    assert c.getParentId() == parent.getId() : "Lazy map: child parent id != parent id";
                }
                assert count == map.size() : "Lazy map: wrong number of children read from cascade";
            }
            else 
            {
                // verify no child records
                assert count == 0 : "Lazy map: child rows were not read";
            }
        }
        
        commit();
    }
}
