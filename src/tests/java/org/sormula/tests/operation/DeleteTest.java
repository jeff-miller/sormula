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
package org.sormula.tests.operation;

import java.util.Set;

import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.operation.DeleteOperation;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests all delete operations.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="operation.delete", dependsOnGroups="operation.insert")
public class DeleteTest extends DatabaseTest<SormulaTest4>
{
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        createTable(SormulaTest4.class, null);
    }
    
    
    @AfterClass
    public void tearDown() throws Exception
    {
        closeDatabase();
    }
    
    
    @Test
    public void deleteOne() throws SormulaException
    {
    	begin();
        selectTestRows(); // must perform each time since other tests are destructive
        
        // choose random row
        SormulaTest4 row = getRandom();

        assert getTable().delete(row) == 1 : "delete one row failed";
        
        // read row to confirm that delete applied
        assert getTable().select(row.getId()) == null : "row was not deleted";
        
        commit();
    }
    
    
    @Test
    public void deleteCollection() throws SormulaException
    {
    	begin();
        selectTestRows(); // must perform each time since other tests are destructive
        
        // choose random set
        Set<SormulaTest4> set = getRandomSet();
        
        // delete
        Table<SormulaTest4> table = getTable();
        assert table.deleteAll(set) == set.size() : "delete count not same as collection size";
        
        // confirm each row was deleted
        for (SormulaTest4 r: set)
        {
            SormulaTest4 r2 = table.select(r.getId());
            assert r2 == null : r.getId() + " was not deleted";
        }
        
        commit();
    }
    
    
    @Test
    public void deleteByOperation() throws SormulaException
    {
    	begin();
        selectTestRows(); // must perform each time since other tests are destructive
        
        DeleteOperation<SormulaTest4> operation = new DeleteOperation<SormulaTest4>(getTable());
        
        for (int i = 0; i < 10; ++i)
        {
            // choose random to delete
            SormulaTest4 row = getRandom();

            // delete
            operation.setRow(row);
            operation.execute();
            
            // confirm
            assert getTable().select(row.getId()) == null : row.getId() + " was not deleted";
        }
        
        operation.close();
        commit();
    }
}
