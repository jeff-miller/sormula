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
package org.sormula.tests.zeroannotation;

import java.util.Set;

import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.tests.operation.OperationTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests some update operations for row class with no annotations.
 * 
 * @author Jeff Miller
 */
@Test(groups="zeroannotation.update", dependsOnGroups="zeroannotation.insert")
public class UpdateTest extends OperationTest<ZeroAnnoationTest>
{
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        createTable(ZeroAnnoationTest.class, null);
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
    	selectTestRows();
    	
        // choose random row
    	ZeroAnnoationTest row = getRandom();

        // new values
        row.setType(99);
        row.setDescription("update by primary key");
        
        assert getTable().update(row) == 1 : "update one row failed";
        
        // read row to confirm that updates applied
        ZeroAnnoationTest row2 = getTable().select(row.getId());
        assert row2 != null && row2.getType() == row.getType() && row2.getDescription().equals(row.getDescription()) :
            " updated row not same";
        
        commit();
    }
    
    
    @Test
    public void updateCollection() throws SormulaException
    {
    	begin();
    	selectTestRows();
    	
    	// choose random set
        Set<ZeroAnnoationTest> set = getRandomSet();
        
        // modify to update
        for (ZeroAnnoationTest row: set)
        {
            row.setType(999);
        }

        // update
        Table<ZeroAnnoationTest> table = getTable();
        assert table.updateAll(set) == set.size() : "update count not same as collection size";
        
        // confirm each row was updated
        for (ZeroAnnoationTest r: set)
        {
            ZeroAnnoationTest r2 = table.select(r.getId());
            assert r2 != null && r2.getType() == r.getType() : "update collection failed";
        }
        
        commit();
    }
}