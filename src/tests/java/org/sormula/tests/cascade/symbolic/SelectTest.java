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
package org.sormula.tests.cascade.symbolic;

import java.util.List;

import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.annotation.cascade.OneToManyCascade;
import org.sormula.annotation.cascade.SelectCascade;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests default {@link SelectCascade} for {@link OneToManyCascade}. The default select cascade
 * for {@link OneToManyCascade} for each of the subclasses of {@link SormulaSymParent}.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="cascade.select", dependsOnGroups="cascade.insert")
public class SelectTest extends DatabaseTest<SormulaSymParent>
{
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        createTable(SormulaSymParent.class);
    }
    
    
    @AfterClass
    public void tearDown() throws Exception
    {
        closeDatabase();
    }

    
    @Test
    public void cascadeSelect1() throws SormulaException
    {
        cascadeSelect(getDatabase().getTable(SormulaSymParent1.class));
    }

    
    @Test
    public void cascadeSelect2() throws SormulaException
    {
        cascadeSelect(getDatabase().getTable(SormulaSymParent2.class));
    }

    
    @Test
    public void cascadeSelect3() throws SormulaException
    {
        cascadeSelect(getDatabase().getTable(SormulaSymParent3.class));
    }

    
    @Test
    public void cascadeSelect4() throws SormulaException
    {
        cascadeSelect(getDatabase().getTable(SormulaSymParent4.class));
    }
    
    
    void cascadeSelect(Table<? extends SormulaSymParent> parentTable) throws SormulaException
    {
        begin();
        Table<SormulaSymChild> childTable = getDatabase().getTable(SormulaSymChild.class);
        
        // for each parent 
        for (SormulaSymParent parent : parentTable.selectAll())
        {
            // verify 1 to many
            List<SormulaSymChild> children = parent.getChildList();
            int count = childTable.selectCount("byParent", parent.getParentId());
            
            if (children.size() > 0)
            {
                // verify all rows selected
                for (SormulaSymChild c: children)
                { 
                    assert c.getParentId() == parent.getParentId() : "Sym: child parent id != parent id";
                }
                assert count == children.size() : "Sym: wrong number of children read from cascade";
            }
            else 
            {
                // verify no child records
                assert count == 0 : "Sym: child rows were not read";
            }
        }
        
        commit();
    }
}
