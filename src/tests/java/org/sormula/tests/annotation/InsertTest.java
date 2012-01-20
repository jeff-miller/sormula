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
package org.sormula.tests.annotation;

import java.util.ArrayList;

import org.sormula.Database;
import org.sormula.SormulaException;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests inserts when annotations are on table class not row or operation class.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="annotation.insert")
public class InsertTest extends DatabaseTest<SormulaTestA>
{
    TestDB db;
    
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        
        // use database with annotations, getDatabase() override
        Database standardTestDb = super.getDatabase();
        db = new TestDB(standardTestDb.getConnection(), standardTestDb.getSchema());
        
        createTable(SormulaTestA.class, 
            "CREATE TABLE " + getSchemaPrefix() + " STA (" +
            " id INTEGER NOT NULL PRIMARY KEY," +
            " type SMALLINT," +
            " description VARCHAR(30)," +
            " unusedInt INTEGER NOT NULL, " +
            " test1 INTEGER NOT NULL, " +
            " test2 INTEGER NOT NULL, " +
            " test3 CHAR(1) NOT NULL " + 
            ")"
        );
    }
    
    
    @AfterClass
    public void tearDown() throws Exception
    {
        closeDatabase();
    }
    
    
    @Override
    public Database getDatabase()
    {
        return db;
    }


    @Test
    public void insertCollection() throws SormulaException
    {
        begin();
        ArrayList<SormulaTestA> list = new ArrayList<SormulaTestA>();
        
        for (int id = 101; id < 400; ++id)
        {
            SormulaTestA row = new SormulaTestA(id, id%5, "Insert collection " + id);
            row.setTest1(new Test1(id%4));
            row.setTest2(new Test2(id%4));
            row.setTest3(new Test3("G"));
            list.add(row);
        }
        
        assert getTable().insertAll(list) == list.size() : "insert collection failed";
        
        // verify test types are correct
        for (SormulaTestA row : getTable().selectAll())
        {
            assert row.getTest1().intValue() == row.getId()%4 : "Test1 type is not correct";
            assert row.getTest2().intValue() == row.getId()%4 : "Test2 type is not correct";
            assert row.getTest3().codeValue().equals("G")     : "Test3 type is not correct";
        }
            
        commit();
    }
}
