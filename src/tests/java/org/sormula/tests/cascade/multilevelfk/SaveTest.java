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
package org.sormula.tests.cascade.multilevelfk;

import java.util.List;

import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests save cascade for multi-level relationships with foreign key ddl and foreign key annotations.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="cascade.save", dependsOnGroups="cascade.insert")
public class SaveTest extends DatabaseTest<SormulaFkTestLevel1>
{
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        createTable(SormulaFkTestLevel1.class);
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
        Table<SormulaFkTestLevel1> table1 = getTable();
        Table<SormulaFkTestLevel2> table2 = getDatabase().getTable(SormulaFkTestLevel2.class);
        Table<SormulaFkTestLevel3> table3 = getDatabase().getTable(SormulaFkTestLevel3.class);
        
        // assumes node 101 exists from insert test
        SormulaFkTestLevel1 node1 = table1.select(101);
        
        // add a new node2 to node1 but keep node3 children
        // to test that node2 is inserted but node3 children are updated
        List<SormulaFkTestLevel2> node1Children = node1.getChildList(); 
        int lastNode2Index = node1Children.size() - 1;
        SormulaFkTestLevel2 node2 = node1Children.get(lastNode2Index); 
        SormulaFkTestLevel2 newNode2 = new SormulaFkTestLevel2(22222,
        		"new node2 to insert with children of " + node2.getId());
        node1.add(newNode2);
        
        // move node2 children to newNode2
        // should cause new record to be inserted but children are updated
        for (SormulaFkTestLevel3 node3: node2.getChildList()) newNode2.add(node3); // sets node3 parent id
        node2.setChildList(null); // children can have only one parent
        
        // save should update all nodes except it should insert new node2 22222
        table1.save(node1);
        
        // verify that node2 22222 was inserted
        SormulaFkTestLevel2 node2Test = table2.select(newNode2.getId());
        assert node2Test != null : "node2 " + newNode2.getId() + " was not inserted";
        assert node2Test.getParentId() == node1.getId() : "node2 " + newNode2.getId() + " wrong parent";
        
        // verify child node3 exist and have parent 22222
        for (SormulaFkTestLevel3 node3Test: node2Test.getChildList())
        {
        	SormulaFkTestLevel3 node3 = table3.select(node3Test.getId());
        	assert node3 != null && node3.getParentId() == newNode2.getId() : 
        		"node3 " + node3Test.getId() + " updated with wrong parent";
        }
        	
        commit();
    }
    
    
    @Test
    public void saveMultiLevelNewNode3() throws SormulaException
    {
        begin();
        Table<SormulaFkTestLevel1> table1 = getTable();
        Table<SormulaFkTestLevel3> table3 = getDatabase().getTable(SormulaFkTestLevel3.class);
        
        // assumes node 101 exists from insert test
        SormulaFkTestLevel1 node1 = table1.select(101);
        
        // add a new leaf node3 to node2 
        // to test that leaf node3 33333 is inserted but sibling nodes are updated
        SormulaFkTestLevel2 node2 = node1.getChildList().get(0);
        SormulaFkTestLevel3 node3 = new SormulaFkTestLevel3(33333, "new leaf to insert with save()");
        node2.add(node3);
        
        // save should update all nodes except it should insert new node3 33333
        table1.save(node1);
        
        // verify that node3 33333 was inserted
        SormulaFkTestLevel3 node3Test = table3.select(node3.getId());
        assert node3Test != null : "node3 " + node3.getId() + " was not inserted";
        assert node3Test.getParentId() == node2.getId() : "node3 " + node3.getId() + " wrong parent";
        
        commit();
    }

    
    @Test
    public void saveAllNewNodes() throws SormulaException
    {
        // test save cascade when all rows are new
        begin();
        Table<SormulaFkTestLevel1> table1 = getTable();
        Table<SormulaFkTestLevel2> table2 = getDatabase().getTable(SormulaFkTestLevel2.class);
        Table<SormulaFkTestLevel3> table3 = getDatabase().getTable(SormulaFkTestLevel3.class);

        SormulaFkTestLevel1 node1 = new SormulaFkTestLevel1(401, "Save parent 401");
        SormulaFkTestLevel2 node2 = new SormulaFkTestLevel2(444, "Save middle node 444");
        node1.add(node2);
        SormulaFkTestLevel3 node3 = new SormulaFkTestLevel3(4443, "Save leaf node 4443");
        node2.add(node3);

        // should insert all 3 nodes
        table1.save(node1);

        // confirm node1 was inserted
        SormulaFkTestLevel1 node1Test = table1.select(node1.getId());
        assert node1Test != null : " node1 " + node1.getId() + " was not saved";
        
        // confirm node2 was inserted
        SormulaFkTestLevel2 node2Test = table2.select(node2.getId());
        assert node2Test != null : " node2 " + node2.getId() + " was not saved";
        
        // confirm node3 was inserted
        SormulaFkTestLevel3 node3Test = table3.select(node3.getId());
        assert node3Test != null : " node3 " + node3.getId() + " was not saved";
        
        // confirm all nodes selected from cascade
        SormulaFkTestLevel2 node1Child = node1Test.getChildList().get(0);
        assert node1Child != null && node1Child.getId() == node2.getId() : " node2 not selected";
        SormulaFkTestLevel3 node2Child = node2Test.getChildList().get(0);
        assert node2Child != null && node2Child.getId() == node3.getId() : " node3 not selected";
        
        commit();
    }
}
