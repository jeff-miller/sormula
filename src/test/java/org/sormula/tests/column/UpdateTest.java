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
package org.sormula.tests.column;

import org.sormula.SormulaException;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests update of table with read only column.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="column.update", dependsOnGroups="column.insert")
public class UpdateTest extends DatabaseTest<ColumnTestRow>
{
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        createTable(ColumnTestRow.class);
    }
    
    
    @AfterClass
    public void tearDown() throws Exception
    {
        closeDatabase();
    }
    
    
    @Test
    public void updateReadOnly() throws SormulaException
    {
    	begin();
    	selectTestRows(); // must perform each time since other tests are destructive
    	
        // choose random row
    	ColumnTestRow row = getRandom();

        // new values
        row.setDescription("updated");
        row.setReadOnlyTest(-ColumnTestRow.READ_ONLY_VALUE); // should not update in db
        
        getTable().update(row);
        
        // read row to confirm that updates applied
        ColumnTestRow row2 = getTable().select(row.getId());
        assert row2.getDescription().equals(row.getDescription()) : "updated row not same";
        assert row2.getReadOnlyTest() == ColumnTestRow.READ_ONLY_VALUE  : "readonly column was changed";
        
        commit();
    }
}
