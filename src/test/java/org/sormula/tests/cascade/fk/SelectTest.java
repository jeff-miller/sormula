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
package org.sormula.tests.cascade.fk;

import org.sormula.SormulaException;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests cascade selects with foreign key annotations.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="cascade.select", dependsOnGroups="cascade.insert")
public class SelectTest extends DatabaseTest<SormulaFKTestParent>
{
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        createTable(SormulaFKTestParent.class);
    }
    
    
    @AfterClass
    public void tearDown() throws Exception
    {
        closeDatabase();
    }

    
    @Test
    public void cascadeSelectList() throws SormulaException
    {
        begin();
        
        // for all parents
        for (SormulaFKTestParent parent : getTable().selectAll())
        {
            // verify that all children have correct foreign key information
            for (SormulaFKTestChildN c: parent.getChildList())
            {
                // foreignKeyValueFields=...
                assert c.getParentId() == parent.getParentId() : "child " + c.getId() + " has wrong parent id";
                
                // foreignKeyReferenceField=...
                SormulaFKTestParent testParent = c.getParent();
                assert testParent != null : "child " + c.getId() + " has no parent reference";
                assert testParent.getParentId() == parent.getParentId() : "child " + c.getId() + " has wrong parent reference";
            }
        }
        
        commit();
    }

    
    @Test
    public void cascadeSelectMap() throws SormulaException
    {
        begin();
        
        // for all parents
        for (SormulaFKTestParent parent : getTable().selectAll())
        {
            // verify that all children have correct foreign key information
            for (SormulaFKTestChildM c: parent.getChildMap().values())
            {
                // foreignKeyValueFields=...
                assert c.getParentId() == parent.getParentId() : "child " + c.getId() + " has wrong parent id";
                
                // foreignKeyReferenceField=...
                SormulaFKTestParent testParent = c.getSormulaFKTestParent();
                assert testParent != null : "child " + c.getId() + " has no parent reference";
                assert testParent.getParentId() == parent.getParentId() : "child " + c.getId() + " has wrong parent reference";
            }
        }
        
        commit();
    }
}
