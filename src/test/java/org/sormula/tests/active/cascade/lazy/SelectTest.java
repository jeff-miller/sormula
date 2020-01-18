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
package org.sormula.tests.active.cascade.lazy;

import java.util.List;

import org.sormula.SormulaException;
import org.sormula.active.ActiveDatabase;
import org.sormula.active.ActiveTable;
import org.sormula.tests.active.ActiveDatabaseTest;
import org.testng.annotations.Test;


/**
 * Tests lazy cascade selects.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="active.select", dependsOnGroups="active.insert")
public class SelectTest extends ActiveDatabaseTest<SormulaTestParentLazyAR>
{
    @Override
    protected void open() throws Exception
    {
        super.open();
        createTable(SormulaTestParentLazyAR.class);
    }

    
    @Test
    public void cascadeSelectLazyAR() throws SormulaException
    {
        ActiveDatabase activeDatabase = getActiveDatabase();
        ActiveTable<SormulaTestParentLazyAR> parentTable = new ActiveTable<>(activeDatabase, SormulaTestParentLazyAR.class);
        ActiveTable<SormulaTestChild1LazyAR> child1Table = new ActiveTable<>(activeDatabase, SormulaTestChild1LazyAR.class);
        ActiveTable<SormulaTestChildNLazyAR> childNTable = new ActiveTable<>(activeDatabase, SormulaTestChildNLazyAR.class);

        // for each parent 
        for (SormulaTestParentLazyAR parent : parentTable.selectAll())
        {
            // verify 1 to 1
            boolean childSelectedEarly = parent.child != null;
            SormulaTestChild1LazyAR child = parent.getChild();

            if (child != null)
            {
                assert !childSelectedEarly : "LazyAR child was selected prematurely";
                assert child == parent.getChild() : "LazyAR child was selected twice";
            
                // verify child was selected
                assert child.getId() == parent.getChild1Id() : "LazyAR 1:1 child id != parent child1 id";
                assert child1Table.select(parent.getChild1Id()) != null : "LazyAR 1:1 child cacasade error";
            }
            else
            {
                // verify no child in db
                assert child1Table.select(parent.getChild1Id()) == null : "LazyAR 1:1 child row not read";
            }
            
            // verify 1 to many
            assert parent.childList.size() == 0 : "LazyAR 1:n children were selected prematurely";
            List<SormulaTestChildNLazyAR> children = parent.getChildList();
            assert children == parent.getChildList() : "LazyAR children were selected twice";
            int countN = childNTable.<Integer>selectCount("id", "byParent", parent.getParentId());
            if (children.size() > 0)
            {
                // verify all rows selected
                for (SormulaTestChildNLazyAR c: children)
                { 
                    assert c.getParentId() == parent.getParentId() : "LazyAR 1:n child parent id != parent id";
                }
                assert countN == children.size() : "LazyAR 1:n wrong number of children read from cascade";
            }
            else 
            {
                // verify no child records
                assert countN == 0 : "LazyAR 1:n child rows were not read";
            }
        }
    }
}
