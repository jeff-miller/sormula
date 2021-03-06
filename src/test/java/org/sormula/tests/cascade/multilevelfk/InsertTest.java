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

import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.operation.ScalarSelectOperation;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.Test;


/**
 * Tests multi-level cascades with foreign key ddl and foreign key annotations.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="cascade.insert")
public class InsertTest extends DatabaseTest<SormulaFkTestLevel1>
{
    @Override
    protected void open() throws Exception
    {
        super.open();
        
        // need to drop tables in proper order due to foreign key constraints
        dropTable(getSchemaPrefix() + SormulaFkTestLevel3.class.getSimpleName());
        dropTable(getSchemaPrefix() + SormulaFkTestLevel2.class.getSimpleName());
        dropTable(getSchemaPrefix() + SormulaFkTestLevel1.class.getSimpleName());
        
        String foreignKeyDdl = ""; // some db's have problems with foreign key constraints
        
        createTable(SormulaFkTestLevel1.class, 
            "CREATE TABLE " + getSchemaPrefix() + SormulaFkTestLevel1.class.getSimpleName() + " (" +
            " id INTEGER NOT NULL PRIMARY KEY," +
            " description VARCHAR(60)" +
            ")"
        );
        
        // create level 2 table
        DatabaseTest<SormulaFkTestLevel2> child2 = new DatabaseTest<>();
        child2.openDatabase();
        if (isForeignKey()) foreignKeyDdl = ", FOREIGN KEY (parentid) REFERENCES " + getSchemaPrefix() + SormulaFkTestLevel1.class.getSimpleName() +"(id)";
        child2.createTable(SormulaFkTestLevel2.class, 
                "CREATE TABLE " + getSchemaPrefix() + SormulaFkTestLevel2.class.getSimpleName() + " (" +
                " id INTEGER NOT NULL PRIMARY KEY," +
                " parentid INTEGER NOT NULL," +
                " description VARCHAR(60)" +
                foreignKeyDdl +
                ")"
            );
        child2.closeDatabase();
        
        // create level 3 table
        DatabaseTest<SormulaFkTestLevel3> child3 = new DatabaseTest<>();
        child3.openDatabase();
        if (isForeignKey()) foreignKeyDdl = ", FOREIGN KEY (parentid) REFERENCES " + getSchemaPrefix() + SormulaFkTestLevel2.class.getSimpleName() +"(id)";
        child3.createTable(SormulaFkTestLevel3.class, 
                "CREATE TABLE " + getSchemaPrefix() + SormulaFkTestLevel3.class.getSimpleName() + " (" +
                " id INTEGER NOT NULL PRIMARY KEY," +
                " parentid INTEGER NOT NULL," +
                " description VARCHAR(60)" +
                foreignKeyDdl +
                ")"
            );
        child3.closeDatabase();
    }
    
    
    @Test
    public void insertMultiLevel() throws SormulaException
    {
        begin();
        insertMultiLevel(101, 210, 3100);
        insertMultiLevel(102, 220, 3200);
        insertMultiLevel(103, 230, 3300);
        commit();
    }
    void insertMultiLevel(int level1Id, int level2BaseId, int level3BaseId) throws SormulaException
    {
    	// level 1 node
        SormulaFkTestLevel1 node1 = new SormulaFkTestLevel1(level1Id, "Insert parent " + level1Id);
        
        // level 2 nodes
        for (int i = 1; i <= 5; ++i)
        {
            SormulaFkTestLevel2 node2 = new SormulaFkTestLevel2(level2BaseId + i, "Child of parent " + node1.getId());
            node1.add(node2);
            
            // level 3 nodes
            for (int j = 1; j <= 3; ++j)
            {
                SormulaFkTestLevel3 node3 = new SormulaFkTestLevel3(level3BaseId + i*10 + j, "Child of parent " + node2.getId());
                node2.add(node3);
            }
        }
        
        // inserts all nodes via cascades
        assert getTable().insert(node1) == 1 : "insertMultiLevel did not insert level 1";
        
        // verify that all children were inserted
        Table<SormulaFkTestLevel2> child2Table = getDatabase().getTable(SormulaFkTestLevel2.class);
        Table<SormulaFkTestLevel3> child3Table = getDatabase().getTable(SormulaFkTestLevel3.class);
        
        try (ScalarSelectOperation<SormulaFkTestLevel2> select2 = new ScalarSelectOperation<>(child2Table);
             ScalarSelectOperation<SormulaFkTestLevel3> select3 = new ScalarSelectOperation<>(child3Table))
        {
            // test level 2 children
            for (SormulaFkTestLevel2 node2: node1.getChildList())
            {
                select2.setParameters(node2.getId());
                select2.execute();
                assert select2.readNext() != null : "level 2 child " + node2.getId() + " was not inserted"; 
                
                // test level 3 children
                for (SormulaFkTestLevel3 node3: node2.getChildList())
                {
                    select3.setParameters(node3.getId());
                    select3.execute();
                    assert select3.readNext() != null : "level 3 child " + node3.getId() + " was not inserted"; 
                }
            }
        }
    }
}
