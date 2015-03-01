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

import org.sormula.CachedTable;
import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.tests.cache.CacheTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests {@link CachedTable}.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="cache.readwrite.select", dependsOnGroups="cache.readwrite.insert")
public class CachedTableTest extends CacheTest<SormulaCacheTestRW>
{
    CachedTable<SormulaCacheTestRW> cachedTable;
    
    
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        cachedTable = new CachedTable<>(getDatabase(), SormulaCacheTestRW.class); // must occur before createTable() invocation
        createTable(SormulaCacheTestRW.class);
        
        // confirm that test is using CachedTest class
        assert getTable().getClass() == CachedTable.class : "test table is not CachedTable";
    }
    
    
    @AfterClass
    public void tearDown() throws Exception
    {
        closeDatabase();
    }

    
    @Override
    public Table<SormulaCacheTestRW> getTable()
    {
        // override with CachedTable to test with it instead of standard test table
        return cachedTable;
    }

    
    protected SormulaCacheTestRW insertTestRow(int id) throws SormulaException
    {
        SormulaCacheTestRW test = new SormulaCacheTestRW(id, 600, "CachedTable test " + id);
        assert getTable().insert(test) == 1 : "insert failed";
        return test;
    }
    
    
    @Test
    public void selectBasic() throws SormulaException
    {
        // insert test record into database
        begin();
        SormulaCacheTestRW test = insertTestRow(601);
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
    

    @Test
    public void selectInsert() throws SormulaException
    {
        // insert test record into database
        begin();
        SormulaCacheTestRW test = insertTestRow(602);
        commit();
    
        // confirm that select followed by insert is error
        getTable().getCache().evictAll(); // start with nothing in cache
        begin();
        getTable().select(test.getId()); // causes UncommittedSelect to be added to cache
        confirmDuplicateException(new SormulaCacheTestRW(test.getId(), 0, "duplicate"));
        commit();
        
        begin();
        confirmInDatabase(test); // confirm original insert is still in db
        commit();
    }
    
    
    @Test
    public void selectUpdate() throws SormulaException
    {
        // insert test record into database
        begin();
        SormulaCacheTestRW test1 = insertTestRow(603);
        commit();
    
        // confirm that select test1 followed by update test2 is equivalent to update test2
        getTable().getCache().evictAll(); // start with nothing in cache
        begin();
        getTable().select(test1.getId()); // causes UncommittedSelect to be added to cache
        SormulaCacheTestRW test2 = new SormulaCacheTestRW(test1.getId(), 11, "update");
        assert getTable().update(test2) == 1 : "update failed";
        confirmCached(test2);
        commit();
        
        begin();
        confirmInDatabase(test2); // confirm test2 was final result
        commit();
    }
    
    
    @Test
    public void selectDelete() throws SormulaException
    {
        // insert test record into database
        begin();
        SormulaCacheTestRW test1 = insertTestRow(604);
        commit();
    
        // confirm that select test1 followed by delete test2 is equivalent to delete test2
        getTable().getCache().evictAll(); // start with nothing in cache
        begin();
        getTable().select(test1.getId()); // causes UncommittedSelect to be added to cache
        SormulaCacheTestRW test2 = new SormulaCacheTestRW(test1.getId(), 11, "delete");
        assert getTable().delete(test2) == 1 : "delete failed";
        confirmNotCached(test1);
        confirmNotCached(test2);
        commit();
        
        begin();
        confirmNotInDatabase(test2); // confirm test2 was deleted
        commit();
    }
}
