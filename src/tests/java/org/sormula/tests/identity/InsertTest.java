/* sormula - Simple object relational mapping
 * Copyright (C) 2011 Jeff Miller
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

import java.util.ArrayList;

import org.sormula.SormulaException;
import org.sormula.annotation.Column;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests inserts when row has {@link Column#identity()}. This test must run first so that
 * test data is inserted for select, update, and delete tests.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="identity.insert")
public class InsertTest extends DatabaseTest<IdentityTest>
{
    @BeforeClass
    public void setUp() throws Exception
    {
        // TODO skip identity tests if jdbc.properties indicates
        openDatabase();
        createTable(IdentityTest.class, 
            "CREATE TABLE " + getSchemaPrefix() + IdentityTest.class.getSimpleName() + " (" +
            " id INTEGER GENERATED ALWAYS AS IDENTITY(START WITH 101) PRIMARY KEY," +
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
    public void insertOne() throws SormulaException
    {
        IdentityTest row = new IdentityTest(-1, "Insert one");
        assert getTable().insert(row) == 1 : "insert one failed";
        assert row.getId() > 0 : "indentity column was not generated";
        commit();
    }
    
    
    @Test
    public void insertCollection() throws SormulaException
    {
        ArrayList<IdentityTest> list = new ArrayList<IdentityTest>();
        
        for (int i = 1; i < 10; ++i)
        {
            list.add(new IdentityTest(-i, "Insert collection " + i));
        }
        
        assert getTable().insertAll(list) == list.size() : "insert collection failed";
        
        for (IdentityTest row: list)
        {
            assert row.getId() > 0 : "indentity column was not generated";
        }
        
        commit();
    }
}
