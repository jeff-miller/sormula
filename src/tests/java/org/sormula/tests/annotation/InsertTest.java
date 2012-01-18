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
package org.sormula.tests.annotation;

import java.util.ArrayList;

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
    boolean preMethod;
    boolean postMethod;

    
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        
        // SormulaTestATable is used for tables usinng row SormulaTestA.class
        getDatabase().addTable(new SormulaTestATable(getDatabase(), SormulaTestA.class));
        
        createTable(SormulaTestA.class, 
            "CREATE TABLE " + getSchemaPrefix() + " STA (" +
            " id INTEGER NOT NULL PRIMARY KEY," +
            " type SMALLINT," +
            " description VARCHAR(30)," +
            " unusedInt INTEGER NOT NULL" +
            ")"
        );
    }
    
    
    @AfterClass
    public void tearDown() throws Exception
    {
        closeDatabase();
    }
    
    
    @Test
    public void insertCollection() throws SormulaException
    {
        ArrayList<SormulaTestA> list = new ArrayList<SormulaTestA>();
        
        for (int i = 101; i < 400; ++i)
        {
            list.add(new SormulaTestA(i, i%5, "Insert collection " + i));
        }
        
        assert getTable().insertAll(list) == list.size() : "insert collection failed";
    }
}
