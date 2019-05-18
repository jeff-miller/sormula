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
package org.sormula.tests.fieldaccess;

import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.annotation.Column;
import org.sormula.annotation.Row;
import org.sormula.operation.ScalarSelectOperation;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.Test;


/**
 * Insert tests for {@link Row#fieldAccess()} and {@link Column#fieldAccess()}.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="fieldaccess.insert")
public class InsertTest extends DatabaseTest<SormulaFaTestParent>
{
    @Override
    protected void open() throws Exception
    {
        super.open();
        createTable(SormulaFaTestParent.class, 
            "CREATE TABLE " + getSchemaPrefix() + SormulaFaTestParent.class.getSimpleName() + " (" +
            " parentid INTEGER NOT NULL PRIMARY KEY," +
            " description VARCHAR(60)," +
            " other VARCHAR(10)" +
            ")"
        );
        
        // create child table for 1 to n relationship
        DatabaseTest<SormulaFaTestChild> childN = new DatabaseTest<>();
        childN.openDatabase();
        childN.createTable(SormulaFaTestChild.class, 
                "CREATE TABLE " + getSchemaPrefix() + SormulaFaTestChild.class.getSimpleName() + " (" +
                " childid INTEGER NOT NULL PRIMARY KEY," +
                " parentid INTEGER NOT NULL," +
                " description VARCHAR(60)" +
                ")"
            );
        childN.closeDatabase();
    }
    

    @Test
    public void insertOneToManyList() throws SormulaException
    {
        begin();
        insertOneToManyList(202, 2200);
        insertOneToManyList(205, 2500);
        insertOneToManyList(203, 2300);
        insertOneToManyList(201, 2100);
        insertOneToManyList(204, 2400);
        commit();
    }
    void insertOneToManyList(int parentId, int childId) throws SormulaException
    {
        SormulaFaTestParent parent = new SormulaFaTestParent(parentId, "Insert parent " + parentId, "");
        
        for (int i = 1; i <= 20; ++i)
        {
            SormulaFaTestChild c = new SormulaFaTestChild(childId + i, "Child of parent " + parentId);
            parent.add(c);
        }
        
        assert getTable().insert(parent) == 1 : "insertOneToManyList did not insert parent";
        
        // verify that all children were inserted
        Table<SormulaFaTestChild> childTable = getDatabase().getTable(SormulaFaTestChild.class);
        
        try (ScalarSelectOperation<SormulaFaTestChild> operation = new ScalarSelectOperation<>(childTable))
        {
            for (SormulaFaTestChild c: parent.getChildList())
            {
                operation.setParameters(c.getChildId());
                operation.execute();
                assert operation.readNext() != null : "child " + c.getChildId() + " was not inserted"; 
            }
        }
    }
}
