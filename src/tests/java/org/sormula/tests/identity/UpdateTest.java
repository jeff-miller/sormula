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
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests update operation for row with an identity column {@link Column#identity()}.
 * Update should work the same as row without an identity column except that identity
 * column is not included in the list of columns to update.
 * 
 * @author Jeff Miller
 */
@Test(groups="identity.update", dependsOnGroups="identity.insert")
public class UpdateTest extends DatabaseTest<IdentityTest>
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
    public void updateOne() throws SormulaException
    {
    	begin();
    	selectTestRows(); // must perform each time since other tests are destructive
    	
        // choose random row
    	IdentityTest row = getRandom();

        // new values
        row.setDescription("update by primary key");
        
        assert getTable().update(row) == 1 : "update one row failed";
        
        // read row to confirm that updates applied
        IdentityTest row2 = getTable().select(row.getId());
        assert row2 != null && row2.getDescription().equals(row.getDescription()) : "updated row not same";
        
        commit();
    }
}
