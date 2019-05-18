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
package org.sormula.tests.column.roid;

import org.sormula.SormulaException;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.Test;


/**
 * Tests update of table with identity and read only column.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="column.update", dependsOnGroups="column.insert")
public class UpdateTest extends DatabaseTest<ColumnTestRoid>
{
    @Override
    protected void open() throws Exception
    {
        if (isTestIdentity())
        {
            super.open();
            createTable(ColumnTestRoid.class);
        }
    }
    
    
    @Test
    public void updateRoid() throws SormulaException
    {
        if (isTestIdentity())
        {
        	begin();
        	selectTestRows(); // must perform each time since other tests may be destructive
        	
            // choose random row
        	ColumnTestRoid row = getRandom();
    
            // new values
            row.setDescription("updated");
            getTable().update(row);
            
            // read row to confirm that updates applied
            ColumnTestRoid row2 = getTable().select(row.getId());
            assert row2.getDescription().equals(row.getDescription()) : "updated row not same";
            
            commit();
        }
    }
}
