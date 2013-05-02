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
package org.sormula.tests.operation.pk;

import org.sormula.SormulaException;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests some select operations to confirm that multiple column primary keys are used
 * properly.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups={"operation.select"}, dependsOnGroups="operation.insert")
public class SelectTestPK extends DatabaseTest<SormulaTestPK>
{
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        createTable(SormulaTestPK.class);
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
    	selectTestRows();
    	
        // choose random row
        SormulaTestPK row = getRandom();
        
        // select by key
        SormulaTestPK selected = getTable().select(row.getId(), row.getType()); // order of parameters determined by Row#primaryKeyFields
        assert selected != null && row.getId() == selected.getId() : "select by primary key failed";
        
        commit();
    }
}
