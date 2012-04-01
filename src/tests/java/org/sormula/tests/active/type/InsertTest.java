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
package org.sormula.tests.active.type;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.sormula.SormulaException;
import org.sormula.active.ActiveDatabase;
import org.sormula.active.ActiveTable;
import org.sormula.annotation.ExplicitType;
import org.sormula.tests.active.ActiveDatabaseTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests inserts type definitions when annotations are on active database class
 * not row or operation class.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="active.insert")
public class InsertTest extends ActiveDatabaseTest<SormulaTypeTestAR>
{
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        
        createTable(SormulaTypeTestAR.class, 
            "CREATE TABLE " + getSchemaPrefix() + SormulaTypeTestAR.class.getSimpleName() + " (" +
            " id INTEGER NOT NULL PRIMARY KEY," +
            " type SMALLINT," +
            " description VARCHAR(30)," +
            " test1 INTEGER NOT NULL " +
            ")"
        );
    }
    
    
    @AfterClass
    public void tearDown() throws Exception
    {
        closeDatabase();
    }
    
    
    @Test
    public void insertTypeTestAR() throws SormulaException
    {
        ArrayList<SormulaTypeTestAR> list = new ArrayList<SormulaTypeTestAR>();
        
        for (int id = 101; id < 400; ++id)
        {
            SormulaTypeTestAR r = new SormulaTypeTestAR(id, id%5, "Insert collection " + id);
            r.setTest1(new Test1(id%4));
            list.add(r);
        }
        
        ActiveDatabase activeDatabase = new CustomActiveDatabase(getDataSource(), getSchema());
        ActiveTable<SormulaTypeTestAR> table = new ActiveTable<SormulaTypeTestAR>(activeDatabase, SormulaTypeTestAR.class);
        
        assert table.insertAll(list) == list.size() : "insert collection failed";
        
        // verify test types are correct
        List<SormulaTypeTestAR> selected = table.selectAll();
        assert list.size() == selected.size() : "inserted size not same as selected size";
        for (SormulaTypeTestAR r: selected)
        {
            assert r.getTest1().intValue() == r.getId()%4 : "Test1 type is not correct";
        }
    }
}


@ExplicitType(type=Test1.class, translator=Test1Translator.class)
class CustomActiveDatabase extends ActiveDatabase
{
    private static final long serialVersionUID = 1L;
    
    public CustomActiveDatabase(DataSource dataSource, String schema)
    {
        super(dataSource, schema);
    }

    public CustomActiveDatabase(DataSource dataSource)
    {
        super(dataSource);
    }
}
