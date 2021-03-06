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

import java.util.Set;

import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.operation.UpdateOperation;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.Test;


/**
 * Tests all update operations.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="operation.update", dependsOnGroups="operation.insert")
public class UpdateTest extends DatabaseTest<SormulaTest4>
{
    @Override
    protected void open() throws Exception
    {
        super.open();
        createTable(SormulaTest4.class);
    }
    
    
    @Test
    public void updateOne() throws SormulaException
    {
    	begin();
    	selectTestRows(); // must perform each time since other tests are destructive
    	
        // choose random row
        SormulaTest4 row = getRandom();

        // new values
        row.setType(99);
        row.setDescription("update by primary key");
        
        assert getTable().update(row) == 1 : "update one row failed";
        
        // read row to confirm that updates applied
        SormulaTest4 row2 = getTable().select(row.getId());
        assert row2 != null && row2.getType() == row.getType() && row2.getDescription().equals(row.getDescription()) :
            " updated row not same";
        
        commit();
    }
    
    
    @Test
    public void updateCollection() throws SormulaException
    {
    	begin();
    	selectTestRows(); // must perform each time since other tests are destructive
    	
    	// choose random set
        Set<SormulaTest4> set = getRandomSet();
        
        // modify to update
        for (SormulaTest4 row: set)
        {
            row.setType(999);
        }

        // update
        Table<SormulaTest4> table = getTable();
        assert table.updateAll(set) == set.size() : "update count not same as collection size";
        
        // confirm each row was updated
        for (SormulaTest4 r: set)
        {
            SormulaTest4 r2 = table.select(r.getId());
            assert r2 != null && r2.getType() == r.getType() : "update collection failed";
        }
        
        commit();
    }
    
    
    @Test
    public void updateBatch() throws SormulaException
    {
        begin();
        selectTestRows(); // must perform each time since other tests are destructive
        
        // choose random set
        Set<SormulaTest4> set = getRandomSet();
        
        // modify to update
        for (SormulaTest4 row: set)
        {
            row.setType(888);
        }

        // update
        Table<SormulaTest4> table = getTable();
        table.updateAllBatch(set);
        
        // confirm each row was updated
        for (SormulaTest4 r: set)
        {
            SormulaTest4 r2 = table.select(r.getId());
            assert r2 != null && r2.getType() == r.getType() : "update batch failed";
        }
        
        commit();
    }
    
    
    @Test
    public void updateByOperation() throws SormulaException
    {
    	begin();
    	selectTestRows(); // must perform each time since other tests are destructive
        
    	try (UpdateOperation<SormulaTest4> operation = new UpdateOperation<>(getTable()))
    	{
            for (int i = 0; i < 10; ++i)
            {
                // choose random to update
                SormulaTest4 row = getRandom();
    
                // update
                row.setType(9999);
                operation.setRow(row);
                operation.execute();
                assert operation.getRowsAffected() == 1 : "update by operation failed";
            }
    	}        
        
        commit();
    }
}
