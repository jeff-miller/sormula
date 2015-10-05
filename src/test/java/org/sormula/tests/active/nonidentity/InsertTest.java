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
package org.sormula.tests.active.nonidentity;

import java.util.ArrayList;

import org.sormula.active.ActiveTable;
import org.sormula.tests.active.ActiveDatabaseTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests inserting active record that has identity column without using the identity
 * generation.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="active.insert")
public class InsertTest extends ActiveDatabaseTest<SormulaTestARNI>
{
    @BeforeClass
    public void setUp() throws Exception
    {
        if (isTestIdentity())
        {
            openDatabase();
            createTable(SormulaTestARNI.class, 
                "CREATE TABLE " + getSchemaPrefix() + SormulaTestARNI.class.getSimpleName() + " (" +
                " id " + getIdentityColumnDDL() + "," +
                " description VARCHAR(30)" +
                ")"
            );
        }
    }
    
    
    @AfterClass
    public void tearDown() throws Exception
    {
        if (isTestIdentity())
        {
            closeDatabase();
        }
    }
    
    
    @Test
    public void insertOneARNI1() 
    {
        if (isTestIdentity())
        {
            ActiveTable<SormulaTestARNI> table = getActiveTable();
            SormulaTestARNI insertedRecord = table.newActiveRecord(); // creates SormulaTestARNI and sets data source
            int id = 1001;
            insertedRecord.setId(id);
            insertedRecord.setDescription("Insert one ARNI 1");
            insertedRecord.insertNonIdentity();
            
            // confirm
            SormulaTestARNI selectedRecord = table.select(id);
            assert selectedRecord != null && selectedRecord.getId() == id : "Insert one ARNI failed for id=" + id;
        }
    }
    
    
    @Test
    public void insertCollectionARNI() 
    {
        if (isTestIdentity())
        {
            // insert collection with known id's
            int[] ids = {2001, 2007, 2003};
            ArrayList<SormulaTestARNI> list = new ArrayList<>(ids.length);
            
            for (int id : ids)
            {
                list.add(new SormulaTestARNI(id, "Insert collection ARNI " + id));
            }
            
            ActiveTable<SormulaTestARNI> table = getActiveTable();
            table.insertNonIdentityAll(list);
            
            // confirm
            for (int id : ids)
            {
                SormulaTestARNI selectedRecord = table.select(id);
                assert selectedRecord != null && selectedRecord.getId() == id : "Insert collection ARNI failed for id=" + id;
            }
        }
    }
    
    
    @Test
    public void insertARNIBatch() 
    {
        if (isTestIdentity())
        {
            // insert collection with known id's
            int[] ids = {3001, 3007, 3003};
            ArrayList<SormulaTestARNI> list = new ArrayList<>(ids.length);
            
            for (int id : ids)
            {
                list.add(new SormulaTestARNI(id, "Insert batch ARNI " + id));
            }
            
            ActiveTable<SormulaTestARNI> table = getActiveTable();
            table.insertNonIdentityAllBatch(list);
            
            // confirm
            for (int id : ids)
            {
                SormulaTestARNI selectedRecord = table.select(id);
                assert selectedRecord != null && selectedRecord.getId() == id : "Insert batch ARNI failed for id=" + id;
            }
        }
    }
}
