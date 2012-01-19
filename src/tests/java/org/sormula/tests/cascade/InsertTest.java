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
package org.sormula.tests.cascade;

import java.util.HashMap;
import java.util.Map;

import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.operation.ScalarSelectOperation;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests cascade inserts for {@linkplain SormulaTestParent}.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="cascade.insert")
public class InsertTest extends DatabaseTest<SormulaTestParent>
{
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        createTable(SormulaTestParent.class, 
            "CREATE TABLE " + getSchemaPrefix() + SormulaTestParent.class.getSimpleName() + " (" +
            " id INTEGER NOT NULL PRIMARY KEY," +
            " child1id INTEGER," +
            " description VARCHAR(60)" +
            ")"
        );
        
        // create child table for 1 to 1 relationship
        DatabaseTest<SormulaTestChild1> child1 = new DatabaseTest<SormulaTestChild1>();
        child1.openDatabase();
        child1.createTable(SormulaTestChild1.class, 
                "CREATE TABLE " + getSchemaPrefix() + SormulaTestChild1.class.getSimpleName() + " (" +
                " id INTEGER NOT NULL PRIMARY KEY," +
                " description VARCHAR(60)" +
                ")"
            );
        child1.closeDatabase();
        
        // create child table for 1 to n relationship
        DatabaseTest<SormulaTestChildN> childN = new DatabaseTest<SormulaTestChildN>();
        childN.openDatabase();
        childN.createTable(SormulaTestChildN.class, 
                "CREATE TABLE " + getSchemaPrefix() + SormulaTestChildN.class.getSimpleName() + " (" +
                " id INTEGER NOT NULL PRIMARY KEY," +
                " parentid INTEGER NOT NULL," +
                " description VARCHAR(60)" +
                ")"
            );
        childN.closeDatabase();
        
        // create child table for map relationship
        DatabaseTest<SormulaTestChildM> childM = new DatabaseTest<SormulaTestChildM>();
        childM.openDatabase();
        childM.createTable(SormulaTestChildM.class, 
                "CREATE TABLE " + getSchemaPrefix() + SormulaTestChildM.class.getSimpleName() + " (" +
                " id INTEGER NOT NULL PRIMARY KEY," +
                " parentid INTEGER NOT NULL," +
                " description VARCHAR(60)" +
                ")"
            );
        childM.closeDatabase();
    }
    
    
    @AfterClass
    public void tearDown() throws Exception
    {
        closeDatabase();
    }
    
    
    @Test
    public void insertOneToOne() throws SormulaException
    {
        begin();
        insertOneToOne(101, 1019);
        insertOneToOne(102, 1029);
        insertOneToOne(103, 1039);
        insertOneToOne(104, 1049);
        commit();
    }
    void insertOneToOne(int parentId, int childId) throws SormulaException
    {
        SormulaTestParent parent = new SormulaTestParent(parentId, "Insert parent " + parentId);
        SormulaTestChild1 child1 = new SormulaTestChild1(childId, "1-to-1 Child of parent " + parentId);
        parent.setChild1Id(childId);
        parent.setChild(child1);
        assert getTable().insert(parent) == 1 : "insertOneToOne did not insert parent";
        
        // verify that child was inserted
        Table<SormulaTestChild1> child1Table = getDatabase().getTable(SormulaTestChild1.class);
        assert child1Table.select(child1.getId()) != null : "child " + child1.getId() + " was not inserted"; 
    }
    
    
    @Test
    public void insertOneToManyList() throws SormulaException
    {
        begin();
        insertOneToManyList(205, 2500);
        insertOneToManyList(204, 2400);
        insertOneToManyList(203, 2300);
        insertOneToManyList(202, 2200);
        insertOneToManyList(201, 2100);
        commit();
    }
    void insertOneToManyList(int parentId, int childId) throws SormulaException
    {
        SormulaTestParent parent = new SormulaTestParent(parentId, "Insert parent " + parentId);
        
        for (int i = 1; i <= 20; ++i)
        {
            SormulaTestChildN c = new SormulaTestChildN(childId + i, "Child of parent " + parentId);
            parent.add(c);
        }
        
        assert getTable().insert(parent) == 1 : "insertOneToManyList did not insert parent";
        
        // verify that all children were inserted
        Table<SormulaTestChildN> childTable = getDatabase().getTable(SormulaTestChildN.class);
        ScalarSelectOperation<SormulaTestChildN> operation = new ScalarSelectOperation<SormulaTestChildN>(childTable);
        
        for (SormulaTestChildN c: parent.getChildList())
        {
            operation.setParameters(c.getId());
            operation.execute();
            assert operation.readNext() != null : "child " + c.getId() + " was not inserted"; 
        }
        
        operation.close();
    }
    

    @Test
    public void insertOneToManyMap() throws SormulaException
    {
        begin();
        insertOneToManyMap(305, 3500);
        insertOneToManyMap(304, 3400);
        insertOneToManyMap(303, 3300);
        insertOneToManyMap(302, 3200);
        insertOneToManyMap(301, 3100);
        commit();
    }
    void insertOneToManyMap(int parentId, int childId) throws SormulaException
    {
        SormulaTestParent parent = new SormulaTestParent(parentId, "Insert parent " + parentId);
        Map<Integer, SormulaTestChildM> map = new HashMap<Integer, SormulaTestChildM>(50);
        parent.setChildMap(map);
        
        for (int i = 1; i <= 20; ++i)
        {
            SormulaTestChildM c = new SormulaTestChildM(childId + i, "Child of parent " + parentId);
            map.put(c.getId(), c);
            c.setParentId(parentId);
        }
        
        assert getTable().insert(parent) == 1 : "insertOneToManyMap did not insert parent";
        
        // verify that all children were inserted
        Table<SormulaTestChildM> childTable = getDatabase().getTable(SormulaTestChildM.class);
        ScalarSelectOperation<SormulaTestChildM> operation = new ScalarSelectOperation<SormulaTestChildM>(childTable);
        for (SormulaTestChildM c: parent.getChildMap().values())
        {
            operation.setParameters(c.getId());
            operation.execute();
            assert operation.readNext() != null : "child " + c.getId() + " was not inserted"; 
        }
        operation.close();
    }
}
