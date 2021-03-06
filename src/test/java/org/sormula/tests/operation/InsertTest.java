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
package org.sormula.tests.operation;

import java.util.ArrayList;

import org.sormula.SormulaException;
import org.sormula.operation.InsertOperation;
import org.sormula.tests.DatabaseTest;
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

    
    @Override
    protected void open() throws Exception
    {
        super.open();
        createTable(SormulaTest4.class, 
            "CREATE TABLE " + getSchemaPrefix() + SormulaTest4.class.getSimpleName() + " (" +
            " id INTEGER NOT NULL PRIMARY KEY," +
            " type SMALLINT," +
            " description VARCHAR(30)" +
            ")"
        );
    }
    
    
    @Test
    public void insertOne() throws SormulaException
    {
        begin();
        assert getTable().insert(new SormulaTest4(13, 1, "Insert one")) == 1 : "insert one failed";
        commit();
    }
    
    
    @Test
    public void insertCollection() throws SormulaException
    {
        ArrayList<SormulaTest4> list = new ArrayList<>();
        
        for (int i = 101; i < 200; ++i)
        {
            list.add(new SormulaTest4(i, 2, "Insert collection " + i));
        }
        
        begin();
        assert getTable().insertAll(list) == list.size() : "insert collection failed";
        commit();
    }
    
    
    @Test
    public void insertBatch() throws SormulaException
    {
        ArrayList<SormulaTest4> list = new ArrayList<>();
        
        int type = 7000;
        for (int i = 1; i < 200; ++i)
        {
            list.add(new SormulaTest4(type + i, type, "Insert batch " + i));
        }
        
        begin();
        getTable().insertAllBatch(list);
        
        // success if selected is same as list size
        // NOTE: oracle returns 0 for rows affected instead of list size
        assert getTable().selectAllWhere("byType", type).size() == list.size() : "insert batch failed";
        
        commit();
    }
    
    
    @Test
    public void insertBatchByOperation() throws SormulaException
    {
        ArrayList<SormulaTest4> list = new ArrayList<>();
        
        int type = 8000;
        for (int i = 1; i < 200; ++i)
        {
            list.add(new SormulaTest4(type + i, type, "Insert batch " + i));
        }
        
        begin();
        try (InsertOperation<SormulaTest4> operation = new InsertOperation<>(getTable()))
        {
            operation.setBatch(true);
            operation.insertAll(list);
        }
        
        // success if selected is same as list size
        // NOTE: oracle returns 0 for rows affected instead of list size
        assert getTable().selectAllWhere("byType", type).size() == list.size() : "insert batch failed";
        
        commit();
    }
    
    
    @Test
    public void insertByOperation() throws SormulaException
    {
        begin();
        try (InsertOperation<SormulaTest4> operation = new InsertOperation<>(getTable()))
        {
            // reverse order so that rows are naturally in order for order by tests
            for (int i = 1010; i > 1000; --i)
            {
                operation.setRow(new SormulaTest4(i, 3, "Insert operation " + i));
                operation.execute();
                assert operation.getRowsAffected() == 1 : "insert by operation failed";
            }
        }
        
        commit();
    }
    
    
    @Test
    public void insertNotifyMethods() throws SormulaException
    {
        if (!getTable().isCached())
        {
            // this test only works if table is not cached
            // cached table may use its own insert operation
            begin();
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
            commit();
            
            assert preMethod && postMethod : "notify methods failed";
        }
    }
}
