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
package org.sormula.tests.cascade;

import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.annotation.Row;
import org.sormula.operation.ScalarSelectOperation;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests cascade inserts for {@link SormulaTestParentArrayCascade}.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="cascade.ac.insert")
public class InsertTestArrayCascade extends DatabaseTest<SormulaTestParentArrayCascade>
{
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        createTable(SormulaTestParentArrayCascade.class, 
                "CREATE TABLE " + getSchemaPrefix() + 
                SormulaTestParentArrayCascade.class.getAnnotation(Row.class).tableName() + " (" +
                " id INTEGER NOT NULL PRIMARY KEY," +
                " description VARCHAR(60)" +
                ")"
            );
            
            // create child table for 1 to n relationship
            DatabaseTest<SormulaTestChildNArrayCascade> childN = new DatabaseTest<>();
            childN.openDatabase();
            childN.createTable(SormulaTestChildNArrayCascade.class, 
                    "CREATE TABLE " + getSchemaPrefix() + 
                    SormulaTestChildNArrayCascade.class.getAnnotation(Row.class).tableName() + " (" +
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
    public void insertOneToManyArray() throws SormulaException
    {
        begin();
        SormulaTestParentArrayCascade parent = new SormulaTestParentArrayCascade(700, "AC Insert parent " + 700);
        
        SormulaTestChildNArrayCascade[] children = new SormulaTestChildNArrayCascade[10]; 
        for (int i = 0; i < children.length; ++i)
        {
            children[i] = new SormulaTestChildNArrayCascade(701 + i, "AC Child of parent " + 700);
        }
        parent.setChildren(children);
        
        assert getTable().insert(parent) == 1 : "insertOneToManyArray did not insert parent";
        
        // verify that all children were inserted
        Table<SormulaTestChildNArrayCascade> childTable = getDatabase().getTable(SormulaTestChildNArrayCascade.class);
        ScalarSelectOperation<SormulaTestChildNArrayCascade> operation = new ScalarSelectOperation<>(childTable);
        for (SormulaTestChildNArrayCascade c: parent.getChildren())
        {
            operation.setParameters(c.getId());
            operation.execute();
            assert operation.readNext() != null : "child " + c.getId() + " was not inserted"; 
        }
        operation.close();
        commit();
    }
}
