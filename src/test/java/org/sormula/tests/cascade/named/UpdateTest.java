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
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.Test;


/**
 * Tests multi-level named cascade updates.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="cascade.update", dependsOnGroups="cascade.insert")
public class UpdateTest extends DatabaseTest<SormulaNCTestLevel1>
{
    @Override
    protected void open() throws Exception
    {
        super.open();
        createTable(SormulaNCTestLevel1.class); 
    }
    
    
    @Test
    public void updateNamedCascades() throws SormulaException
    {
        begin();
        updateNamedCascades(102, "1-to-2", "2-to-3");
        updateNamedCascades(104, "*"); // 104 has thing1 and thing2, test that they are updated
        commit();
    }
    
    
    void updateNamedCascades(int level1Id, String...requiredCascades) throws SormulaException
    {
        Table<SormulaNCTestLevel1> table1 = getDatabase().getTable(SormulaNCTestLevel1.class);
        table1.setRequiredCascades(requiredCascades); // use specific cascades

        SormulaNCTestLevel1 node1Updated = table1.select(level1Id); // from insert test
        
        // update children
        SormulaNCTestLevel2 node2Updated = node1Updated.getChildList().get(0);
        node2Updated.setDescription("updated:" + node2Updated.getDescription());
        
        SormulaNCTestLevel3 node3Updated = node2Updated.getChildList().get(0);
        node3Updated.setDescription("updated:" + node3Updated.getDescription());
        
        SormulaNCThing thing1Updated = node1Updated.getThing1();
        if (thing1Updated != null) thing1Updated.setDescription("updated:" + thing1Updated.getDescription());
        
        SormulaNCThing thing2Updated = node1Updated.getThing2();
        if (thing2Updated != null) thing2Updated.setDescription("updated:" + thing2Updated.getDescription());
        
        SormulaNCThing thing3Updated = node1Updated.getThing3();
        if (thing3Updated != null) thing3Updated.setDescription("updated:" + thing3Updated.getDescription());
        
        table1.update(node1Updated);
        
        // confirm updates
        Table<SormulaNCTestLevel2> table2 = getDatabase().getTable(SormulaNCTestLevel2.class);
        SormulaNCTestLevel2 node2 = table2.select(node2Updated.getId());
        assert node2.getDescription().equals(node2Updated.getDescription()) : "node2 was not updated";
       
        Table<SormulaNCTestLevel3> table3 = getDatabase().getTable(SormulaNCTestLevel3.class);
        SormulaNCTestLevel3 node3 = table3.select(node3Updated.getId());
        assert node3.getDescription().equals(node3Updated.getDescription()) : "node3 was not updated";
        
        Table<SormulaNCThing> thingTable = getDatabase().getTable(SormulaNCThing.class);
        if (requiredCascades.length == 1 && requiredCascades[0].equals("*")) // wildcard means all cascades should be used
        {
            // confirm that cascade for thing1 WAS used
            assert thingTable.select(thing1Updated.getId()).getDescription().equals(thing1Updated.getDescription()) : 
                "thing1 " + thing1Updated.getId() + " was not updated"; 
            
            // confirm that cascade for thing2 WAS used
            assert thingTable.select(thing2Updated.getId()).getDescription().equals(thing2Updated.getDescription()) : 
                "thing2 " + thing2Updated.getId() + " was not updated"; 
        }
        
        // confirm that cascade for thing3 WAS ALWAYS used
        assert thingTable.select(thing3Updated.getId()).getDescription().equals(thing3Updated.getDescription()) : 
            "thing3 " + thing3Updated.getId() + " was not updated"; 
    }
}
