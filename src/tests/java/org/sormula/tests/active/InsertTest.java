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

import java.util.ArrayList;

import org.sormula.active.ActiveTable;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests org.sormula.active insert operations. This test must run first so that
 * test data is inserted for select, update, and delete tests.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="active.insert")
public class InsertTest extends ActiveDatabaseTest<SormulaTestAR>
{
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        createTable(SormulaTestAR.class, 
            "CREATE TABLE " + getSchemaPrefix() + SormulaTestAR.class.getSimpleName() + " (" +
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
    public void insertOneAR1() 
    {
        ActiveTable<SormulaTestAR> table = getActiveTable();
        SormulaTestAR record = table.newActiveRecord(); // creates SormulaTestAR and sets data source
        record.setId(9001);
        record.setType(9);
        record.setDescription("Insert one AR 1");
        assert record.insert() == 1 : record.getDescription() + " failed";
    }
    
    
    @Test
    public void insertOneAR2() 
    {
        // create record with new operator instead of with ActiveRecord.newActiveRecord()
        SormulaTestAR record = new SormulaTestAR();  
        record.attach(getActiveDatabase()); // record needs to know databasse
        record.setId(9002);
        record.setType(9);
        record.setDescription("Insert one AR 2");
        assert record.insert() == 1 : record.getDescription() + " failed";
    }
    
    
    @Test
    public void insertOneAR3() 
    {
        // an unconvential way to use ActiveRecordTable to do the work
        SormulaTestAR record = new SormulaTestAR();
        record.setId(9003);
        record.setType(9);
        record.setDescription("Insert one AR 3");
        ActiveTable<SormulaTestAR> table = getActiveTable();
        assert table.insert(record) == 1 : record.getDescription() + " failed";
    }
    
    
    @Test
    public void insertCollectionAR() 
    {
        ArrayList<SormulaTestAR> list = new ArrayList<SormulaTestAR>();
        
        for (int i = 9501; i < 9599; ++i)
        {
            list.add(new SormulaTestAR(i, 99, "Insert collection AR " + i));
        }
        
        ActiveTable<SormulaTestAR> table = getActiveTable();
        assert table.insertAll(list) == list.size() : "insert collection AR failed";
    }
    
    @Test
    public void insertCollectionARBatch() 
    {
        ArrayList<SormulaTestAR> list = new ArrayList<SormulaTestAR>();
        
        int type = 9600;
        for (int i = 1; i < 20; ++i)
        {
            list.add(new SormulaTestAR(type + i, type, "Insert collection AR batch " + i));
        }
        
        ActiveTable<SormulaTestAR> table = getActiveTable();
        table.insertAllBatch(list); // don't rely on return value to verify since Oracle returns 0
        assert table.selectAllWhere("byType", type).size() == list.size() : "insert collection AR batch failed";
    }
}
