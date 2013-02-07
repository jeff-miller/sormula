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
package org.sormula.tests.active.cache;

import javax.sql.DataSource;

import org.sormula.SormulaException;
import org.sormula.active.ActiveDatabase;
import org.sormula.active.ActiveException;
import org.sormula.active.ActiveTable;
import org.sormula.active.ActiveTransaction;
import org.sormula.annotation.cache.Cached;
import org.sormula.cache.readwrite.ReadWriteCache;
import org.sormula.tests.active.ActiveDatabaseTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests {@link Cached} annotation on active database class not row or operation class.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="active.insert")
public class ActiveCacheTest extends ActiveDatabaseTest<SormulaTestARCached>
{
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        
        createTable(SormulaTestARCached.class, 
            "CREATE TABLE " + getSchemaPrefix() + SormulaTestARCached.class.getSimpleName() + " (" +
            " id INTEGER NOT NULL PRIMARY KEY," +
            " type SMALLINT," +
            " description VARCHAR(30)" +
            ")"
        );
    }
    
    
    @AfterClass
    public void tearDown() throws Exception
    {
        closeDatabase();
    }
    
    
    @Test
    public void cacheTest() throws SormulaException
    {
        // use CashedActiveDatabase to test annotation on it
        ActiveDatabase activeDatabase = new CashedActiveDatabase(getDataSource(), getSchema());
        ActiveTable<SormulaTestARCached> table = new ActiveTable<SormulaTestARCached>(activeDatabase, SormulaTestARCached.class);

        // test cache methods
        assert !table.isCached() && table.getCache() == null :
            "table should not be cached"; // cache not known outside of transaction
        table.flush(); // test no exception
        
        // test with one transaction for entire test 
        // (cache is not kept bewteen transactions since database instance is new for each transaction)
        ActiveTransaction transaction = new ActiveTransaction(activeDatabase);
        
        try
        {
            transaction.begin();
    
            assert table.isCached() && table.getCache() != null : "table should be cached";
            
            // insert test record
            SormulaTestARCached record1 = table.newActiveRecord(); // creates SormulaTestARCached and sets data source
            record1.setId(10001);
            record1.setType(100);
            record1.setDescription("Insert cached " + record1.getId());
            assert record1.insert() == 1 : record1.getDescription() + " failed";
            
            // select to confirm that it was read from cache (same object)
            SormulaTestARCached record2 = table.select(record1.getId());
            assert record2 != null && record2 == record1 : "record was not cached";
        
            // test flush
            table.flush();
            SormulaTestARCached record3 = table.select(record1.getId());
            assert record3 != null && record3 != record1 : "record was not flushed from cached";
            
            transaction.commit();
        }
        catch (ActiveException e)
        {
            transaction.rollback();
        }
    }
}


@Cached(type=ReadWriteCache.class)
class CashedActiveDatabase extends ActiveDatabase
{
    private static final long serialVersionUID = 1L;
    
    public CashedActiveDatabase(DataSource dataSource, String schema)
    {
        super(dataSource, schema);
    }

    public CashedActiveDatabase(DataSource dataSource)
    {
        super(dataSource);
    }
}
