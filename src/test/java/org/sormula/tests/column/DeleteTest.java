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
import org.testng.annotations.Test;


/**
 * Tests delete from table with read only column.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="column.delete", dependsOnGroups="column.insert")
public class DeleteTest extends DatabaseTest<ColumnTestRow>
{
    @Override
    protected void open() throws Exception
    {
        super.open();
        createTable(ColumnTestRow.class);
    }
    
    
    @Test
    public void deleteReadOnly() throws SormulaException
    {
    	begin();
    	selectTestRows(); // must perform each time since other tests are destructive
    	
        // choose random row
    	ColumnTestRow row = getRandom();

    	// confirm no exceptions upon delete
        getTable().delete(row);
        
        commit();
    }
}
