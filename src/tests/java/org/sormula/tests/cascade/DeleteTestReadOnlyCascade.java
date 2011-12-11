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
 * Tests readonly cascade deletes for {@linkplain SormulaTestParentReadOnlyCascade}.
 * Delete parents with odd number id (and cascades to children).
 * 
 * @author Jeff Miller
 */
@Test(groups="cascade.roc.delete", dependsOnGroups="cascade.roc.insert")
public class DeleteTestReadOnlyCascade extends DatabaseTest<SormulaTestParentReadOnlyCascade>
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
    public void deleteOneToOne() throws SormulaException
    {
        begin();
        Table<SormulaTestParentReadOnlyCascade> parentTable = getTable();
        Table<SormulaTestChild1ReadOnlyCascade> child1Table = getDatabase().getTable(SormulaTestChild1ReadOnlyCascade.class);
        
        // insert 1 parent and 1 child to test (must do separately since cascade is readonly)
        SormulaTestParentReadOnlyCascade parent = new SormulaTestParentReadOnlyCascade(920, "parent roc 920");
        SormulaTestChild1ReadOnlyCascade child1 = new SormulaTestChild1ReadOnlyCascade(9920, "child roc of parent 920");
        parent.setChild(child1);
        parent.setChild1Id(child1.getId());
        assert parentTable.insert(parent) == 1: "1:1 test set up parent not inserted";
        assert child1Table.insert(child1) == 1: "1:1 test set up child not inserted";
        
        // test delete on 1 to 1 relationship
        assert parentTable.delete(parent) == 1 : "1:1 parent was not deleted";
        
        // select directly to test that child was NOT deleted
        SormulaTestChild1ReadOnlyCascade selectedChild1 = child1Table.select(child1.getId());
        assert selectedChild1 != null : "1:1 child was deleted using readonly cascade";
        
        commit();
    }
    
    
    @Test
    public void deleteOneToManyList() throws SormulaException
    {
        begin();
        Table<SormulaTestParentReadOnlyCascade> parentTable = getTable();
        Table<SormulaTestChildNReadOnlyCascade> childNTable = getDatabase().getTable(SormulaTestChildNReadOnlyCascade.class);

        // insert 1 parent and 1 child to test (must do separately since cascade is readonly)
        SormulaTestParentReadOnlyCascade parent = new SormulaTestParentReadOnlyCascade(990, "parent roc 990");
        SormulaTestChildNReadOnlyCascade childN = new SormulaTestChildNReadOnlyCascade(9990, "child roc of parent 990");
        parent.add(childN);
        assert parentTable.insert(parent) == 1: "1:n test set up parent not inserted";
        assert childNTable.insert(childN) == 1: "1:n test set up child not inserted";
        
        // test delete on 1 to many relationship via list
        assert parentTable.delete(parent) == 1 : "1:n parent was not deleted";
        
        // select directly to test that child was NOT deleted
        SormulaTestChildNReadOnlyCascade selectedChildN = childNTable.select(childN.getId());
        assert selectedChildN != null : "1:n child was deleted using readonly cascade";
        
        commit();
    }
}
