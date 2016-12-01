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
package org.sormula.tests.operation;

import org.sormula.Table;
import org.sormula.tests.DatabaseTest;
import org.sormula.tests.TestDatabase;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests that a connection can be reset without creating new sormula objects.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="operation.select", dependsOnGroups="operation.insert")
public class SelectTestManyConnections extends DatabaseTest<SormulaTest4>
{
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        createTable(SormulaTest4.class);
    }
    
    
    @AfterClass
    public void tearDown() throws Exception
    {
        closeDatabase();
    }

    
    @Test
    public void selectByPrimaryKey() throws Exception
    {
    	Table<SormulaTest4> testTable = getTable(); // same table object uses many connections
    	
    	begin();
    	selectTestRows(); // must perform each time since other tests are destructive

    	// choose random row
        SormulaTest4 row = getRandom();
        
        // select by primary key
        SormulaTest4 selected = testTable.select(row.getId());
        assert selected != null && row.getId() == selected.getId() : "1st select by primary key failed";
        
        commit();
        
        // change the connection for the database and table objects
        TestDatabase testDatabase = getDatabase();
        testDatabase.getConnection().close(); // close current connection
        testDatabase.setConnection(getConnection()); // use new connection
        
        // select by primary key
        begin();
        SormulaTest4 selected2 = testTable.select(row.getId());
        assert selected2 != null && row.getId() == selected2.getId() : "2nd select by primary key failed";
        commit();
    }
}