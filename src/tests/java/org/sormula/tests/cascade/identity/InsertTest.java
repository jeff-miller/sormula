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
package org.sormula.tests.cascade.identity;

import org.sormula.SormulaException;
import org.sormula.log.ClassLogger;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests cascade inserts for {@link SormulaIdentityParent}.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="cascade.insert")
public class InsertTest extends DatabaseTest<SormulaIdentityParent>
{
    private static final ClassLogger log = new ClassLogger();
    
    
    @BeforeClass
    public void setUp() throws Exception
    {
        if (isTestIdentity())
        {
            openDatabase();
            createTable(SormulaIdentityParent.class, 
                "CREATE TABLE " + getSchemaPrefix() + SormulaIdentityParent.class.getSimpleName() + " (" +
                " parentid INTEGER GENERATED ALWAYS AS IDENTITY(START WITH 11) PRIMARY KEY," +
                " description VARCHAR(60)" +
                ")"
            );
            
            // create child table for 1 to n relationship
            DatabaseTest<SormulaIdentityChildN> childN = new DatabaseTest<SormulaIdentityChildN>();
            childN.openDatabase();
            childN.createTable(SormulaIdentityChildN.class, 
                    "CREATE TABLE " + getSchemaPrefix() + SormulaIdentityChildN.class.getSimpleName() + " (" +
                    " childid INTEGER GENERATED ALWAYS AS IDENTITY(START WITH 1001) PRIMARY KEY," +
                    " parentid INTEGER NOT NULL," +
                    " description VARCHAR(60)" +
                    ")"
                );
            childN.closeDatabase();
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
    public void insertOne() throws SormulaException
    {
        if (isTestIdentity())
        {
            begin();
            SormulaIdentityParent parent = new SormulaIdentityParent("Insert parent");
            SormulaIdentityChildN child = new SormulaIdentityChildN("insert child");
            parent.add(child);
            
            assert getTable().insert(parent) == 1 : "insert parent failed";
            assert child.getParentId() == parent.getParentId() : "child parent id was not set";
            
            commit();
        }
    }
}
