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

import java.util.HashMap;

import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.tests.cache.CacheTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests cached inserts for {@link ReadWriteOnly}. This test must run first so that
 * test data is inserted for select, update, and delete tests.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="cache.readonly.insert")
public class InsertTest extends CacheTest<SormulaCacheTestRO>
{
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        createTable(SormulaCacheTestRO.class, 
            "CREATE TABLE " + getSchemaPrefix() + SormulaCacheTestRO.class.getSimpleName() + " (" +
            " id INTEGER NOT NULL PRIMARY KEY," +
            " type SMALLINT," +
            " description VARCHAR(30)" +
            ")"
        );
        //getDatabase().setTimings(true);
    }
    
    
    @AfterClass
    public void tearDown() throws Exception
    {
        closeDatabase();
    }
    
    
    @Test
    public void tableAfterTransactionStart() throws SormulaException
    {
        // test that cached table can be created after transaction begins
        begin();
        SormulaCacheTestRO test = new SormulaCacheTestRO(999, 999, "transaction new Table");
        
        // create new Table instance (don't get existing)
        Table<SormulaCacheTestRO> table = new Table<>(getDatabase(), SormulaCacheTestRO.class);
        
        // this will throw IllegalCacheStateException prior to bug fix
        table.insert(test);
        
        commit();
    }
    
    
    protected SormulaCacheTestRO insertTestRow(int id) throws SormulaException
    {
        SormulaCacheTestRO test = new SormulaCacheTestRO(id, 100, "Insert " + id);
        assert getTable().insert(test) == 1 : "insert failed";
        confirmCached(test);
        return test;
    }
    
    
    @Test
    public void insertBasic() throws SormulaException
    {
        begin();
        SormulaCacheTestRO test = insertTestRow(101);
        commit();
        
        begin();
        confirmCached(test); // fails if Cached.evictOnTransactionEnd()=true
        confirmInDatabase(test);
        commit();
    }

    
    // note: insertInsert() test is not needed since database detects duplicate before any cache activity
    
    
    @Test
    public void insertUpdate() throws SormulaException
    {
        begin();
        SormulaCacheTestRO test1 = insertTestRow(103);
        
        // confirm that insert test1 followed by update test2 works
        SormulaCacheTestRO test2 = new SormulaCacheTestRO(test1.getId(), 11, "update");
        assert getTable().update(test2) == 1 : "update failed";
        confirmCached(test2);
        commit();
    }
    

    @Test
    public void insertDelete() throws SormulaException
    {
        begin();
        SormulaCacheTestRO test1 = insertTestRow(104);
        
        // confirm that insert test1 followed by delete test2 results in no cache row
        SormulaCacheTestRO test2 = new SormulaCacheTestRO(test1.getId(), 0, "delete");
        assert getTable().delete(test2) == 1 : "delete failed";
        confirmNotCached(test1);
        confirmNotCached(test2);
        commit();
    }
    

    @Test
    public void insertSelected() throws SormulaException
    {
        begin();
        
        // insert cached rows
        HashMap<Integer, SormulaCacheTestRO> testMap = new HashMap<>();
        for (int id = 111; id < 115; ++id)
        {
            SormulaCacheTestRO test = insertTestRow(id);
            testMap.put(id, test);
        }
        
        // confirm that selectAll returns uncommitted cached test rows
        // selectAll method uses cache.selected()
        for (SormulaCacheTestRO s : getTable().selectAll())
        {
            SormulaCacheTestRO test = testMap.get(s.getId());
            if (test != null) assert test == s : "selectAll did not use uncommitted cache for " + s.getId();
        }
        
        commit();
        
        begin();
        
        // confirm that selectAll returns committed cached test rows
        // selectAll method uses cache.selected()
        for (SormulaCacheTestRO s : getTable().selectAll())
        {
            SormulaCacheTestRO test = testMap.get(s.getId());
            if (test != null) assert test == s : "selectAll did not use committed cache for " + s.getId();
        }
        
        commit();

        begin();
        // confirm that selectAll gets new rows
        getTable().getCache().evictAll();
        for (SormulaCacheTestRO s : getTable().selectAll())
        {
            SormulaCacheTestRO test = testMap.get(s.getId());
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
            SormulaCacheTestRO test = insertTestRow(105);
            
            // confirm that insert followed by rollback is equivalent to no-op
            rollback();
            
            begin();
            confirmNotCached(test);
            confirmNotInDatabase(test);
            commit();
        }
    }
}
