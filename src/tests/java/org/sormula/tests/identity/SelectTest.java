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
package org.sormula.tests.identity;

import org.sormula.SormulaException;
import org.sormula.annotation.Column;
import org.sormula.tests.operation.OperationTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests select operation for row with an identity column {@link Column#identity()}.
 * Select should work the same as row without an identity column.
 * 
 * @author Jeff Miller
 */
@Test(groups="identity.select", dependsOnGroups="identity.insert")
public class SelectTest extends OperationTest<IdentityTest>
{
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        createTable(IdentityTest.class, null);
    }
    
    
    @AfterClass
    public void tearDown() throws Exception
    {
        closeDatabase();
    }

    
    @Test
    public void selectByPrimaryKey() throws SormulaException
    {
    	begin();
    	selectTestRows(); // must perform each time since other tests are destructive

    	// choose random row
    	IdentityTest row = getRandom();
        
        // select by primary key
    	IdentityTest selected = getTable().select(row.getId());
        assert selected != null && row.getId() == selected.getId() : "select by primary key failed";
        
        commit();
    }
}
