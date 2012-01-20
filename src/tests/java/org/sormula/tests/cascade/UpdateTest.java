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
package org.sormula.tests.cascade;

import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests cascade udpates for {@link SormulaTestParent}.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="cascade.update", dependsOnGroups="cascade.insert")
public class UpdateTest extends DatabaseTest<SormulaTestParent>
{
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        createTable(SormulaTestParent.class, null);
    }
    
    
    @AfterClass
    public void tearDown() throws Exception
    {
        closeDatabase();
    }
    
    
    public void updateOneToOne() throws SormulaException
    {
        begin();
        Table<SormulaTestParent> parentTable = getTable();
        Table<SormulaTestChild1> child1Table = getDatabase().getTable(SormulaTestChild1.class);
        
        for (SormulaTestParent parent: parentTable.selectAll())
        {
            SormulaTestChild1 child1 = parent.getChild();
            
            if (child1 != null)
            {
                // test update on 1 to 1 relationship
                child1.setDescription(child1.getDescription() + " updated1");
                parentTable.update(parent);
                
                // select directly to test update
                SormulaTestChild1 selectedChild1 = child1Table.select(child1.getId());
                assert selectedChild1 != null && selectedChild1.getDescription().equals(child1.getDescription()) :
                    "1:1 child was not updated";
            }
        }
        
        commit();
    }
    
    
    @Test
    public void updateOneToManyList() throws SormulaException
    {
        begin();
        Table<SormulaTestParent> parentTable = getTable();
        Table<SormulaTestChildN> childNTable = getDatabase().getTable(SormulaTestChildN.class);
        
        for (SormulaTestParent parent: parentTable.selectAll())
        {
            if (parent.getChildList().size() > 0)
            {
                // test update on 1 to many relationship via list
                for (SormulaTestChildN c: parent.getChildList())
                {
                    c.setDescription(c.getDescription() + " updatedN");
                }

                parentTable.update(parent);
                
                for (SormulaTestChildN c: parent.getChildList())
                {
                    // select directly to test update
                    SormulaTestChildN selectedChildN = childNTable.select(c.getId());
                    assert selectedChildN != null : "1:n no child " + c.getId();
                    assert selectedChildN.getDescription().equals(c.getDescription()) : "1:n child was not updated";
                }
            }
        }
        
        commit();
    }
    
    
    @Test
    public void updateOneToManyMap() throws SormulaException
    {
        begin();
        Table<SormulaTestParent> parentTable = getTable();
        Table<SormulaTestChildM> childMTable = getDatabase().getTable(SormulaTestChildM.class);
        
        for (SormulaTestParent parent: parentTable.selectAll())
        {
            if (parent.getChildMap().size() > 0)
            {
                // test update on 1 to many relationship via map
                for (SormulaTestChildM c: parent.getChildMap().values())
                {
                    c.setDescription(c.getDescription() + " updatedM");
                }

                parentTable.update(parent);
                
                for (SormulaTestChildM c: parent.getChildMap().values())
                {
                    // select directly to test update
                    SormulaTestChildM selectedChildM = childMTable.select(c.getId());
                    assert selectedChildM != null : "map: no child " + c.getId();
                    assert selectedChildM.getDescription().equals(c.getDescription()) : "map: child was not updated";
                }
            }
        }
        
        commit();
    }
}
