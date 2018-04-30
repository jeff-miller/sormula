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
package org.sormula.tests.selector;

import java.util.ArrayList;
import java.util.Collections;

import org.sormula.SormulaException;
import org.sormula.selector.PaginatedSelector;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Inserts test data for {@link PaginatedSelector} tests.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="operation.insert")
public class PsInsert extends DatabaseTest<SormulaPsTest>
{
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        createTable(SormulaPsTest.class, 
            "CREATE TABLE " + getSchemaPrefix() + SormulaPsTest.class.getSimpleName() + " (" +
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
    public void insert() throws SormulaException
    {
        ArrayList<SormulaPsTest> list = new ArrayList<>(200);
        
        // insert 2 types so that subset can be selected for testing
        // type 1 has 100 for testing page sizes with same number of rows
        // type 2 has 173 for testing last page with different number of rows
        int id = 1;
        for ( ; id <= 100; ++id)
        {
            list.add(new SormulaPsTest(id, 1, "type 1 " + id));
        }
        for (++id; id <= 173; ++id)
        {
            list.add(new SormulaPsTest(id, 2, "type 2 " + id));
        }
        
        // insert in random order to avoid false positives that are coincidentally ordered in db
        Collections.shuffle(list);
        
        begin();
        assert getTable().insertAll(list) == list.size() : "insert test data failed";
        commit();
    }
}
