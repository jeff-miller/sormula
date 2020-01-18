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
import org.testng.annotations.Test;


/**
 * Tests multi-level named cascade deletes.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="cascade.delete", dependsOnGroups="cascade.insert")
public class DeleteTest extends DatabaseTest<SormulaNCTestLevel1>
{
    @Override
    protected void open() throws Exception
    {
        super.open();
        createTable(SormulaNCTestLevel1.class); 
    }

    
    @Test
    public void deleteNamedCascades() throws SormulaException
    {
        begin();
        deleteNamedCascades(101, "1-to-2", "2-to-3");
        deleteNamedCascades(103, "*"); // 103 has thing1 and thing2, test that they are deleted
        commit();
    }
    
    
    void deleteNamedCascades(int level1Id, String...requiredCascades) throws SormulaException
    {
        Table<SormulaNCTestLevel1> table1 = getDatabase().getTable(SormulaNCTestLevel1.class);
        table1.setRequiredCascades(requiredCascades); // use specific cascades

        SormulaNCTestLevel1 node1 = table1.select(level1Id); // from insert test
        
        // delete root node and all children for cascaded names
        table1.delete(node1);
        
        // confirm deletes
        assert table1.select(level1Id) == null : "root node was not deleted " + level1Id; 

        // verify that all children were deleted
        Table<SormulaNCTestLevel2> table2 = getDatabase().getTable(SormulaNCTestLevel2.class);
        Table<SormulaNCTestLevel3> table3 = getDatabase().getTable(SormulaNCTestLevel3.class);
        Table<SormulaNCThing> thingTable = getDatabase().getTable(SormulaNCThing.class);
        
        try (ScalarSelectOperation<SormulaNCTestLevel2> select2 = new ScalarSelectOperation<>(table2);
             ScalarSelectOperation<SormulaNCTestLevel3> select3 = new ScalarSelectOperation<>(table3))
        {
            // test level 2 children
            for (SormulaNCTestLevel2 node2: node1.getChildList())
            {
                select2.setParameters(node2.getId());
                select2.execute();
                assert select2.readNext() == null : "level 2 child " + node2.getId() + " was not deleted"; 
                
                // test level 3 children
                for (SormulaNCTestLevel3 node3: node2.getChildList())
                {
                    select3.setParameters(node3.getId());
                    select3.execute();
                    assert select3.readNext() == null : "level 3 child " + node3.getId() + " was not deleted"; 
                }
            }
        }
        
        if (requiredCascades.length == 1 && requiredCascades[0].equals("*")) // wildcard means all cascades should be used
        {
            // confirm that cascade for thing1 WAS deleted
            assert thingTable.select(node1.getThing1Id()) == null : "thing1 " + node1.getThing1Id() + " was not deleted"; 
            
            // confirm that cascade for thing2 WAS deleted
            assert thingTable.select(node1.getThing2Id()) == null : "thing2 " + node1.getThing2Id() + " was not deleted"; 
        }
        
        // confirm that cascade for thing3 WAS ALWAYS deleted
        assert thingTable.select(node1.getThing3Id()) == null : "thing3 " + node1.getThing3Id() + " was not deleted"; 
    }
}
