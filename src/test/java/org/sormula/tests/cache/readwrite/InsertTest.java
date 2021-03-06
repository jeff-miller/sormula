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

import java.util.HashMap;

import org.sormula.SormulaException;
import org.sormula.cache.readwrite.ReadWriteCache;
import org.sormula.tests.cache.CacheTest;
import org.testng.annotations.Test;


/**
 * Tests cached inserts for {@link ReadWriteCache}. This test must run first so that
 * test data is inserted for select, update, and delete tests.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="cache.readwrite.insert")
public class InsertTest extends CacheTest<SormulaCacheTestRW>
{
    @Override
    protected void open() throws Exception
    {
        openDatabase(true); // true to use data source to test new connection after Database.close()
        createTable(SormulaCacheTestRW.class, 
            "CREATE TABLE " + getSchemaPrefix() + SormulaCacheTestRW.class.getSimpleName() + " (" +
            " id INTEGER NOT NULL PRIMARY KEY," +
            " type SMALLINT," +
            " description VARCHAR(30)" +
            ")"
        );
    }
    
    
    protected SormulaCacheTestRW insertTestRow(int id) throws SormulaException
    {
        SormulaCacheTestRW test = new SormulaCacheTestRW(id, 100, "Insert " + id);
        assert getTable().insert(test) == 1 : "insert failed";
        confirmCached(test);
        return test;
    }
    

    @Test
    public void insertBasic() throws SormulaException
    {
        begin();
        SormulaCacheTestRW test = insertTestRow(101);
        confirmCached(test);
        commit();
        
        begin();
        confirmCached(test); // fails if Cached.evictOnTransactionEnd()=true
        confirmInDatabase(test);
        commit();
    }
    

    @Test
    public void insertInsert() throws SormulaException
    {
        begin();
        SormulaCacheTestRW test = insertTestRow(102);
        
        // confirm duplicate cache inserts are not permitted
        confirmDuplicateException(new SormulaCacheTestRW(test.getId(), 0, "duplicate"));
        
        confirmCached(test); // confirm original insert is still in cache
        commit();
        
        begin();
        confirmInDatabase(test); // confirm original insert is in db
        commit();
    }
    

    @Test
    public void insertUpdate() throws SormulaException
    {
        begin();
        SormulaCacheTestRW test1 = insertTestRow(103);
        
        // confirm that insert test1 followed by update test2 is equivalent to insert of test2
        SormulaCacheTestRW test2 = new SormulaCacheTestRW(test1.getId(), 11, "update");
        assert getTable().update(test2) == 1 : "update failed";
        confirmCached(test2);
        commit();
        
        begin();
        confirmInDatabase(test2); // confirm test2 was final result
        commit();
    }
    

    @Test
    public void insertDelete() throws SormulaException
    {
        begin();
        SormulaCacheTestRW test1 = insertTestRow(104);
        
        // confirm that insert test1 followed by delete test2 is equivalent to no-op
        SormulaCacheTestRW test2 = new SormulaCacheTestRW(test1.getId(), 0, "delete");
        assert getTable().delete(test2) == 1 : "delete failed";
        confirmNotCached(test1);
        confirmNotCached(test2);
        commit();
        
        begin();
        confirmNotInDatabase(test1); // confirm not in database
        commit();
    }
    
    
    @Test
    public void insertSelected() throws SormulaException
    {
        begin();
        
        // insert cached rows
        HashMap<Integer, SormulaCacheTestRW> testMap = new HashMap<>();
        for (int id = 111; id < 115; ++id)
        {
            SormulaCacheTestRW test = insertTestRow(id);
            testMap.put(id, test);
        }
        
        // confirm that selectAll returns uncommitted cached test rows
        // selectAll method uses cache.selected()
        for (SormulaCacheTestRW s : getTable().selectAll())
        {
            SormulaCacheTestRW test = testMap.get(s.getId());
            if (test != null) assert test == s : "selectAll did not use uncommitted cache for " + s.getId();
        }
        
        commit();
        
        begin();
        
        // confirm that selectAll returns committed cached test rows
        // selectAll method uses cache.selected()
        for (SormulaCacheTestRW s : getTable().selectAll())
        {
            SormulaCacheTestRW test = testMap.get(s.getId());
            if (test != null) assert test == s : "selectAll did not use committed cache for " + s.getId();
        }
        
        commit();

        begin();
        // confirm that selectAll gets new rows
        getTable().getCache().evictAll();
        for (SormulaCacheTestRW s : getTable().selectAll())
        {
            SormulaCacheTestRW test = testMap.get(s.getId());
            if (test != null) assert test != s : "row not evicted for " + s.getId();
        }
        commit();
    }
    

    @Test
    public void insertRollback() throws SormulaException
    {
        if (isUseTransacation()) // only test if transactions are used
        {
            begin();
            SormulaCacheTestRW test = insertTestRow(105);
            
            // confirm that insert followed by rollback is equivalent to no-op
            rollback();
            
            begin();
            confirmNotCached(test);
            confirmNotInDatabase(test);
            commit();
        }
    }


    @Test
    // tests closing Database and then reusing it
    public void insertAfterClose() throws SormulaException
    {
        if (isUseTransacation()) // only test if transactions are used
        {
            begin();
            SormulaCacheTestRW test = insertTestRow(131);
            confirmCached(test);
            commit();
            
            // row should still be cached after database is closed
            getDatabase().close();
            getDatabase().close(); // multiple closes should not fail
            
            begin();
            confirmCached(test); // fails if Cached.evictOnTransactionEnd()=true
            confirmInDatabase(test);
            insertTestRow(132); // should not fail
            commit();
        }
    }
}
