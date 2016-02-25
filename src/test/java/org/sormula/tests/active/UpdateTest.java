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

import org.sormula.active.ActiveDatabase;
import org.sormula.active.ActiveException;
import org.sormula.active.ActiveTable;
import org.sormula.active.ActiveTransaction;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests org.sormula.active update operations.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="active.update", dependsOnGroups="active.insert")
public class UpdateTest extends ActiveDatabaseTest<SormulaTestAR>
{
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        createTable(SormulaTestAR.class);
    }
    
    
    @AfterClass
    public void tearDown() throws Exception
    {
        closeDatabase();
    }
    
    
    @Test
    public void updateOneAR() 
    {
        // test with one transaction for entire test
        ActiveTransaction transaction = new ActiveTransaction(activeDatabase);
        
        try
        {
            transaction.begin();
            
            ActiveTable<SormulaTestAR> table = getActiveTable();
            selectTestRows(); // must perform each time since other tests are destructive
            SormulaTestAR record = getRandom();
            
            // new values
            record.setType(99);
            record.setDescription("AR update by primary key");
            
            assert record.update() == 1 : record.getDescription() + " failed";
            
            // read record to confirm that updates applied
            SormulaTestAR record2 = table.select(record.getId());
            assert record2 != null && record.getType() == record2.getType() && record.getDescription().equals(record2.getDescription()) :
                record.getDescription() + " updated row not same";
            
            transaction.commit();
        }
        catch (ActiveException e)
        {
            transaction.rollback();
        }
    }
    
    
    @Test
    public void updateOneARBatch() 
    {
        // test with one transaction for entire test
        ActiveTransaction transaction = new ActiveTransaction(activeDatabase);
        
        try
        {
            transaction.begin();
            
            ActiveTable<SormulaTestAR> table = getActiveTable();
            selectTestRows(); // must perform each time since other tests are destructive
            SormulaTestAR record = getRandom();
            
            // new values
            record.setType(99);
            record.setDescription("AR update by primary key in batch");
            
            assert record.updateBatch() == 1 : record.getDescription() + " failed";
            
            // read record to confirm that updates applied
            SormulaTestAR record2 = table.select(record.getId());
            assert record2 != null && record.getType() == record2.getType() && record.getDescription().equals(record2.getDescription()) :
                record.getDescription() + " updated row not same";
            
            transaction.commit();
        }
        catch (ActiveException e)
        {
            transaction.rollback();
        }
    }
    

    @Test
    public void updateCollectionAR() 
    {
        // test transaction with default active database
        ActiveDatabase.setDefault(getActiveDatabase());
        ActiveTransaction transaction = new ActiveTransaction();
        
        try
        {
            transaction.begin();
        	selectTestRows(); // must perform each time since other tests are destructive
        	
        	// choose random set
            Set<SormulaTestAR> set = getRandomSet();
            
            // modify to update
            for (SormulaTestAR r: set)
            {
                r.setType(999);
            }
    
            // update
            ActiveTable<SormulaTestAR> table = getActiveTable();
            assert table.updateAll(set) == set.size() : "update AR count not same as collection size";
            
            // confirm each row was updated
            for (SormulaTestAR r: set)
            {
                SormulaTestAR r2 = table.select(r.getId());
                assert r2 != null && r2.getType() == r.getType() : "update collection AR failed";
            }
            
            transaction.commit();
        }
        catch (ActiveException e)
        {
            transaction.rollback();
        }
        finally
        {
            ActiveDatabase.setDefault(null); // reset
        }
    }
    

    @Test
    public void updateCollectionARBatch() 
    {
        // test transaction with default active database
        ActiveDatabase.setDefault(getActiveDatabase());
        ActiveTransaction transaction = new ActiveTransaction();
        
        try
        {
            transaction.begin();
            selectTestRows(); // must perform each time since other tests are destructive
            
            // choose random set
            Set<SormulaTestAR> set = getRandomSet();
            
            // modify to update
            for (SormulaTestAR r: set)
            {
                r.setType(888);
            }
    
            // update
            ActiveTable<SormulaTestAR> table = getActiveTable();
            table.updateAllBatch(set);
            
            // confirm each row was updated
            for (SormulaTestAR r: set)
            {
                SormulaTestAR r2 = table.select(r.getId());
                assert r2 != null && r2.getType() == r.getType() : "update collection AR batch failed";
            }
            
            transaction.commit();
        }
        catch (ActiveException e)
        {
            transaction.rollback();
        }
        finally
        {
            ActiveDatabase.setDefault(null); // reset
        }
    }
}
