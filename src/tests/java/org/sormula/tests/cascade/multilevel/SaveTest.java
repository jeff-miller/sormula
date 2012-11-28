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

import java.util.List;

import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests save cascade for multi-level relationships.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="cascade.save", dependsOnGroups="cascade.insert")
public class SaveTest extends DatabaseTest<SormulaTestLevel1>
{
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        createTable(SormulaTestLevel1.class);
    }
    
    
    @AfterClass
    public void tearDown() throws Exception
    {
        closeDatabase();
    }
    
    
    @Test
    public void saveMultiLevelNewNode2() throws SormulaException
    {
        begin();
        Table<SormulaTestLevel1> table1 = getTable();
        Table<SormulaTestLevel2> table2 = getDatabase().getTable(SormulaTestLevel2.class);
        Table<SormulaTestLevel3> table3 = getDatabase().getTable(SormulaTestLevel3.class);
        
        // assumes node 101 exists from insert test
        SormulaTestLevel1 node1 = table1.select(101);
        
        // add a new node2 to node1 but keep node3 children
        // to test that node2 is inserted but node3 children are updated
        List<SormulaTestLevel2> node1Children = node1.getChildList(); 
        int lastNode2Index = node1Children.size() - 1;
        SormulaTestLevel2 node2 = node1Children.get(lastNode2Index); 
        SormulaTestLevel2 newNode2 = new SormulaTestLevel2(22222,
        		"new node2 to insert with children of " + node2.getId());
        node1.add(newNode2);
        
        // move node2 children to newNode2
        // should cause new record to be inserted but children are updated
        for (SormulaTestLevel3 node3: node2.getChildList()) newNode2.add(node3); // sets node3 parent id
        node2.setChildList(null); // children can have only one parent
        
        // save should update all nodes except it should insert new node2 22222
        table1.save(node1);
        
        // verify that node2 22222 was inserted
        SormulaTestLevel2 node2Test = table2.select(newNode2.getId());
        assert node2Test != null : "node2 " + newNode2.getId() + " was not inserted";
        assert node2Test.getParentId() == node1.getId() : "node2 " + newNode2.getId() + " wrong parent";
        
        // verify child node3 exist and have parent 22222
        for (SormulaTestLevel3 node3Test: node2Test.getChildList())
        {
        	SormulaTestLevel3 node3 = table3.select(node3Test.getId());
        	assert node3 != null && node3.getParentId() == newNode2.getId() : 
        		"node3 " + node3Test.getId() + " updated with wrong parent";
        }
        	
        commit();
    }
    
    
    @Test
    public void saveMultiLevelNewNode3() throws SormulaException
    {
        begin();
        Table<SormulaTestLevel1> table1 = getTable();
        Table<SormulaTestLevel3> table3 = getDatabase().getTable(SormulaTestLevel3.class);
        
        // assumes node 101 exists from insert test
        SormulaTestLevel1 node1 = table1.select(101);
        
        // add a new leaf node3 to node2 
        // to test that leaf node3 33333 is inserted but sibling nodes are updated
        SormulaTestLevel2 node2 = node1.getChildList().get(0);
        SormulaTestLevel3 node3 = new SormulaTestLevel3(33333, "new leaf to insert with save()");
        node2.add(node3);
        
        // save should update all nodes except it should insert new node3 33333
        table1.save(node1);
        
        // verify that node3 33333 was inserted
        SormulaTestLevel3 node3Test = table3.select(node3.getId());
        assert node3Test != null : "node3 " + node3.getId() + " was not inserted";
        assert node3Test.getParentId() == node2.getId() : "node3 " + node3.getId() + " wrong parent";
        
        commit();
    }
}
