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
 * Tests cascade deletes for {@link SormulaTestParent}.
 * Delete parents with odd number id (and cascades to children).
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="cascade.delete", dependsOnGroups="cascade.insert")
public class DeleteTest extends DatabaseTest<SormulaTestParent>
{
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        createTable(SormulaTestParent.class);
    }
    
    
    @AfterClass
    public void tearDown() throws Exception
    {
        closeDatabase();
    }
    
    
    public void deleteOneToOne() throws SormulaException
    {
        begin();
        Table<SormulaTestParent> parentTable = getTable();
        Table<SormulaTestChild1> child1Table = getDatabase().getTable(SormulaTestChild1.class);
        
        for (SormulaTestParent parent: parentTable.selectAll())
        {
            if (parent.getId() % 2 == 1)
            {
                SormulaTestChild1 child1 = parent.getChild();
                
                if (child1 != null)
                {
                    // test delete on 1 to 1 relationship
                    parentTable.delete(parent);
                    
                    // select directly to test delete
                    SormulaTestChild1 selectedChild1 = child1Table.select(child1.getChildId());
                    assert selectedChild1 == null : "1:1 child was not deleted";
                }
            }
        }
        
        commit();
    }
    
    
    @Test
    public void deleteOneToManyList() throws SormulaException
    {
        begin();
        Table<SormulaTestParent> parentTable = getTable();
        Table<SormulaTestChildN> childNTable = getDatabase().getTable(SormulaTestChildN.class);
        
        for (SormulaTestParent parent: parentTable.selectAll())
        {
            if (parent.getId() % 2 == 1 && parent.getChildList().size() > 0)
            {
                // test delete on 1 to many relationship via list
                parentTable.delete(parent);
                
                for (SormulaTestChildN c: parent.getChildList())
                {
                    // select directly to test delete
                    SormulaTestChildN selectedChildN = childNTable.select(c.getId());
                    assert selectedChildN == null : "1:n child was not deleted";
                }
            }
        }
        
        commit();
    }
    

    @Test
    public void deleteOneToManyMap() throws SormulaException
    {
        begin();
        Table<SormulaTestParent> parentTable = getTable();
        Table<SormulaTestChildM> childMTable = getDatabase().getTable(SormulaTestChildM.class);
        
        for (SormulaTestParent parent: parentTable.selectAll())
        {
            if (parent.getId() % 2 == 1 && parent.getChildMap().size() > 0)
            {
                // test delete on 1 to many relationship via map
                parentTable.delete(parent);
                
                for (SormulaTestChildM c: parent.getChildMap().values())
                {
                    // select directly to test delete
                    SormulaTestChildM selectedChildM = childMTable.select(c.getId());
                    assert selectedChildM == null : "map: child was not deleted";
                }
            }
        }
        
        commit();
    }
}
