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

import org.sormula.active.ActiveDatabase;
import org.sormula.active.ActiveTable;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests org.sormula.active save operations. 
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="active.save", dependsOnGroups="active.insert")
public class SaveTest extends ActiveDatabaseTest<SormulaTestAR>
{
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        createTable(SormulaTestAR.class);
    }
    
    
    @AfterClass
    public void tearDown() throws Exception
    {
        closeDatabase();
    }
    
    
    @Test
    public void saveOneAR1()
    {
        ActiveTable<SormulaTestAR> table = getActiveTable();
        SormulaTestAR record = table.newActiveRecord(); // creates SormulaTestAR and sets data source
        record.setId(8001);
        record.setType(8);
        record.setDescription("Save one AR 1");
        assert record.save() == 1 : record.getDescription() + " failed";
    }
    
    
    @Test
    public void saveOneAR2()
    {
        // create record with new operator instead of with ActiveRecord.newActiveRecord()
        SormulaTestAR record = new SormulaTestAR();  
        record.attach(getActiveDatabase()); // record needs to know data source
        record.setId(8002);
        record.setType(8);
        record.setDescription("Save one AR 2");
        assert record.save() == 1 : record.getDescription() + " failed";
    }
    
    
    @Test
    public void saveOneAR3()
    {
        // an unconvential way to use ActiveRecordTable to do the work
        ActiveTable<SormulaTestAR> table = getActiveTable();
        SormulaTestAR record = new SormulaTestAR();
        record.setId(8003);
        record.setType(8);
        record.setDescription("Save one AR 3");
        assert table.save(record) == 1 : record.getDescription() + " failed";
    }
    
    
    @Test
    public void saveOneAR4()
    {
        // test default active data base
        ActiveDatabase.setDefault(getActiveDatabase());
        SormulaTestAR record = new SormulaTestAR();
        record.setId(8004);
        record.setType(8);
        record.setDescription("Save one AR 4");
        assert record.save() == 1 : record.getDescription() + " failed";
    }
    
    
    @Test
    public void saveCollectionAR()
    {
        ArrayList<SormulaTestAR> list = new ArrayList<>();
        
        for (int i = 8501; i < 8599; ++i)
        {
            list.add(new SormulaTestAR(i, 88, "Save collection AR " + i));
        }
        
        ActiveTable<SormulaTestAR> table = getActiveTable();
        
        // records are new so insert should be performed
        assert table.saveAll(list) == list.size() : "save collection AR failed";
        
        // records exist so update should be performed
        assert table.saveAll(list) == list.size() : "save collection AR failed";
    }
}
