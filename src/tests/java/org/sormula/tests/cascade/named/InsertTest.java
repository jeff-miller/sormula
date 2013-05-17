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
package org.sormula.tests.cascade.named;

import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.operation.ScalarSelectOperation;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests multi-level named cascade inserts.
 * <p>
 * SelectTest was not written since {@link UpdateTest} and {@link DeleteTest} perform selects for named
 * cascades and other tests perform unnamed cascadeds.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="cascade.insert")
public class InsertTest extends DatabaseTest<SormulaNCTestLevel1>
{
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        
        // need to drop tables in proper order due to foreign key constraints
        dropTable(getSchemaPrefix() + SormulaNCTestLevel3.class.getSimpleName());
        dropTable(getSchemaPrefix() + SormulaNCTestLevel2.class.getSimpleName());
        dropTable(getSchemaPrefix() + SormulaNCTestLevel1.class.getSimpleName());
        
        String foreignKeyDdl = ""; // some db's have problems with foreign key constraints
        
        createTable(SormulaNCTestLevel1.class, 
            "CREATE TABLE " + getSchemaPrefix() + SormulaNCTestLevel1.class.getSimpleName() + " (" +
            " id INTEGER NOT NULL PRIMARY KEY," +
            " description VARCHAR(60)," +
            " thing1id INTEGER," + // tests named cascade
            " thing2id INTEGER," + // tests unnamed cascade
            " thing3id INTEGER"  + // tests wildcard cascade
            ")"
        );
        
        // create level 2 table
        DatabaseTest<SormulaNCTestLevel2> child2 = new DatabaseTest<SormulaNCTestLevel2>();
        child2.openDatabase();
        if (isForeignKey()) foreignKeyDdl = ", FOREIGN KEY (parentid) REFERENCES " + getSchemaPrefix() + SormulaNCTestLevel1.class.getSimpleName() +"(id)";
        child2.createTable(SormulaNCTestLevel2.class, 
                "CREATE TABLE " + getSchemaPrefix() + SormulaNCTestLevel2.class.getSimpleName() + " (" +
                " id INTEGER NOT NULL PRIMARY KEY," +
                " parentid INTEGER NOT NULL," +
                " description VARCHAR(60)" +
                foreignKeyDdl +
                ")"
            );
        child2.closeDatabase();
        
        // create level 3 table
        DatabaseTest<SormulaNCTestLevel3> child3 = new DatabaseTest<SormulaNCTestLevel3>();
        child3.openDatabase();
        if (isForeignKey()) foreignKeyDdl = ", FOREIGN KEY (parentid) REFERENCES " + getSchemaPrefix() + SormulaNCTestLevel2.class.getSimpleName() +"(id)";
        child3.createTable(SormulaNCTestLevel3.class, 
                "CREATE TABLE " + getSchemaPrefix() + SormulaNCTestLevel3.class.getSimpleName() + " (" +
                " id INTEGER NOT NULL PRIMARY KEY," +
                " parentid INTEGER NOT NULL," +
                " description VARCHAR(60)" +
                foreignKeyDdl +
                ")"
            );
        child3.closeDatabase();
        
