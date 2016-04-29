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

import java.util.Set;

import org.sormula.SormulaException;
import org.sormula.Table;
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
        if (isTestIdentity() && isTestIdentityOverride()) 
        {
            begin();
            
            // save with new id to test insert operation
            IdentityTest newRow = new IdentityTest();
            int rowId = -88888; // remember original in case it is erroneously changed upon insert
            newRow.setId(rowId); // use negative to avoid collision with generated ids
    
            // new values
            newRow.setDescription("save non identity by primary key");
            
            // save should use insert not update since id is not in database
            assert getTable().saveNonIdentity(newRow) == 1 : "save  non identity one row failed";
            
            // read row to confirm that insert without identity was used
            IdentityTest row2 = getTable().select(rowId);
            assert row2 != null && row2.getDescription().equals(newRow.getDescription()) : "saved non identity not same";
            
            commit();
        }
    }
    
    
    @Test
    public void saveNonIdentityBatch() throws SormulaException
    {
        if (isTestIdentity() && isTestIdentityOverride())
        {
            begin();
            selectTestRows(); // must perform each time since other tests are destructive
            Set<IdentityTest> set = getRandomSet();
            
            // add one new row to test insert
            int rowId = -77777; // remember original in case it is erroneously changed upon insert
            IdentityTest newRow = new IdentityTest();
            newRow.setId(rowId);
            newRow.setDescription("save non identity batch");
            set.add(newRow);
            
            // save where new row is inserted and others are updates
            // insert should use id value from row and not create new identity
            Table<IdentityTest> table = getTable();
            table.saveNonIdentityAllBatch(set);
            commit();
            
            // read row to confirm that insert without identity was used
            begin();
            IdentityTest row2 = getTable().select(rowId);
            assert row2 != null && row2.getDescription().equals(newRow.getDescription()) : "saved non identity batch not same";
            commit();
        }
    }
}
