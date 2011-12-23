/* sormula - Simple object relational mapping
 * Copyright (C) 2011 Jeff Miller
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
 * Tests readonly cascade udpates for {@linkplain SormulaTestParentReadOnlyCascade}.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="cascade.roc.update", dependsOnGroups="cascade.roc.insert")
public class UpdateTestReadOnlyCascade extends DatabaseTest<SormulaTestParentReadOnlyCascade>
{
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        createTable(SormulaTestParentReadOnlyCascade.class, null);
    }
    
    
    @AfterClass
    public void tearDown() throws Exception
    {
        closeDatabase();
    }
    
    
    @Test
    public void updateOneToOne() throws SormulaException
    {
        begin();
        Table<SormulaTestParentReadOnlyCascade> parentTable = getTable();
        Table<SormulaTestChild1ReadOnlyCascade> child1Table = getDatabase().getTable(SormulaTestChild1ReadOnlyCascade.class);
        
        // insert 1 parent and 1 child to test (must do separately since cascade is readonly)
        SormulaTestParentReadOnlyCascade parent = new SormulaTestParentReadOnlyCascade(910, "parent roc 910");
        SormulaTestChild1ReadOnlyCascade child1 = new SormulaTestChild1ReadOnlyCascade(9910, "child roc of parent 910");
        parent.setChild(child1);
        parent.setChild1Id(child1.getId());
        assert parentTable.insert(parent) == 1: "1:1 test set up parent not inserted";
        assert child1Table.insert(child1) == 1: "1:1 test set up child not inserted";
        
        // test update on 1 to 1 relationship
        child1.setDescription(child1.getDescription() + " updated1 roc");
        assert parentTable.update(parent) == 1 : "1:1 parent was not updated";
        
        // select directly to test child was NOT updated
        SormulaTestChild1ReadOnlyCascade selectedChild1 = child1Table.select(child1.getId());
        assert selectedChild1 != null : "1:1 no child " + child1.getId();
        assert !selectedChild1.getDescription().equals(child1.getDescription()) :
            "1:1 child was updated using readonly cascade";
        
        commit();
    }
    
    
    @Test
    public void updateOneToManyList() throws SormulaException
    {
        begin();
        Table<SormulaTestParentReadOnlyCascade> parentTable = getTable();
        Table<SormulaTestChildNReadOnlyCascade> childNTable = getDatabase().getTable(SormulaTestChildNReadOnlyCascade.class);

        // insert 1 parent and 1 child to test (must do separately since cascade is readonly)
        SormulaTestParentReadOnlyCascade parent = new SormulaTestParentReadOnlyCascade(970, "parent roc 970");
        SormulaTestChildNReadOnlyCascade childN = new SormulaTestChildNReadOnlyCascade(9970, "child roc of parent 970");
        parent.add(childN);
        assert parentTable.insert(parent) == 1: "1:n test set up parent not inserted";
        assert childNTable.insert(childN) == 1: "1:n test set up child not inserted";
        
        // test update on 1 to n relationship        
        childN.setDescription(childN.getDescription() + " updatedN roc");
        assert parentTable.update(parent) == 1 : "1:n parent was not updated";
                
        // select directly to test that child was NOT updated
        SormulaTestChildNReadOnlyCascade selectedChildN = childNTable.select(childN.getId());
        assert selectedChildN != null : "1:n no child " + childN.getId();
        assert !selectedChildN.getDescription().equals(childN.getDescription()) : 
            "1:n child was updated using readonly cascade";
        
        commit();
    }
}
