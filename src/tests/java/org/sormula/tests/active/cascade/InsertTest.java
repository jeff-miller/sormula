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
package org.sormula.tests.active.cascade;

import org.sormula.SormulaException;
import org.sormula.active.ActiveDatabase;
import org.sormula.active.ActiveTable;
import org.sormula.tests.DatabaseTest;
import org.sormula.tests.active.ActiveDatabaseTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests cascade inserts for org.sormula.active package. 
 * <p>
 * No update or delete tests were created since the cascade and attach tests
 * are tested here. The other features are delegated to sormula classes that 
 * are tested elsewhere.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="active.insert")
public class InsertTest extends ActiveDatabaseTest<SormulaTestParentAR>
{
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        createTable(SormulaTestParentAR.class, 
            "CREATE TABLE " + getSchemaPrefix() + SormulaTestParentAR.class.getSimpleName() + " (" +
            " id INTEGER NOT NULL PRIMARY KEY," +
            " child1id INTEGER," +
            " description VARCHAR(60)" +
            ")"
        );
        
        // create child table for 1 to 1 relationship
        DatabaseTest<SormulaTestChild1AR> child1 = new DatabaseTest<>();
        child1.openDatabase();
        child1.createTable(SormulaTestChild1AR.class, 
                "CREATE TABLE " + getSchemaPrefix() + SormulaTestChild1AR.class.getSimpleName() + " (" +
                " id INTEGER NOT NULL PRIMARY KEY," +
                " description VARCHAR(60)" +
                ")"
            );
        child1.closeDatabase();
        
        // create child table for 1 to n relationship
        DatabaseTest<SormulaTestChildNAR> childN = new DatabaseTest<>();
        childN.openDatabase();
        childN.createTable(SormulaTestChildNAR.class, 
                "CREATE TABLE " + getSchemaPrefix() + SormulaTestChildNAR.class.getSimpleName() + " (" +
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
    public void insertOneToOneAR() 
    {
        insertOneToOneAR(101, 1019);
        insertOneToOneAR(102, 1029);
        insertOneToOneAR(103, 1039);
        insertOneToOneAR(104, 1049);
    }
    void insertOneToOneAR(int parentId, int childId)
    {
        ActiveDatabase activeDatabase = getActiveDatabase();
        
        SormulaTestParentAR parent = new SormulaTestParentAR(parentId, "Insert parent " + parentId);
        parent.attach(activeDatabase); // only need to attach parent, child will be attached by cascade
        SormulaTestChild1AR child1 = new SormulaTestChild1AR(childId, "1-to-1 Child of parent " + parentId);
        parent.setChild1Id(childId);
        parent.setChild(child1);
        assert parent.insert() == 1 : "insertOneToOne AR did not insert parent";
        
        // verify that child was inserted
        ActiveTable<SormulaTestChild1AR> child1Table = new ActiveTable<>(activeDatabase, SormulaTestChild1AR.class);
        SormulaTestChild1AR c1 = child1Table.select(child1.getId());
        assert c1 != null : "child " + child1.getId() + " was not inserted";
        
        // verify that child was attached
        assert child1.getActiveDatabase() == activeDatabase : "child was not attached to active database";
    }
    
    
    @Test
    public void insertOneToManyListAR() throws SormulaException
    {
        insertOneToManyListAR(205, 2500);
        insertOneToManyListAR(204, 2400);
        insertOneToManyListAR(203, 2300);
        insertOneToManyListAR(202, 2200);
        insertOneToManyListAR(201, 2100);
    }
    void insertOneToManyListAR(int parentId, int childId) throws SormulaException
    {
        ActiveDatabase activeDatabase = getActiveDatabase();
        SormulaTestParentAR parent = new SormulaTestParentAR(parentId, "AR Insert parent " + parentId);
        parent.attach(activeDatabase); // only need to attach parent, children will be attached by cascade
        
        for (int i = 1; i <= 20; ++i)
        {
            SormulaTestChildNAR c = new SormulaTestChildNAR(childId + i, "AR Child of parent " + parentId);
            parent.add(c);
        }
        
        assert parent.insert() == 1 : "insertOneToManyListAR did not insert parent";
        
        // verify that all children were inserted
        ActiveTable<SormulaTestChildNAR> childTable = new ActiveTable<>(activeDatabase, SormulaTestChildNAR.class);
        for (SormulaTestChildNAR c: parent.getChildList())
        {
            // verify that child was attached
            assert c.getActiveDatabase() == activeDatabase : "child was not attached to active database";

            assert childTable.select(c.getId()) != null : "AR child " + c.getId() + " was not inserted";
        }
    }
}
