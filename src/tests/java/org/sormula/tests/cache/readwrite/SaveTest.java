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
 * Tests cached saves for {@link ReadWriteCache}. 
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="cache.readwrite.save", dependsOnGroups="cache.readwrite.insert")
public class SaveTest extends CacheTest<SormulaCacheTestRW>
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
    
    
    // create row in cache as Uncommitted save by delete followed by insert
    protected SormulaCacheTestRW insertSavedTestRow(int id) throws SormulaException
    {
        SormulaCacheTestRW test = new SormulaCacheTestRW(id, 500, "Save test " + id);
        getTable().delete(test);
        getTable().insert(test);
        return test;
    }
    
    
    // basic save tests have been covered by other tests in this package
    
    
    @Test
    public void saveInsert() throws SormulaException
    {
        // test that insert r2 for uncommitted save r1 is duplicate exception
        begin();
        SormulaCacheTestRW test = insertSavedTestRow(501);
        
        // confirm duplicate cache inserts are not permitted
        confirmDuplicateException(new SormulaCacheTestRW(test.getId(), 0, "duplicate"));
        
        confirmCached(test); // confirm original insert is still in cache
        commit();
        
        begin();
        confirmInDatabase(test); // confirm original inserted into db
        commit();
    }
    
    
    @Test
    public void saveUpdate() throws SormulaException
    {
        // test that insert r2 for uncommitted save r1 is equivalent to save r2
        begin();
        SormulaCacheTestRW test = insertSavedTestRow(502);
        
        SormulaCacheTestRW updated = new SormulaCacheTestRW(test.getId(), 0, "updated save " + test.getId());
        getTable().update(updated);
        confirmCached(updated); // confirm updated is now in cache
        commit();
        
        begin();
        confirmInDatabase(updated); // confirm updated inserted into db
        commit();
    }
    
    
    @Test
    public void saveDelete() throws SormulaException
    {
        // test that delete r2 for uncommitted save r1 is equivalent to delete r2
        begin();
        SormulaCacheTestRW test = insertSavedTestRow(503);
        
        SormulaCacheTestRW deleted = new SormulaCacheTestRW(test.getId(), 0, "deleted save " + test.getId());
        getTable().delete(deleted);
        
        // confirm neither are in cache
        confirmNotCached(test);
        confirmNotCached(deleted); 
        commit();
        
        // confirm neither are in database
        begin();
        confirmNotInDatabase(test);
        confirmNotInDatabase(deleted);
        commit();
    }
    
    
    // rollback tests have been covered by other tests in this package
    
    
    
    @Test
    public void updateSelected() throws SormulaException
    {
        // test that cached row is used for selectAll
        
        // insert test row
        begin();
        SormulaCacheTestRW test = insertSavedTestRow(504);
        commit(); // write to db to enable it to be selected with selectAll
        
        // update test row to get it into cache as uncommitted save
        getTable().getCache().evictAll(); // start with empty cache
        begin();
        SormulaCacheTestRW testCached = insertSavedTestRow(504);
        confirmCached(testCached);
        
        // select all type 500 rows (insertSavedTestRow inserts type 500)
        boolean tested = false;
        for (SormulaCacheTestRW s : getTable().selectAllWhere("type", 500))
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
