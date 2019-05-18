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
package org.sormula.tests.active;

import java.util.Set;

import org.sormula.active.ActiveTable;
import org.testng.annotations.Test;


/**
 * Tests org.sormula.active delete operations.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="active.delete", dependsOnGroups="active.insert")
public class DeleteTest extends ActiveDatabaseTest<SormulaTestAR>
{
    @Override
    protected void open() throws Exception
    {
        super.open();
        createTable(SormulaTestAR.class);
    }
    
    
    @Test
    public void deleteOneAR() 
    {
        selectTestRows(); // must perform each time since other tests are destructive
        SormulaTestAR record = getRandom();

        assert record.delete() == 1 : "AR delete one row failed";
        
        // read row to confirm that delete applied
        ActiveTable<SormulaTestAR> table = getActiveTable();
        assert table.select(record.getId()) == null : "AR row was not deleted";
    }
    
    
    @Test
    public void deleteOneARBatch() 
    {
        selectTestRows(); // must perform each time since other tests are destructive
        SormulaTestAR record = getRandom();

        assert record.deleteBatch() == 1 : "AR delete one row batch failed";
        
        // read row to confirm that delete applied
        ActiveTable<SormulaTestAR> table = getActiveTable();
        assert table.select(record.getId()) == null : "AR row was not deleted batch";
    }
    
    
    @Test
    public void deleteCollectionAR() 
    {
        selectTestRows(); // must perform each time since other tests are destructive
        
        // choose random set
        Set<SormulaTestAR> set = getRandomSet();
        
        // delete
        ActiveTable<SormulaTestAR> table = getActiveTable();
        assert table.deleteAll(set) == set.size() : "AR delete count not same as collection size";
        
        // confirm each row was deleted
        for (SormulaTestAR r: set)
        {
            SormulaTestAR r2 = table.select(r.getId());
            assert r2 == null : r.getId() + " was not deleted";
        }
    }
    
    
    @Test
    public void deleteCollectionARBatch() 
    {
        selectTestRows(); // must perform each time since other tests are destructive
        
        // choose random set
        Set<SormulaTestAR> set = getRandomSet();
        
        // delete
        ActiveTable<SormulaTestAR> table = getActiveTable();
        table.deleteAllBatch(set);
        
        // confirm each row was deleted
        for (SormulaTestAR r: set)
        {
            SormulaTestAR r2 = table.select(r.getId());
            assert r2 == null : r.getId() + " was not deleted";
        }
    }
}
