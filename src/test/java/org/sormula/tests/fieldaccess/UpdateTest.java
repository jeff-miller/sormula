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
package org.sormula.tests.fieldaccess;

import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.annotation.Column;
import org.sormula.annotation.Row;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Update tests for {@link Row#fieldAccess()} and {@link Column#fieldAccess()}.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="fieldaccess.update", dependsOnGroups="fieldaccess.insert")
public class UpdateTest extends DatabaseTest<SormulaFaTestParent>
{
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        createTable(SormulaFaTestParent.class);
    }
    
    
    @AfterClass
    public void tearDown() throws Exception
    {
        closeDatabase();
    }
    
    
    @Test
    public void updateOneToManyList() throws SormulaException
    {
        begin();
        Table<SormulaFaTestParent> parentTable = getTable();
        Table<SormulaFaTestChild> childTable = getDatabase().getTable(SormulaFaTestChild.class);
        
        for (SormulaFaTestParent parent: parentTable.selectAll())
        {
            if (parent.getChildList().size() > 0)
            {
                // test update on 1 to many relationship via list
                for (SormulaFaTestChild c: parent.getChildList())
                {
                    c.description = c.description + " updated";
                }

                parentTable.update(parent);
                
                for (SormulaFaTestChild c: parent.getChildList())
                {
                    // select directly to test update
                    SormulaFaTestChild selectedChild = childTable.select(c.getChildId());
                    assert selectedChild != null : "1:n no child " + c.getChildId();
                    assert selectedChild.description.equals(c.description) : "child was not updated";
                }
            }
        }
        
        commit();
    }
}