        // thing table
        DatabaseTest<SormulaNCThing> thing = new DatabaseTest<SormulaNCThing>();
        thing.openDatabase();
        thing.createTable(SormulaNCThing.class, 
                "CREATE TABLE " + getSchemaPrefix() + SormulaNCThing.class.getSimpleName() + " (" +
                " id INTEGER NOT NULL PRIMARY KEY," +
                " description VARCHAR(60)" +
                ")"
            );
        thing.closeDatabase();
    }
    
    
    @AfterClass
    public void tearDown() throws Exception
    {
        closeDatabase();
    }
    
    
    @Test
    public void insertNamedCascades() throws SormulaException
    {
        begin();
        insertNamedCascades(101, 210, 3100, "1-to-2", "2-to-3"); // will be deleted by DeleteTest
        insertNamedCascades(102, 220, 3200, "1-to-2", "2-to-3");
        insertNamedCascades(103, 230, 3300, "*"); // will be deleted by DeleteTest
        insertNamedCascades(104, 240, 3400, "*");
        commit();
    }
    void insertNamedCascades(int level1Id, int level2BaseId, int level3BaseId, String...requiredCascades) throws SormulaException
    {
    	// level 1 node
        SormulaNCTestLevel1 node1 = new SormulaNCTestLevel1(level1Id, "Insert parent " + level1Id);
        
        // level 2 nodes
        for (int i = 1; i <= 5; ++i)
        {
            SormulaNCTestLevel2 node2 = new SormulaNCTestLevel2(level2BaseId + i, "Child of parent " + node1.getId());
            node1.add(node2);
            
            // level 3 nodes
            for (int j = 1; j <= 3; ++j)
            {
                SormulaNCTestLevel3 node3 = new SormulaNCTestLevel3(level3BaseId + i*10 + j, "Child of parent " + node2.getId());
                node2.add(node3);
            }
        }
        
        SormulaNCThing thing1 = new SormulaNCThing(10000 + level1Id, "thing1");
        node1.setThing1(thing1);
        
        SormulaNCThing thing2 = new SormulaNCThing(20000 + level1Id, "thing2");
        node1.setThing2(thing2);
        
        SormulaNCThing thing3 = new SormulaNCThing(30000 + level1Id, "thing3");
        node1.setThing3(thing3);
        
        // inserts all nodes via cascades
        Table<SormulaNCTestLevel1> table1 = getDatabase().getTable(SormulaNCTestLevel1.class);
        table1.setRequiredCascades(requiredCascades); // use specific cascades
        assert table1.insert(node1) == 1 : "insertNamedCascades did not insert level 1";
        
        // verify that all children were inserted
        Table<SormulaNCTestLevel2> table2 = getDatabase().getTable(SormulaNCTestLevel2.class);
        Table<SormulaNCTestLevel3> table3 = getDatabase().getTable(SormulaNCTestLevel3.class);
        Table<SormulaNCThing> thingTable = getDatabase().getTable(SormulaNCThing.class);
        ScalarSelectOperation<SormulaNCTestLevel2> select2 = new ScalarSelectOperation<SormulaNCTestLevel2>(table2);
        ScalarSelectOperation<SormulaNCTestLevel3> select3 = new ScalarSelectOperation<SormulaNCTestLevel3>(table3);
        
        // test level 2 children
        for (SormulaNCTestLevel2 node2: node1.getChildList())
        {
            select2.setParameters(node2.getId());
            select2.execute();
            assert select2.readNext() != null : "level 2 child " + node2.getId() + " was not inserted"; 
            
            // test level 3 children
            for (SormulaNCTestLevel3 node3: node2.getChildList())
            {
                select3.setParameters(node3.getId());
                select3.execute();
                assert select3.readNext() != null : "level 3 child " + node3.getId() + " was not inserted"; 
            }
        }
        
        select2.close();
        select3.close();
        
        if (requiredCascades.length == 1 && requiredCascades[0].equals("*")) // wildcard means all cascades should be used
        {
            // confirm that cascade for thing1 WAS used
            assert thingTable.select(thing1.getId()) != null : "thing1 " + thing1.getId() + " was not inserted "; 
            
            // confirm that cascade for thing2 WAS used
            assert thingTable.select(thing2.getId()) != null : "thing2 " + thing2.getId() + " was not inserted "; 
        }
        else
        {
            // confirm that cascade for thing1 WAS NOT used
            assert thingTable.select(thing1.getId()) == null : "thing1 " + thing1.getId() + " was inserted"; 
            
            // confirm that cascade for thing2 NOT used
            assert thingTable.select(thing2.getId()) == null : "thing2 " + thing2.getId() + " was inserted"; 
        }
        
        // confirm that cascade for thing3 WAS ALWAYS used regardless of required cascades
        assert thingTable.select(thing3.getId()) != null : "thing3 " + thing3.getId() + " was not inserted";
    }
}
