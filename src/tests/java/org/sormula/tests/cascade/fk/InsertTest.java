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
package org.sormula.tests.cascade.fk;

import java.util.HashMap;
import java.util.Map;

import org.sormula.SormulaException;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests cascade inserts for {@link SormulaFKTestParent}.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="cascade.insert")
public class InsertTest extends DatabaseTest<SormulaFKTestParent>
{
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        createTable(SormulaFKTestParent.class, 
            "CREATE TABLE " + getSchemaPrefix() + SormulaFKTestParent.class.getSimpleName() + " (" +
            " parentid INTEGER NOT NULL PRIMARY KEY," +
            " child1id INTEGER," +
            " description VARCHAR(60)" +
            ")"
        );
        
        // create child table for 1 to n relationship
        DatabaseTest<SormulaFKTestChildN> childN = new DatabaseTest<SormulaFKTestChildN>();
        childN.openDatabase();
        childN.createTable(SormulaFKTestChildN.class, 
                "CREATE TABLE " + getSchemaPrefix() + SormulaFKTestChildN.class.getSimpleName() + " (" +
                " id INTEGER NOT NULL PRIMARY KEY," +
                " parentid INTEGER NOT NULL," +
                " description VARCHAR(60)" +
                ")"
            );
        childN.closeDatabase();
        
        // create child table for map relationship
        DatabaseTest<SormulaFKTestChildM> childM = new DatabaseTest<SormulaFKTestChildM>();
        childM.openDatabase();
        childM.createTable(SormulaFKTestChildM.class, 
                "CREATE TABLE " + getSchemaPrefix() + SormulaFKTestChildM.class.getSimpleName() + " (" +
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
    public void insertOneToManyList() throws SormulaException
    {
        begin();
        insertOneToManyList(205, 2500);
        commit();
    }
    void insertOneToManyList(int parentId, int childId) throws SormulaException
    {
        SormulaFKTestParent parent = new SormulaFKTestParent(parentId, "Insert parent " + parentId);
        
        for (int i = 1; i <= 20; ++i)
        {
            SormulaFKTestChildN c = new SormulaFKTestChildN(childId + i, "Child of parent " + parentId);
            parent.add(c);
        }
        
        assert getTable().insert(parent) == 1 : "insertOneToManyList did not insert parent";
        
        // verify that all children have correct foreign key information
        for (SormulaFKTestChildN c: parent.getChildList())
        {
            // foreignKeyValueFields=...
            assert c.getParentId() == parent.getParentId() : "child " + c.getId() + " has wrong parent id";
            
            // foreignKeyReferenceField=...
            SormulaFKTestParent testParent = c.getParent();
            assert testParent != null : "child " + c.getId() + " has no parent reference";
            assert testParent.getParentId() == parent.getParentId() : "child " + c.getId() + " has wrong parent reference";
        }
    }

    
    @Test
    public void insertOneToManyMap() throws SormulaException
    {
        begin();
        insertOneToManyMap(301, 3100);
        commit();
    }
    void insertOneToManyMap(int parentId, int childId) throws SormulaException
    {
        SormulaFKTestParent parent = new SormulaFKTestParent(parentId, "Insert parent " + parentId);
        Map<Integer, SormulaFKTestChildM> map = new HashMap<Integer, SormulaFKTestChildM>(50);
        parent.setChildMap(map);
        
        for (int i = 1; i <= 20; ++i)
        {
            SormulaFKTestChildM c = new SormulaFKTestChildM(childId + i, "Child of parent " + parentId);
            map.put(c.getId(), c);
            
            // dont' need to set parent since foreignKeyValueFields="parentId" 
            // c.setParentId(parentId);
        }
        
        assert getTable().insert(parent) == 1 : "insertOneToManyMap did not insert parent";
        
        // verify that all children have correct foreign key information
        for (SormulaFKTestChildM c: map.values())
        {
            // foreignKeyValueFields=...
            assert c.getParentId() == parent.getParentId() : "child " + c.getId() + " has wrong parent id";
            
            // foreignKeyReferenceField=...
            SormulaFKTestParent testParent = c.getSormulaFKTestParent();
            assert testParent != null : "child " + c.getId() + " has no parent reference";
            assert testParent.getParentId() == parent.getParentId() : "child " + c.getId() + " has wrong parent reference";
        }
    }
}
