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
package org.sormula.tests.cache.readwrite;

import org.sormula.SormulaException;
import org.sormula.cache.readwrite.ReadWriteCache;
import org.sormula.cache.writable.CacheWriteException;
import org.sormula.tests.cache.CacheTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests cached updates for {@link ReadWriteCache}. 
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="cache.readwrite.update", dependsOnGroups="cache.readwrite.insert")
public class UpdateTest extends CacheTest<SormulaCacheTestRW>
{
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        createTable(SormulaCacheTestRW.class);
    }
    
    
    @AfterClass
    public void tearDown() throws Exception
    {
        closeDatabase();
    }
    
    
    protected SormulaCacheTestRW insertTestRow(int id) throws SormulaException
    {
        SormulaCacheTestRW test = new SormulaCacheTestRW(id, 300, "Update test " + id);
        assert getTable().insert(test) == 1 : "insert failed";
        return test;
    }
    
    
    protected SormulaCacheTestRW updateTestRow(SormulaCacheTestRW test) throws SormulaException
    {
        SormulaCacheTestRW updated = new SormulaCacheTestRW(test.getId(), test.getType(), "Updated test " +
                test.getId() + " " + randomInt(Integer.MAX_VALUE)); // random int keeps each update unique
        assert getTable().update(updated) == 1 : "update failed";
        return updated;
    }
    
    
    protected SormulaCacheTestRW updateNonexistent(int id) throws SormulaException
    {
        SormulaCacheTestRW updated = new SormulaCacheTestRW(id, 0, "non existent row " + id);
        assert getTable().update(updated) == 1 : "update failed";
        return updated;
    }
    
    
    @Test
    public void updateBasicExistent() throws SormulaException
    {
        // insert test record into database
        begin();
        SormulaCacheTestRW test = insertTestRow(301);
        commit();
        confirmInDatabase(test);
        
        // update to start with UncommittedUpdateRow
        getTable().getCache().evictAll(); // start with empty cache
        begin();
        SormulaCacheTestRW updated = updateTestRow(test);
        confirmCached(updated);
        commit();
        
        begin();
        confirmInDatabase(updated); // confirm updated row was written to db
        commit();
    }
    
    
    @Test
    public void updateBasicNonexistent() throws SormulaException
    {
        begin();
        SormulaCacheTestRW updated = updateNonexistent(302);
        confirmCached(updated);
        commit();
        
        begin();
        confirmNotInDatabase(updated); // updated should still did not exist in database
        commit();
    }
    
    
    @Test
    public void updateExistentInsert() throws SormulaException
    {
        // insert test record into database
        begin();
        SormulaCacheTestRW test = insertTestRow(303);
        commit();
        confirmInDatabase(test);
        
        begin();
        getTable().getCache().evictAll(); // start with empty cache
        
        // update to start with UncommittedUpdateRow
        SormulaCacheTestRW updated = updateTestRow(test);
        confirmCached(updated);
        
        // duplicate row (different type and description)
        SormulaCacheTestRW duplicate = new SormulaCacheTestRW(test.getId(), 0, "duplicate " + test.getId());
        assert getTable().insert(duplicate) == 1 : "insert failed"; // cached but not yet committed
        
        // duplicate insert should fail upon commit since row already existed
        try
        {
            // must commit cache this way since table tranasaction listener does not throw exceptions
            getTable().getCache().commit(getDatabase().getTransaction()); 
            
            assert false : "duplicate insert allowed";
        }
        catch (CacheWriteException e)
        {
            // expected
            rollback();
        }
        
        // confirm original row did not change in database
        begin();
        confirmNotInDatabase(duplicate);
        confirmInDatabase(test);
        commit();
    }
    
    
    @Test
    public void updateNonexistentInsert() throws SormulaException
    {
        // insert test record into database
        begin();
        SormulaCacheTestRW updated = updateNonexistent(304);
        confirmCached(updated);
        
        // duplicate row (different type and description)
        SormulaCacheTestRW duplicate = new SormulaCacheTestRW(updated.getId(), 0, "duplicate " + updated.getId());
        assert getTable().insert(duplicate) == 1 : "insert failed"; // cached but not yet committed
        
        // duplicate insert should succeed since updated did not exist in database
        commit();
        
        begin();
        // confirm original row did not change in database
        confirmNotInDatabase(updated);
        
        // confirm duplicate was inserted since updated row was not in database at start of test
        confirmInDatabase(duplicate);
        commit();
    }
    
    
    @Test
    public void updateUpdate() throws SormulaException
    {
        // test that update r1 followed by update r2 is equivalent to update r2
        
        // insert test record into database
        begin();
        SormulaCacheTestRW test = insertTestRow(305);
        commit();
        confirmInDatabase(test);
        
        begin();
        getTable().getCache().evictAll(); // start with empty cache
        
        // first update
        SormulaCacheTestRW updated = updateTestRow(test);
        confirmCached(updated);
        
        // update again
        SormulaCacheTestRW updated2 = updateTestRow(test);
        confirmCached(updated2);
        
        commit();
        
        // confirm 2nd update saved to database 
        begin();
        confirmNotInDatabase(updated);
        confirmInDatabase(updated2);
        commit();
    }
    
    
    @Test
    public void updateDelete() throws SormulaException
    {
        // test that update r1 followed by delete r2 is equivalent to delete r2
        
        // insert test record into database
        begin();
        SormulaCacheTestRW test = insertTestRow(306);
        commit();
        confirmInDatabase(test);
        
        // update then delete
        getTable().getCache().evictAll(); // start with empty cache
        begin();
        SormulaCacheTestRW updated = updateTestRow(test);
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
            SormulaCacheTestRW test = insertTestRow(307);
            commit();

            // update then rollback
            begin();
            SormulaCacheTestRW updated = updateTestRow(test);
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
        SormulaCacheTestRW test = insertTestRow(308);
        commit();
        
        // update test row to get it into cache as uncommitted udpate
        getTable().getCache().evictAll(); // start with empty cache
        begin();
        SormulaCacheTestRW testCached = updateTestRow(test);
        confirmCached(testCached);
        
        // select all type 300 rows (insertTestRow inserts type 300)
        boolean tested = false;
        for (SormulaCacheTestRW s : getTable().selectAllWhere("type", 300))
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
