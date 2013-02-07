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
import org.sormula.tests.cache.CacheTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests cached deletes for {@link ReadWriteCache}. 
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="cache.readwrite.delete", dependsOnGroups="cache.readwrite.insert")
public class DeleteTest extends CacheTest<SormulaCacheTestRW>
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
        SormulaCacheTestRW test = new SormulaCacheTestRW(id, 400, "Delete test " + id);
        assert getTable().insert(test) == 1 : "insert failed";
        return test;
    }
    
    
    @Test
    public void deleteBasicExistent() throws SormulaException
    {
        // insert test record into database
        begin();
        SormulaCacheTestRW test = insertTestRow(401);
        commit();
        confirmInDatabase(test);
        
        // delete to start with UncommittedDeleteRow
        getTable().getCache().evictAll(); // start with empty cache
        begin();
        getTable().delete(test);
        commit();
        
        begin();
        confirmNotInDatabase(test); // confirm delete occurred
        commit();
    }
    
    
    @Test
    public void deleteBasicNonexistent() throws SormulaException
    {
        // test delete of non existent produces no exceptions
        begin();
        SormulaCacheTestRW test = new SormulaCacheTestRW(402, 0, "nonexistent");
        getTable().delete(test);
        commit();
    }
    
    
    @Test
    public void deleteExistentInsert() throws SormulaException
    {
        // test delete existing row r1 then insert r2 is equivalent to save r2
        
        // insert test record into database
        begin();
        SormulaCacheTestRW test = insertTestRow(403);
        commit();
        confirmInDatabase(test);
        
        begin();
        getTable().getCache().evictAll(); // start with empty cache
        
        // delete to start with UncommittedDelete
        getTable().delete(test);
        
        // insert row with same key (different type and description)
        SormulaCacheTestRW duplicate = new SormulaCacheTestRW(test.getId(), 0, "duplicate " + test.getId());
        assert getTable().insert(duplicate) == 1 : "insert failed"; // cached but not yet committed
        confirmCached(duplicate);
        confirmNotCached(test);
        commit();
        
        // confirm original row was replaced by new row in database
        begin();
        confirmInDatabase(duplicate);
        confirmNotInDatabase(test);
        commit();
    }
    
    
    @Test
    public void deleteNonexistentInsert() throws SormulaException
    {
        // test delete non existing row r1 then insert r2 is equivalent to save r2
        begin();
        
        // delete to start with UncommittedDelete
        SormulaCacheTestRW test = new SormulaCacheTestRW(404, 0, "nonexistent");
        getTable().delete(test);
        
        // insert row with same key (different type and description)
        SormulaCacheTestRW duplicate = new SormulaCacheTestRW(test.getId(), 0, "duplicate " + test.getId());
        assert getTable().insert(duplicate) == 1 : "insert failed"; // cached but not yet committed
        confirmCached(duplicate);
        confirmNotCached(test);
        commit();
        
        // confirm duplicate was inserted and test was not inserted
        begin();
        confirmInDatabase(duplicate);
        confirmNotInDatabase(test);
        commit();
    }
    
    
    @Test
    public void deleteUpdate() throws SormulaException
    {
        // test that delete r1 followed by update r2 is equivalent to delete r1
        
        // insert test record into database
        begin();
        SormulaCacheTestRW test = insertTestRow(405);
        commit();
        confirmInDatabase(test);
        
        getTable().getCache().evictAll(); // start with empty cache
        begin();
        
        // delete then update
        getTable().delete(test);
        SormulaCacheTestRW updated = new SormulaCacheTestRW(test.getId(), 0, "no-op " + test.getId());
        getTable().update(updated);
        
        // should have ignored update
        confirmNotCached(updated);
        
        commit();
        
        // confirm delete 
        begin();
        confirmNotInDatabase(updated);
        confirmNotInDatabase(test);
        commit();
    }
    
    
    @Test
    public void deleteDelete() throws SormulaException
    {
        // test that delete r1 followed by delete r2 is equivalent to delete r1
        
        // insert test record into database
        begin();
        SormulaCacheTestRW test = insertTestRow(406);
        commit();
        confirmInDatabase(test);
        
        // delete then delete
        getTable().getCache().evictAll(); // start with empty cache
        begin();
        getTable().delete(test);
        SormulaCacheTestRW deleted = new SormulaCacheTestRW(test.getId(), 0, "no-op " + test.getId());
        getTable().delete(deleted);
        commit();
        
        // confirm
        begin();
        confirmNotInDatabase(test);
        confirmNotInDatabase(deleted);
        commit();
    }
    

    @Test
    public void deleteRollback() throws SormulaException
    {
        // test that delete followed by rollback does not delete from database
        
        if (isUseTransacation()) // only test if transactions are used
        {
            // insert test record into database
            begin();
            SormulaCacheTestRW test = insertTestRow(407);
            commit();

            // delete then rollback
            begin();
            SormulaCacheTestRW deleted = new SormulaCacheTestRW(test.getId(), 0, "no-op " + test.getId());
            getTable().delete(deleted);
            rollback();
            
            // confirm
            begin();
            confirmInDatabase(test);
            commit();
        }
    }
    
    
    @Test
    public void deleteSelected() throws SormulaException
    {
        // test that deleted cached row is not selected with selectAll
        
        // insert test row
        begin();
        SormulaCacheTestRW test = insertTestRow(408);
        commit();
        
        // delete test row to get it into cache as uncommitted delete
        getTable().getCache().evictAll(); // start with empty cache
        begin();
        getTable().delete(test);
        
        // select all type 400 rows (type 400 used by these tests)
        boolean tested = false;
        for (SormulaCacheTestRW s : getTable().selectAllWhere("type", 400))
        {
            // confirm that cached deleted row was NOT selected
            assert s.getId() != test.getId() : "selectAll returned deleted row " + s.getId();
            tested = true;
        }
        
        assert tested : "no rows tested for " + test.getId();
        commit();
    }
}
