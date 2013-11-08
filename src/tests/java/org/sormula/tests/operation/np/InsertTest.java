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
package org.sormula.tests.operation.np;

import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.operation.ScalarSelectOperation;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests named parameter selects.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="cascade.insert")
public class InsertTest extends DatabaseTest<SormulaTestNP1>
{
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        createTable(SormulaTestNP1.class, 
            "CREATE TABLE " + getSchemaPrefix() + SormulaTestNP1.class.getSimpleName() + " (" +
            " level1id INTEGER NOT NULL PRIMARY KEY," +
            " description VARCHAR(60)" +
            ")"
        );
        
        // create level 2 table
        DatabaseTest<SormulaTestNP2> child2 = new DatabaseTest<>();
        child2.openDatabase();
        child2.createTable(SormulaTestNP2.class, 
                "CREATE TABLE " + getSchemaPrefix() + SormulaTestNP2.class.getSimpleName() + " (" +
                " level2id INTEGER NOT NULL PRIMARY KEY," +
                " parentid INTEGER NOT NULL," +
                " description VARCHAR(60)" +
                ")"
            );
        child2.closeDatabase();
    }
    
    
    @AfterClass
    public void tearDown() throws Exception
    {
        closeDatabase();
    }
    
    
    
    @Test
    public void insertMultiLevel() throws SormulaException
    {
        begin();
        insertMultiLevel(101, 210, 3100);
        insertMultiLevel(102, 220, 3200);
        insertMultiLevel(104, 240, 3400);
        insertMultiLevel(103, 230, 3300);
        commit();
    }
    void insertMultiLevel(int level1Id, int level2BaseId, int level3BaseId) throws SormulaException
    {
    	// level 1 node
        SormulaTestNP1 node1 = new SormulaTestNP1(level1Id, "Insert parent " + level1Id);
        
        // level 2 nodes
        for (int i = 1; i <= 5; ++i)
        {
            SormulaTestNP2 node2 = new SormulaTestNP2(level2BaseId + i, "Child of parent " + node1.getLevel1Id());
            node1.add(node2);
        }
        
        // inserts all nodes via cascades
        assert getTable().insert(node1) == 1 : "insertMultiLevel did not insert level 1";
        
        // verify that all children were inserted
        Table<SormulaTestNP2> child2Table = getDatabase().getTable(SormulaTestNP2.class);
        ScalarSelectOperation<SormulaTestNP2> select2 = new ScalarSelectOperation<>(child2Table);
        
        // test level 2 children
        for (SormulaTestNP2 node2: node1.getChildList())
        {
            select2.setParameters(node2.getLevel2Id());
            select2.execute();
            assert select2.readNext() != null : "level 2 child " + node2.getLevel2Id() + " was not inserted"; 
        }
        
        select2.close();
    }
}
