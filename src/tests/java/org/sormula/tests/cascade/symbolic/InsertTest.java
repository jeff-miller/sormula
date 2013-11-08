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
package org.sormula.tests.cascade.symbolic;

import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests cascade inserts for {@link SormulaSymParent}.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="cascade.insert")
public class InsertTest extends DatabaseTest<SormulaSymParent>
{
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        createTable(SormulaSymParent.class, 
            "CREATE TABLE " + getSchemaPrefix() + SormulaSymParent.class.getSimpleName() + " (" +
            " parentid INTEGER PRIMARY KEY," +
            " description VARCHAR(60)" +
            ")"
        );
        
        // create child table for 1 to n relationship
        DatabaseTest<SormulaSymChild> child = new DatabaseTest<>();
        child.openDatabase();
        child.createTable(SormulaSymChild.class, 
                "CREATE TABLE " + getSchemaPrefix() + SormulaSymChild.class.getSimpleName() + " (" +
                " childid INTEGER PRIMARY KEY," +
                " parentid INTEGER NOT NULL," +
                " description VARCHAR(60)" +
                ")"
            );
        child.closeDatabase();
    }
    
    
    @AfterClass
    public void tearDown() throws Exception
    {
        closeDatabase();
    }
    
    
    @Test
    public void insert1() throws SormulaException
    {
        begin();
        Table<SormulaSymParent1> parentTable = getDatabase().getTable(SormulaSymParent1.class);
        insertOneToManyList(parentTable, new SormulaSymParent1(12, "Insert sym parent 1"), 1200);
        insertOneToManyList(parentTable, new SormulaSymParent1(13, "Insert sym parent 1"), 1300);
        insertOneToManyList(parentTable, new SormulaSymParent1(11, "Insert sym parent 1"), 1100);
        commit();
    }
    
    
    @Test
    public void insert2() throws SormulaException
    {
        begin();
        Table<SormulaSymParent2> parentTable = getDatabase().getTable(SormulaSymParent2.class);
        insertOneToManyList(parentTable, new SormulaSymParent2(22, "Insert sym parent 2"), 2200);
        insertOneToManyList(parentTable, new SormulaSymParent2(23, "Insert sym parent 2"), 2300);
        insertOneToManyList(parentTable, new SormulaSymParent2(21, "Insert sym parent 2"), 2100);
        commit();
    }
    
    
    @Test
    public void insert3() throws SormulaException
    {
        begin();
        Table<SormulaSymParent3> parentTable = getDatabase().getTable(SormulaSymParent3.class);
        insertOneToManyList(parentTable, new SormulaSymParent3(32, "Insert sym parent 3"), 3200);
        insertOneToManyList(parentTable, new SormulaSymParent3(33, "Insert sym parent 3"), 3300);
        insertOneToManyList(parentTable, new SormulaSymParent3(31, "Insert sym parent 3"), 3100);
        commit();
    }
    
    
    @Test
    public void insert4() throws SormulaException
    {
        begin();
        Table<SormulaSymParent4> parentTable = getDatabase().getTable(SormulaSymParent4.class);
        insertOneToManyList(parentTable, new SormulaSymParent4(42, "Insert sym parent 4"), 4200);
        insertOneToManyList(parentTable, new SormulaSymParent4(43, "Insert sym parent 4"), 4300);
        insertOneToManyList(parentTable, new SormulaSymParent4(41, "Insert sym parent 4"), 4100);
        commit();
    }
    
    
    <T extends SormulaSymParent> void insertOneToManyList(Table<T> parentTable, T parent, int childBaseId) throws SormulaException
    {
        for (int i = 1; i <= 20; ++i)
        {
            SormulaSymChild child = new SormulaSymChild(childBaseId + i, "sym child for parent " + parent.getParentId());
            parent.add(child);
        }
        
        assert parentTable.insert(parent) == 1 : "insert sym parent failed";
    }
}
