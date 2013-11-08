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
package org.sormula.tests.cascade.lazy;

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
 * Tests cascade inserts for {@link SormulaTestParentLazy1}.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="cascade.insert")
public class InsertTest extends DatabaseTest<SormulaTestParentLazy1>
{
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        createTable(SormulaTestParentLazy1.class, 
            "CREATE TABLE " + getSchemaPrefix() + "SormulaTestParentLazy (" +
            " id INTEGER NOT NULL PRIMARY KEY," +
            " description VARCHAR(60)" +
            ")"
        );
        
        // create child table for map relationship
        DatabaseTest<SormulaTestChildLazy> childM = new DatabaseTest<>();
        childM.openDatabase();
        childM.createTable(SormulaTestChildLazy.class, 
                "CREATE TABLE " + getSchemaPrefix() + SormulaTestChildLazy.class.getSimpleName() + " (" +
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
    public void insertOneToManyMapLazy() throws SormulaException
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
        SormulaTestParentLazy1 parent = new SormulaTestParentLazy1(parentId, "Insert parent " + parentId);
        Map<Integer, SormulaTestChildLazy> map = new HashMap<>(50);
        parent.setChildMap(map);
        
        for (int i = 1; i <= 20; ++i)
        {
            SormulaTestChildLazy c = new SormulaTestChildLazy(childId + i, "Child of parent " + parentId);
            map.put(c.getId(), c);
            c.setParentId(parentId);
        }
        
        assert getTable().insert(parent) == 1 : "insertOneToManyMap did not insert parent";
        
        // verify that all children were inserted
        Table<SormulaTestChildLazy> childTable = getDatabase().getTable(SormulaTestChildLazy.class);
        try (ScalarSelectOperation<SormulaTestChildLazy> operation = new ScalarSelectOperation<>(childTable))
        {
            for (SormulaTestChildLazy c: parent.getChildMap().values())
            {
                operation.setParameters(c.getId());
                operation.execute();
                assert operation.readNext() != null : "child " + c.getId() + " was not inserted"; 
            }
        }
    }
}
