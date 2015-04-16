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
package org.sormula.tests.fieldaccess;

import java.util.List;

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
@Test(singleThreaded=true, groups="fieldaccess.select", dependsOnGroups="fieldaccess.insert")
public class SelectTest extends DatabaseTest<SormulaFaTestParent>
{
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        createTable(SormulaFaTestParent.class);
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
        Table<SormulaFaTestChild> childTable = getDatabase().getTable(SormulaFaTestChild.class);
        
        // for each parent 
        for (SormulaFaTestParent parent : getTable().selectAll())
        {
            // verify 1 to many
            List<SormulaFaTestChild> children = parent.getChildList();
            int count = childTable.selectCount("byParent", parent.parentId); // tests non parameterized type of selectCount()
            if (children.size() > 0)
            {
                // verify all rows selected
                for (SormulaFaTestChild c: children)
                { 
                    assert c.parentId == parent.parentId : "child parent id != parent id";
                    assert c.setChildIdMethodInvoked : "setChildIdMethod was invoked instead of direct access";
                }
                assert count == children.size() : "wrong number of children read from cascade";
            }
            else 
            {
                // verify no child records
                assert count == 0 : "child rows were not read";
            }
        }
        
        commit();
    }
}
