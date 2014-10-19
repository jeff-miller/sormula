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
package org.sormula.tests.cache.readonly;

import org.sormula.SormulaException;
import org.sormula.cache.readonly.ReadOnlyCache;
import org.sormula.tests.cache.CacheTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests cached updates for {@link ReadOnlyCache}. 
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="cache.readonly.update", dependsOnGroups="cache.readonly.insert")
public class UpdateTest extends CacheTest<SormulaCacheTestRO>
{
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        createTable(SormulaCacheTestRO.class);
    }
    
    
    @AfterClass
    public void tearDown() throws Exception
    {
        closeDatabase();
    }
    
    
    protected SormulaCacheTestRO insertTestRow(int id) throws SormulaException
    {
        SormulaCacheTestRO test = new SormulaCacheTestRO(id, 300, "Update test " + id);
        assert getTable().insert(test) == 1 : "insert failed";
        return test;
    }
    
    
    protected SormulaCacheTestRO updateTestRow(SormulaCacheTestRO test) throws SormulaException
    {
        SormulaCacheTestRO updated = new SormulaCacheTestRO(test.getId(), test.getType(), "Updated test " +
                test.getId() + " " + randomInt(Integer.MAX_VALUE)); // random int keeps each update unique
        assert getTable().update(updated) == 1 : "update failed";
        return updated;
    }
    
    
    @Test
    public void updateBasic() throws SormulaException
    {
        // insert test record into database
        begin();
        SormulaCacheTestRO test = insertTestRow(301);
        commit();
        confirmInDatabase(test);
        
        // update to start with UncommittedUpdateRow
        getTable().getCache().evictAll(); // start with empty cache
        begin();
        SormulaCacheTestRO updated = updateTestRow(test);
        confirmCached(updated);
        commit();
    }
    
    
    // updateInsert() is not needed since database detects duplicate inserts
    
    
    @Test
    public void updateUpdate() throws SormulaException
    {
        // test that update r1 followed by update r2 is equivalent to update r2
        
        // insert test record into database
        begin();
        SormulaCacheTestRO test = insertTestRow(305);
        commit();
        
        begin();
        confirmInDatabase(test);
        getTable().getCache().evictAll(); // start with empty cache
        
        // first update
        SormulaCacheTestRO updated = updateTestRow(test);
        confirmCached(updated);
        
        // update again
        SormulaCacheTestRO updated2 = updateTestRow(test);
        confirmCached(updated2);
        
        commit();
    }
    
    
    @Test
    public void updateDelete() throws SormulaException
    {
        // test that update r1 followed by delete r2 is cached as delete r2
        
        // insert test record into database
        begin();
        SormulaCacheTestRO test = insertTestRow(306);
        commit();
        confirmInDatabase(test);
        
        // update then delete
        getTable().getCache().evictAll(); // start with empty cache
        begin();
        SormulaCacheTestRO updated = updateTestRow(test);
        getTable().delete(updated);
        commit();
        
        // confirm
        begin();
        confirmNotInDatabase(test);
        confirmNotInDatabase(updated);
        commit();
    }
    

    @Test
    public void updateRollback() throws SormulaException
    {
        // test that update followed by rollback does not update database
        
        if (isUseTransacation()) // only test if transactions are used
        {
            // insert test record into database
            begin();
            SormulaCacheTestRO test = insertTestRow(307);
            commit();

            // update then rollback
            begin();
            SormulaCacheTestRO updated = updateTestRow(test);
            rollback();
            
            // confirm
            begin();
            confirmInDatabase(test);
            confirmNotInDatabase(updated);
            commit();
        }
    }
    

    @Test
    public void updateSelected() throws SormulaException
    {
        // test that cached row is used for selectAll
        
        // insert test row
        begin();
        SormulaCacheTestRO test = insertTestRow(308);
        commit();
        
        // update test row to get it into cache as uncommitted udpate
        getTable().getCache().evictAll(); // start with empty cache
        begin();
        SormulaCacheTestRO testCached = updateTestRow(test);
        confirmCached(testCached);
        
        // select all type 300 rows (insertTestRow inserts type 300)
        boolean tested = false;
        for (SormulaCacheTestRO s : getTable().selectAllWhere("type", 300))
        {
            if (s.getId() == test.getId())
            {
                // confirm that cached row was selected
                assert s == testCached : "selectAll did not use cached row " + s.getId();
                tested = true;
                break;
            }
        }
        
        assert tested : "selectAll did not test " + test.getId();
        
        commit();
    }
}
