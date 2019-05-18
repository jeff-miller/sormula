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
package org.sormula.tests.zeroannotation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sormula.SormulaException;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.Test;


/**
 * Tests some insert operations for row with no annotations. This test must run first so that
 * test data is inserted for select, update, and delete tests.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="zeroannotation.insert")
public class InsertTest extends DatabaseTest<ZeroAnnotationTest>
{
    boolean preMethod;
    boolean postMethod;

    
    @Override
    protected void open() throws Exception
    {
        super.open();
        createTable(ZeroAnnotationTest.class, 
            "CREATE TABLE " + getSchemaPrefix() + ZeroAnnotationTest.class.getSimpleName() + " (" +
            " zatid INTEGER NOT NULL PRIMARY KEY," +
            " type SMALLINT," +
            " description VARCHAR(30)," +
            " childid INTEGER NOT NULL" +
            ")"
        );

        // child table
        DatabaseTest<ZeroAnnotationChild> child = new DatabaseTest<>();
        child.openDatabase();
        child.createTable(ZeroAnnotationChild.class, 
                "CREATE TABLE " + getSchemaPrefix() + ZeroAnnotationChild.class.getSimpleName() + " (" +
                " childid INTEGER NOT NULL PRIMARY KEY," +
                " zatid INTEGER NOT NULL" +
                ")"
            );
        child.closeDatabase();
    }
    
    
    @Test
    public void insertOne() throws SormulaException
    {
        begin();
        assert getTable().insert(new ZeroAnnotationTest(13, 1, "Insert one")) == 1 : "insert one failed";
        commit();
    }
    
    
    @Test
    public void insertCollection() throws SormulaException
    {
        ArrayList<ZeroAnnotationTest> list = new ArrayList<>();
        int childId = 1;
        
        for (int zatId = 101; zatId < 200; ++zatId)
        {
            ZeroAnnotationTest zat = new ZeroAnnotationTest(zatId, 2, "Insert collection " + zatId);
            list.add(zat);
            
            // add test children for each default cascade
            switch (zatId % 4) // insure only 1 parent for a child
            {
                case 0:
                    // list
                    List<ZeroAnnotationChild> testList = zat.getTestList();
                    for (int i = 0; i < 3; ++i)
                    {
                        testList.add(new ZeroAnnotationChild(childId++, zatId));
                    }
                    break;
                    
                case 1:
                    // map
                    Map<Integer, ZeroAnnotationChild> testMap = zat.getTestMap();
                    for (int i = 0; i < 3; ++i)
                    {
                        ZeroAnnotationChild zac = new ZeroAnnotationChild(childId++, zatId);
                        testMap.put(zac.hashCode(), zac);
                    }
                    break;
                    
                case 2:
                    // array
                    ZeroAnnotationChild[] testArray = new ZeroAnnotationChild[3];
                    for (int i = 0; i < testArray.length; ++i)
                    {
                        testArray[i] = new ZeroAnnotationChild(childId++, zatId);
                    }
                    zat.setTestArray(testArray);// create only when array has values
                    break;
                    
                case 3:
                    // class
                    zat.setTestChild(new ZeroAnnotationChild(childId++, zatId));
                    break;
            }
        }
        
        begin();
        assert getTable().insertAll(list) == list.size() : "insert collection failed";
        commit();
    }
}
