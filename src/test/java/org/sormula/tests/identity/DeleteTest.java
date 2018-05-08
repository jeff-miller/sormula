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
import org.sormula.log.SormulaLogger;
import org.sormula.log.SormulaLoggerFactory;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests delete operation for row with an identity column {@link Column#identity()}.
 * Delete should work the same as row without an identity column.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="identity.delete", dependsOnGroups="identity.insert")
public class DeleteTest extends DatabaseTest<IdentityTest>
{
    private static final SormulaLogger log = SormulaLoggerFactory.getClassLogger();
    
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
    public void deleteOne() throws SormulaException
    {
        if (isTestIdentity())
        {
        	begin();
            selectTestRows(); // must perform each time since other tests are destructive
            
            // choose random row
            IdentityTest row = getRandom();
    
            assert getTable().delete(row) == 1 : "delete one row failed";
            
            // read row to confirm that delete applied
            assert getTable().select(row.getId()) == null : "row was not deleted";
            
            commit();
        }
    }
}
