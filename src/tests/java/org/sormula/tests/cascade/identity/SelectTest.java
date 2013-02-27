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
package org.sormula.tests.cascade.identity;

import java.util.List;

import org.sormula.SormulaException;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests cascade select annotations foreign key reference. This test is really unrelated
 * to identity columns but was put here since it is related to foreign key values tested 
 * in {@link InsertTest}.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="cascade.select", dependsOnGroups="cascade.insert")
public class SelectTest extends DatabaseTest<SormulaIdentityParent>
{
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        createTable(SormulaIdentityParent.class);
    }
    
    
    @AfterClass
    public void tearDown() throws Exception
    {
        if (isTestIdentity())
        {
            closeDatabase();
        }
    }

    
    @Test
    public void cascadeSelect() throws SormulaException
    {
        if (isTestIdentity())
        {
            begin();
            
            // for each parent 
            for (SormulaIdentityParent parent : getTable().selectAll())
            {
                // verify 1 to many
                List<SormulaIdentityChildN> children = parent.getChildList();
                
                assert children.size() > 0 : "no SormulaIdentityChildN rows to test";
                
                // verify foreign key reference was set by cascade
                for (SormulaIdentityChildN c: children)
                { 
                    SormulaIdentityParent childParent = c.getParent();
                    assert childParent != null : "child parent reference was not set by cascade";
                    assert childParent.getParentId() == parent.getParentId() : "1:n child parent id != parent id";
                }
            }
            
            commit();
        }
    }
}
