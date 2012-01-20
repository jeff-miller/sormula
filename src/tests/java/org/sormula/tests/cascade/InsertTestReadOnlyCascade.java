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
 * Tests cascade inserts for {@linkplain SormulaTestParentReadOnlyCascade}. Inserts
 * should not cascade.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="cascade.roc.insert")
public class InsertTestReadOnlyCascade extends DatabaseTest<SormulaTestParentReadOnlyCascade>
{
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        createTable(SormulaTestParentReadOnlyCascade.class, 
                "CREATE TABLE " + getSchemaPrefix() + 
                SormulaTestParentReadOnlyCascade.class.getAnnotation(Row.class).tableName() + " (" +
                " id INTEGER NOT NULL PRIMARY KEY," +
                " child1id INTEGER," +
                " description VARCHAR(60)" +
                ")"
            );
            
            // create child table for 1 to 1 relationship
            DatabaseTest<SormulaTestChild1ReadOnlyCascade> child1 = new DatabaseTest<SormulaTestChild1ReadOnlyCascade>();
            child1.openDatabase();
            child1.createTable(SormulaTestChild1ReadOnlyCascade.class, 
                    "CREATE TABLE " + getSchemaPrefix() + 
                    SormulaTestChild1ReadOnlyCascade.class.getAnnotation(Row.class).tableName() + " (" +
                    " id INTEGER NOT NULL PRIMARY KEY," +
                    " description VARCHAR(60)" +
                    ")"
                );
            child1.closeDatabase();
            
            // create child table for 1 to n relationship
            DatabaseTest<SormulaTestChildNReadOnlyCascade> childN = new DatabaseTest<SormulaTestChildNReadOnlyCascade>();
            childN.openDatabase();
            childN.createTable(SormulaTestChildNReadOnlyCascade.class, 
                    "CREATE TABLE " + getSchemaPrefix() + 
                    SormulaTestChildNReadOnlyCascade.class.getAnnotation(Row.class).tableName() + " (" +
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
    void insertOneToOne() throws SormulaException
    {
        begin();
        SormulaTestParentReadOnlyCascade parent = new SormulaTestParentReadOnlyCascade(900, "ROC Insert parent 900");
        SormulaTestChild1ReadOnlyCascade child1 = new SormulaTestChild1ReadOnlyCascade(9900, "ROC 1-to-1 Child of parent 9900");
        parent.setChild1Id(9900);
        parent.setChild(child1);
        assert getTable().insert(parent) == 1 : "insertOneToOne did not insert parent";
        
        // verify that child was NOT inserted
        Table<SormulaTestChild1ReadOnlyCascade> child1Table = getDatabase().getTable(SormulaTestChild1ReadOnlyCascade.class);
        assert child1Table.select(child1.getId()) == null : "child " + child1.getId() + " was inserted using readonly cascade";
        commit();
    }
    
    
    @Test
    public void insertOneToManyList() throws SormulaException
    {
        begin();
        SormulaTestParentReadOnlyCascade parent = new SormulaTestParentReadOnlyCascade(950, "ROC Insert parent " + 950);
        
        for (int i = 1; i <= 10; ++i)
        {
            SormulaTestChildNReadOnlyCascade c = new SormulaTestChildNReadOnlyCascade(9950 + i, "ROC Child of parent " + 950);
            parent.add(c);
        }
        
        assert getTable().insert(parent) == 1 : "insertOneToManyList did not insert parent";
        
        // verify that all children were NOT inserted
        Table<SormulaTestChildNReadOnlyCascade> childTable = getDatabase().getTable(SormulaTestChildNReadOnlyCascade.class);
        ScalarSelectOperation<SormulaTestChildNReadOnlyCascade> operation = new ScalarSelectOperation<SormulaTestChildNReadOnlyCascade>(childTable);
        for (SormulaTestChildNReadOnlyCascade c: parent.getChildList())
        {
            operation.setParameters(c.getId());
            operation.execute();
            assert operation.readNext() == null : "child " + c.getId() + " was inserted using readonly cascade"; 
        }
        operation.close();
        commit();
    }
}
