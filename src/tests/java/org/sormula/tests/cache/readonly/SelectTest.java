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
 * Tests cached selects for {@link ReadOnlyCache}. 
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="cache.readonly.select", dependsOnGroups="cache.readonly.insert")
public class SelectTest extends CacheTest<SormulaCacheTestRO>
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
        SormulaCacheTestRO test = new SormulaCacheTestRO(id, 200, "Select test " + id);
        assert getTable().insert(test) == 1 : "insert failed";
        return test;
    }
    
    
    @Test
    public void selectBasic() throws SormulaException
    {
        // insert test record into database
        begin();
        SormulaCacheTestRO test = insertTestRow(201);
        commit();
        
        // confirm that select is from cache
        begin();
        confirmCached(getTable().select(test.getId()));
        commit();
        
        // repeat but test object is not in committed cache
        getTable().getCache().evictAll(); // start with nothing in cache
        begin();
        test = getTable().select(test.getId()); // causes UncommittedSelect to be added to cache
        confirmCached(test);
        commit();
    }
    

    // selectInsert() is not needed since database will report duplicate inserts
    
    
    @Test
    public void selectUpdate() throws SormulaException
    {
        // insert test record into database
        begin();
        SormulaCacheTestRO test1 = insertTestRow(203);
        commit();
    
        // confirm that select test1 followed by update test2 is cached as update test2
        getTable().getCache().evictAll(); // start with nothing in cache
        begin();
        getTable().select(test1.getId()); // causes UncommittedSelect to be added to cache
        SormulaCacheTestRO test2 = new SormulaCacheTestRO(test1.getId(), 11, "update");
        assert getTable().update(test2) == 1 : "update failed";
        confirmCached(test2);
        commit();
    }
    

    @Test
    public void selectDelete() throws SormulaException
    {
        // insert test record into database
        begin();
        SormulaCacheTestRO test1 = insertTestRow(204);
        commit();
    
        // confirm that select test1 followed by delete test2 is cached as delete test2
        getTable().getCache().evictAll(); // start with nothing in cache
        begin();
        getTable().select(test1.getId()); // causes UncommittedSelect to be added to cache
        SormulaCacheTestRO test2 = new SormulaCacheTestRO(test1.getId(), 11, "delete");
        assert getTable().delete(test2) == 1 : "delete failed";
        confirmNotCached(test1);
        confirmNotCached(test2);
        commit();
    }
    

    @Test
    public void selectRollback() throws SormulaException
    {
        if (isUseTransacation()) // only test if transactions are used
        {
            begin();
            SormulaCacheTestRO test = insertTestRow(205);
            commit();
         
            // confirm that select followed by rollback does not cache the select
            getTable().getCache().evictAll(); // start with nothing in cache
            begin();
            getTable().select(test.getId()); // causes UncommittedSelect to be added to cache            
            rollback();
            
            begin();
            confirmNotCached(test);
            confirmInDatabase(test);
            commit();
        }
    }
    
    
    @Test
    public void selectSelected() throws SormulaException
    {
        // test that cached row is used for selectAll
        
        // insert test row
        begin();
        SormulaCacheTestRO test = insertTestRow(206);
        commit();
        
        // select test row to get it into cache as uncommitted select
        getTable().getCache().evictAll(); // start with empty cache
        begin();
        SormulaCacheTestRO testCached = getTable().select(test.getId());
        confirmCached(testCached);
        
        // select all type 200 rows (insertTestRow inserts type 200)
        boolean tested = false;
        for (SormulaCacheTestRO s : getTable().selectAllWhere("type", 200))
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
