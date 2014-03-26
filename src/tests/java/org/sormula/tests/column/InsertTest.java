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
package org.sormula.tests.column;

import java.util.ArrayList;

import org.sormula.SormulaException;
import org.sormula.annotation.Column;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests inserts when row has {@link Column#readOnly()}. This test must run first so that
 * test data is inserted for select, update, and delete tests.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="column.insert")
public class InsertTest extends DatabaseTest<ColumnTestRow>
{
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        createTable(ColumnTestRow.class, 
            "CREATE TABLE " + getSchemaPrefix() + ColumnTestRow.class.getSimpleName() + " (" +
            " id INTEGER PRIMARY KEY," +
            " readonlytest INTEGER DEFAULT " + ColumnTestRow.READ_ONLY_VALUE + "," + // default value to test readonly column
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
    public void insertReadOnly() throws SormulaException
    {
        begin();
        ArrayList<ColumnTestRow> list = new ArrayList<>();
        
        for (int i = 1; i < 10; ++i)
        {
            list.add(new ColumnTestRow(i, "Column test " + i));
        }
        
        getTable().insertAll(list);
        commit();
    }
}
