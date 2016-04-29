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
 * Tests saving active record that has identity column without using the identity
 * generation.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="active.save", dependsOnGroups="active.insert")
public class SaveTest extends ActiveDatabaseTest<SormulaTestARNI>
{
    @BeforeClass
    public void setUp() throws Exception
    {
        if (isTestIdentity())
        {
            openDatabase();
            createTable(SormulaTestARNI.class);
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
    public void saveOneARNI1() 
    {
        if (isTestIdentity() && isTestIdentityOverride())
        {
            ActiveTable<SormulaTestARNI> table = getActiveTable();
            SormulaTestARNI insertedRecord = table.newActiveRecord(); // creates SormulaTestARNI and sets data source
            int id = -4001; // use negative to avoid collisions with generated keys
            insertedRecord.setId(id);
            insertedRecord.setDescription("Save one ARNI 1");
            insertedRecord.saveNonIdentity();
            
            // confirm
            SormulaTestARNI selectedRecord = table.select(id);
            assert selectedRecord != null && selectedRecord.getId() == id : "Save one ARNI failed for id=" + id;
        }
    }
    
    
    @Test
    public void saveCollectionARNI() 
    {
        if (isTestIdentity() && isTestIdentityOverride())
        {
            // insert collection with known id's
            int[] ids = {-5001, -5007, -5003}; // use negative to avoid collisions with generated keys
            ArrayList<SormulaTestARNI> list = new ArrayList<>(ids.length);
            
            for (int id : ids)
            {
                list.add(new SormulaTestARNI(id, "Save collection ARNI " + id));
            }
            
            ActiveTable<SormulaTestARNI> table = getActiveTable();
            table.saveNonIdentityAll(list);
            
            // confirm
            for (int id : ids)
            {
                SormulaTestARNI selectedRecord = table.select(id);
                assert selectedRecord != null && selectedRecord.getId() == id : "Save collection ARNI failed for id=" + id;
            }
        }
    }
    

    @Test
    public void saveARNIBatch() 
    {
        if (isTestIdentity() && isTestIdentityOverride())
        {
            // insert collection with known id's
            int[] ids = {-6001, -6007, -6003}; // use negative to avoid collisions with generated keys
            ArrayList<SormulaTestARNI> list = new ArrayList<>(ids.length);
            
            for (int id : ids)
            {
                list.add(new SormulaTestARNI(id, "Save batch ARNI " + id));
            }
            
            ActiveTable<SormulaTestARNI> table = getActiveTable();
            table.saveNonIdentityAllBatch(list);
            
            // confirm
            for (int id : ids)
            {
                SormulaTestARNI selectedRecord = table.select(id);
                assert selectedRecord != null && selectedRecord.getId() == id : "Save batch ARNI failed for id=" + id;
            }
        }
    }
}
