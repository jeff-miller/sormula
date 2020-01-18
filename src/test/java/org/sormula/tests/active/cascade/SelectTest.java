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
package org.sormula.tests.active.cascade;

import java.util.List;

import org.sormula.SormulaException;
import org.sormula.active.ActiveDatabase;
import org.sormula.active.ActiveTable;
import org.sormula.tests.active.ActiveDatabaseTest;
import org.testng.annotations.Test;


/**
 * Tests cascade select annotations.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="active.select", dependsOnGroups="active.insert")
public class SelectTest extends ActiveDatabaseTest<SormulaTestParentAR>
{
    @Override
    protected void open() throws Exception
    {
        super.open();
        createTable(SormulaTestParentAR.class);
    }

    
    @Test
    public void cascadeSelectAR() throws SormulaException
    {
        ActiveDatabase activeDatabase = getActiveDatabase();
        ActiveTable<SormulaTestParentAR> parentTable = new ActiveTable<>(activeDatabase, SormulaTestParentAR.class);
        ActiveTable<SormulaTestChild1AR> child1Table = new ActiveTable<>(activeDatabase, SormulaTestChild1AR.class);
        ActiveTable<SormulaTestChildNAR> childNTable = new ActiveTable<>(activeDatabase, SormulaTestChildNAR.class);
        
        // for each parent 
        for (SormulaTestParentAR parent : parentTable.selectAll())
        {
            // verify that parent was attached
            assert parent.getActiveDatabase() == activeDatabase : "parent was not attached to active database";

            // verify 1 to 1
            SormulaTestChild1AR child = parent.getChild();
            
            if (child != null)
            {
                // verify child was selected
                assert child.getChild1Id() == parent.getChild1Id() : "AR 1:1 child id != parent child1 id";
                assert child1Table.select(parent.getChild1Id()) != null : "AR 1:1 child cacasade error";
                
                // verify that child was attached
                assert child.getActiveDatabase() == activeDatabase : "child was not attached to active database";

            }
            else
            {
                // verify no child in db
                assert child1Table.select(parent.getChild1Id()) == null : "AR 1:1 child row not read";
            }
            
            
            // verify 1 to many
            List<SormulaTestChildNAR> children = parent.getChildList();
            int countN = childNTable.<Integer>selectCount("id", "byParent", parent.getParentId());
            if (children.size() > 0)
            {
                // verify all rows selected
                for (SormulaTestChildNAR c: children)
                { 
                    assert c.getParentId() == parent.getParentId() : "AR 1:n child parent id != parent id";
                    
                    // verify that child was attached
                    assert c.getActiveDatabase() == activeDatabase : "child was not attached to active database";
                }
                assert countN == children.size() : "AR 1:n wrong number of children read from cascade";
            }
            else 
            {
                // verify no child records
                assert countN == 0 : "AR 1:n child rows were not read";
            }
        }
    }
}
