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
package org.sormula.tests.operation;

import java.util.ArrayList;

import org.sormula.SormulaException;
import org.sormula.operation.InsertOperation;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests all insert operations. This test must run first so that
 * test data is inserted for select, update, and delete tests.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="operation.insert")
public class InsertTest extends DatabaseTest<SormulaTest4>
{
    boolean preMethod;
    boolean postMethod;

    
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        createTable(SormulaTest4.class, 
            "CREATE TABLE " + getSchemaPrefix() + SormulaTest4.class.getSimpleName() + " (" +
            " id INTEGER NOT NULL PRIMARY KEY," +
            " type SMALLINT," +
            " description VARCHAR(30)" +
            ")"
        );
    }
    
    
    @AfterClass
    public void tearDown() throws Exception
    {
        closeDatabase();
    }
    
    
    @Test
    public void insertOne() throws SormulaException
    {
        assert getTable().insert(new SormulaTest4(13, 1, "Insert one")) == 1 : "insert one failed";
    }
    
    
    @Test
    public void insertCollection() throws SormulaException
    {
        ArrayList<SormulaTest4> list = new ArrayList<SormulaTest4>();
        
        for (int i = 101; i < 200; ++i)
        {
            list.add(new SormulaTest4(i, 2, "Insert collection " + i));
        }
        
        assert getTable().insertAll(list) == list.size() : "insert collection failed";
    }
    
    
    @Test
    public void insertByOperation() throws SormulaException
    {
        InsertOperation<SormulaTest4> operation = new InsertOperation<SormulaTest4>(getTable());
        
        // reverse order so that rows are natuarlly in order for order by tests
        for (int i = 1010; i > 1000; --i)
        {
            operation.setRow(new SormulaTest4(i, 3, "Insert operation " + i));
            operation.execute();
            assert operation.getRowsAffected() == 1 : "insert by operation failed";
        }
        
        operation.close();
    }
    
    
    @Test
    public void insertNotifyMethods() throws SormulaException
    {
        final SormulaTest4 testRow = new SormulaTest4(7, 4, "Insert 7");
        
        InsertOperation<SormulaTest4> operation = new InsertOperation<SormulaTest4>(getTable())
        {
            @Override
            protected void preExecute(SormulaTest4 row)
            {
                preMethod = testRow.getId() == row.getId();
            }
            
            @Override
            protected void postExecute(SormulaTest4 row)
            {
                postMethod = testRow.getId() == row.getId();
            }
        };
        
        operation.setRow(testRow);
        operation.execute();
        operation.close();
        
        assert preMethod && postMethod : "notify methods failed";
    }
}
