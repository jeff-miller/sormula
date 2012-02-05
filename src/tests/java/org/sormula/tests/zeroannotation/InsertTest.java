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
package org.sormula.tests.zeroannotation;

import java.util.ArrayList;

import org.sormula.SormulaException;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests some insert operations for row with no annotations. This test must run first so that
 * test data is inserted for select, update, and delete tests.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="zeroannotation.insert")
public class InsertTest extends DatabaseTest<ZeroAnnotationTest>
{
    boolean preMethod;
    boolean postMethod;

    
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        createTable(ZeroAnnotationTest.class, 
            "CREATE TABLE " + getSchemaPrefix() + ZeroAnnotationTest.class.getSimpleName() + " (" +
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
    public void insertOne() throws SormulaException
    {
        assert getTable().insert(new ZeroAnnotationTest(13, 1, "Insert one")) == 1 : "insert one failed";
    }
    
    
    @Test
    public void insertCollection() throws SormulaException
    {
        ArrayList<ZeroAnnotationTest> list = new ArrayList<>();
        
        for (int i = 101; i < 200; ++i)
        {
            list.add(new ZeroAnnotationTest(i, 2, "Insert collection " + i));
        }
        
        assert getTable().insertAll(list) == list.size() : "insert collection failed";
    }
}
