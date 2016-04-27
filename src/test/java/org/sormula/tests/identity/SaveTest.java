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
package org.sormula.tests.identity;

import org.sormula.SormulaException;
import org.sormula.annotation.Column;
import org.sormula.log.ClassLogger;
import org.sormula.operation.InsertOperation;
import org.sormula.operation.UpdateOperation;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests save operation for row with an identity column {@link Column#identity()}.
 * Save operation work as {@link InsertOperation} if primary key does exist otherwise it should work as
 * {@link UpdateOperation}. 
 * <p>
 * If identity parameter is false, then primary key is not generated for inserts. Instead
 * the identity column value inserted is the value of {@link IdentityTest#getId()}.
 *  
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="identity.update", dependsOnGroups="identity.insert")
public class SaveTest extends DatabaseTest<IdentityTest>
{
    private static final ClassLogger log = new ClassLogger();
    
    @BeforeClass
    public void setUp() throws Exception
    {
        if (isTestIdentity())
        {
            openDatabase();
            createTable(IdentityTest.class);
        }
        else
        {
            log.info("skipping identity test " + getClass());
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
    public void saveOne() throws SormulaException
    {
        if (isTestIdentity())
        {
        	begin();
        	selectTestRows(); // must perform each time since other tests are destructive
        	
            // choose random row
        	IdentityTest row = getRandom();
        	int rowId = row.getId(); // remember original in case it is erroneously changed upon insert
    
            // new values
            row.setDescription("save by primary key");
            
            assert getTable().save(row) == 1 : "save one row failed";
            
            // read row to confirm that updates applied
            IdentityTest row2 = getTable().select(rowId);
            assert row2 != null && row2.getDescription().equals(row.getDescription()) : "saved row not same";
            
            commit();
        }
    }
    
    
    @Test
    public void saveOneNonIdentity() throws SormulaException
    {
        // TODO need way to know if nonIdentityMethods should be tested since some db's may not allow normal insert for identity columns
        // TODO add new property in jdbc.properties like testNonIdentityMethods?
        if (isTestIdentity()) 
        {
            begin();
            selectTestRows(); // must perform each time since other tests are destructive
            
            // choose random row
            IdentityTest row = getRandom();
            row.setId(-row.getId()); // use negative as primary key to force insert not update
            int rowId = row.getId(); // remember original in case it is erroneously changed upon insert
    
            // new values
            row.setDescription("save non identity by primary key");
            
            assert getTable().saveNonIdentity(row) == 1 : "save  non identity one row failed";
            
            // read row to confirm that insert without identity was used
            IdentityTest row2 = getTable().select(rowId);
            assert row2 != null && row2.getDescription().equals(row.getDescription()) : "saved row not same";
            
            commit();
        }
    }
}
