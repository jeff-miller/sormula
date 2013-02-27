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
import java.util.List;

import org.sormula.Database;
import org.sormula.operation.DeleteOperation;
import org.sormula.operation.OperationException;
import org.sormula.operation.SqlOperation;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests read-only attributes of {@link Database} and {@link SqlOperation}.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="operation.readonly")
public class ReadOnlyTest extends DatabaseTest<SormulaTest4RO>
{
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        createTable(SormulaTest4RO.class, 
            "CREATE TABLE " + getSchemaPrefix() + SormulaTest4RO.class.getSimpleName() + " (" +
            " id INTEGER NOT NULL PRIMARY KEY," +
            " type SMALLINT," +
            " description VARCHAR(30)" +
            ")"
        );
        
        List<SormulaTest4RO> testRows = new ArrayList<>();
        for (int i = 1; i <= 20; ++i)
        {
            testRows.add(new SormulaTest4RO(i, 1, "ReadOnlyTest " + i));
        }
        begin();
        getTable().insertAll(testRows);
        commit();
        getDatabase().setReadOnly(true);
    }
    
    
    @AfterClass
    public void tearDown() throws Exception
    {
        closeDatabase();
    }


    @Test
    public void insertReadOnly() throws Exception
    {
        begin();
        
        try
        {
            getTable().insert(new SormulaTest4RO(666, 1, "Insert read-only"));
            throw new Exception("insert using read-only operation");
        }
        catch (OperationException e)
        {
            // insert should fail with a message about read-only
            if (!e.getMessage().contains("read-only")) throw e;
        }
        
        commit();
    }


    @Test
    public void updateReadOnly() throws Exception
    {
        begin();
        
        try
        {
            getTable().updateAll(getTable().selectAll());
            throw new Exception("update using read-only operation");
        }
        catch (OperationException e)
        {
            // update should fail with a message about read-only
            if (!e.getMessage().contains("read-only")) throw e;
        }
        
        commit();
    }

    
    @Test
    public void deleteReadOnly() throws Exception
    {
        begin();
        
        // test readonly at operation level
        getDatabase().setReadOnly(false); // operation gets false when created
        DeleteOperation<SormulaTest4RO> delete = new DeleteOperation<>(getTable());
        assert !delete.isReadOnly() : "incorrect read-only state";
        
        delete.setReadOnly(true);
        try
        {
            delete.delete(getTable().selectAll().get(0));
            throw new Exception("delete using read-only operation");
        }
        catch (OperationException e)
        {
            // delete should fail with a message about read-only
            if (!e.getMessage().contains("read-only")) throw e;
        }
        finally
        {
            // put back to readonly in case more tests are to run
            getDatabase().setReadOnly(true); 
        }
        
        commit();
    }
}