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
package org.sormula.tests.operation.pk;

import java.util.ArrayList;

import org.sormula.SormulaException;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Creates table and inserts data for select tests.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="operation.insert")
public class InsertTestPK extends DatabaseTest<SormulaTestPK>
{
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        
        // order of columns is intentionally different from class fields
        createTable(SormulaTestPK.class, 
            "CREATE TABLE " + getSchemaPrefix() + SormulaTestPK.class.getSimpleName() + " (" +
            " other SMALLINT," +
            " type SMALLINT," +
            " description VARCHAR(30)," +
            " id INTEGER NOT NULL," +
            " primary key (id, type)" +
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
        begin();
        assert getTable().insert(new SormulaTestPK(13, 2, "Insert PK one 13-2")) == 1 : "insert PK one failed";
        assert getTable().insert(new SormulaTestPK(13, 1, "Insert PK one 13-1")) == 1 : "insert PK one failed";
        commit();
    }
    
    
    @Test
    public void insertCollection() throws SormulaException
    {
        ArrayList<SormulaTestPK> list = new ArrayList<>();
        
        for (int i = 101; i < 110; ++i)
        {
            list.add(new SormulaTestPK(i, 22, "Insert PK collection " + i + "-22"));
            list.add(new SormulaTestPK(i, 23, "Insert PK collection " + i + "-23"));
        }
        
        begin();
        assert getTable().insertAll(list) == list.size() : "insert PK collection failed";
        commit();
    }
}
