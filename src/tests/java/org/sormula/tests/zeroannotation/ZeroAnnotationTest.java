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
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 * Row class with no annotations. First column is primary key when no annotation is specified.
 * Used by {@link InsertTest}, {@link SelectTest}, {@link UpdateTest},
 * and {@link DeleteTest}.
 * <p>
 * No annotations are needed for insert, update, delete, and select by primary key if row
 * class conforms to the following:
 * <ul>
 * <li>Table name and class name are the same</li>
 * <li>Column names and class field names are the same</li>
 * <li>First field corresponds to primary column</li>
 * <li>All fields in class are columns in table</li>
 * </ul>
 * 
 * @author Jeff Miller
 */
public class ZeroAnnotationTest
{
    int zatId; // this is primary key by default on JVM's that reflect this field first (most JVM's do)
    int type;
    String description;

    // tests default OneToManyCascade for a List
    List<ZeroAnnotationChild> testList;

    // tests default OneToManyCascade for a Map
    Map<Integer, ZeroAnnotationChild> testMap; 

    // tests default OneToManyCascade for an array
    ZeroAnnotationChild[] testArray;

    // tests default OneToOneCascade for a class
    int childId; // required for OneToOneCascade since default is primaryKey select
    ZeroAnnotationChild testChild;
    
    
    public ZeroAnnotationTest()
    {
    }

    
    public ZeroAnnotationTest(int id, int type, String description)
    {
        this.zatId = id;
        this.type = type;
        this.description = description;
        
        testList = new ArrayList<>();
        testMap = new HashMap<>();
        // init in test only when used to avoid array with nulls testArray = new ZeroAnnotationChild[3];
    }
    
    
    public int getZatId()
    {
        return zatId;
    }
    public void setZatId(int zatId)
    {
        this.zatId = zatId;
    }
    
    
    public String getDescription()
    {
        return description;
    }
    public void setDescription(String description)
    {
        this.description = description;
    }


    public int getType()
    {
        return type;
    }
    public void setType(int type)
    {
        this.type = type;
    }
    
    
    public List<ZeroAnnotationChild> getTestList()
    {
        return testList;
    }
    public void setTestList(List<ZeroAnnotationChild> testList)
    {
        this.testList = testList;
    }


    public Map<Integer, ZeroAnnotationChild> getTestMap()
    {
        return testMap;
    }
    public void setTestMap(Map<Integer, ZeroAnnotationChild> testMap)
    {
        this.testMap = testMap;
    }


    public ZeroAnnotationChild[] getTestArray()
    {
        return testArray;
    }
    public void setTestArray(ZeroAnnotationChild[] testArray)
    {
        this.testArray = testArray;
    }


    public int getChildId()
    {
        return childId;
    }
    public void setChildId(int childId)
    {
        this.childId = childId;
    }


    public ZeroAnnotationChild getTestChild()
    {
        return testChild;
    }
    public void setTestChild(ZeroAnnotationChild testChild)
    {
        this.testChild = testChild;
        if (testChild != null) childId = testChild.getChildId();
        else childId = 0;
    }
}
