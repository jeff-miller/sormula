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
package org.sormula.tests.active.cascade.lazy;

import org.sormula.SormulaException;
import org.sormula.active.ActiveDatabase;
import org.sormula.active.ActiveTable;
import org.sormula.tests.DatabaseTest;
import org.sormula.tests.active.ActiveDatabaseTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * TODO fix db not closed in one or more tests in this package
 * 
 * Inserts test records for org.sormula.tests.active.cascade.lazy. 
 * No update or delete tests since they are tested in other packages.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="active.insert")
public class InsertTest extends ActiveDatabaseTest<SormulaTestParentLazyAR>
{
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        createTable(SormulaTestParentLazyAR.class, 
            "CREATE TABLE " + getSchemaPrefix() + SormulaTestParentLazyAR.class.getSimpleName() + " (" +
            " parentid INTEGER NOT NULL PRIMARY KEY," +
            " child1id INTEGER," +
            " description VARCHAR(60)" +
            ")"
        );
        
        // create child table for 1 to 1 relationship
        DatabaseTest<SormulaTestChild1LazyAR> child1 = new DatabaseTest<SormulaTestChild1LazyAR>();
        child1.openDatabase();
        child1.createTable(SormulaTestChild1LazyAR.class, 
                "CREATE TABLE " + getSchemaPrefix() + SormulaTestChild1LazyAR.class.getSimpleName() + " (" +
                " id INTEGER NOT NULL PRIMARY KEY," +
                " description VARCHAR(60)" +
                ")"
            );
        child1.closeDatabase();
        
        // create child table for 1 to n relationship
        DatabaseTest<SormulaTestChildNLazyAR> childN = new DatabaseTest<SormulaTestChildNLazyAR>();
        childN.openDatabase();
        childN.createTable(SormulaTestChildNLazyAR.class, 
                "CREATE TABLE " + getSchemaPrefix() + SormulaTestChildNLazyAR.class.getSimpleName() + " (" +
                " id INTEGER NOT NULL PRIMARY KEY," +
                " parentid INTEGER NOT NULL," +
                " description VARCHAR(60)" +
                ")"
            );
        childN.closeDatabase();
    }
    
    
    @AfterClass
    public void tearDown() throws Exception
    {
        closeDatabase();
    }
    
    
    @Test
    public void insertOneToOneLazyAR() 
    {
        insertOneToOneLazyAR(101, 1019);
        insertOneToOneLazyAR(102, 1029);
        insertOneToOneLazyAR(103, 1039);
        insertOneToOneLazyAR(104, 1049);
    }
    void insertOneToOneLazyAR(int parentId, int childId)
    {
        ActiveDatabase activeDatabase = getActiveDatabase();
        
        SormulaTestParentLazyAR parent = new SormulaTestParentLazyAR(parentId, "Insert parent " + parentId);
        parent.attach(activeDatabase); // only need to attach parent, child will be attached by cascade
        SormulaTestChild1LazyAR child1 = new SormulaTestChild1LazyAR(childId, "1-to-1 Child of parent " + parentId);
        parent.setChild1Id(childId);
        parent.setChild(child1);
        assert parent.insert() == 1 : "insertOneToOneLazyAR did not insert parent";
        
        // verify that child was inserted
        ActiveTable<SormulaTestChild1LazyAR> child1Table = new ActiveTable<SormulaTestChild1LazyAR>(activeDatabase, SormulaTestChild1LazyAR.class);
        SormulaTestChild1LazyAR c1 = child1Table.select(childId);
        assert c1 != null : "child " + childId + " was not inserted";
    }
    
    
    @Test
    public void insertOneToManyListLazyAR() throws SormulaException
    {
        insertOneToManyListLazyAR(205, 2500);
        insertOneToManyListLazyAR(204, 2400);
        insertOneToManyListLazyAR(203, 2300);
        insertOneToManyListLazyAR(202, 2200);
        insertOneToManyListLazyAR(201, 2100);
    }
    void insertOneToManyListLazyAR(int parentId, int childId) throws SormulaException
    {
        ActiveDatabase activeDatabase = getActiveDatabase();
        SormulaTestParentLazyAR parent = new SormulaTestParentLazyAR(parentId, "LazyAR Insert parent " + parentId);
        parent.attach(activeDatabase); // only need to attach parent, children will be attached by cascade
        
        for (int i = 1; i <= 20; ++i)
        {
            SormulaTestChildNLazyAR c = new SormulaTestChildNLazyAR(childId + i, "LazyAR Child of parent " + parentId);
            parent.add(c);
        }
        
        assert parent.insert() == 1 : "insertOneToManyListLazyAR did not insert parent";
        
        // verify that all children were inserted
        ActiveTable<SormulaTestChildNLazyAR> childTable = new ActiveTable<SormulaTestChildNLazyAR>(activeDatabase, SormulaTestChildNLazyAR.class);
        for (SormulaTestChildNLazyAR c: parent.getChildList())
        {
            // verify that child was inserted
            assert childTable.select(c.getId()) != null : "LazyAR child " + c.getId() + " was not inserted";
        }
    }
}
