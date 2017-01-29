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
import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.operation.DeleteOperation;
import org.sormula.operation.ReadOnlyException;
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
        
        // insert test rows
        List<SormulaTest4RO> testRows = new ArrayList<>();
        for (int i = 1; i <= 20; ++i)
        {
            testRows.add(new SormulaTest4RO(i, 1, "ReadOnlyTest " + i));
        }
        begin();
        Table<SormulaTest4RO> table = freshTableInstance();
        table.setReadOnly(false); // explicitly set since table is annotated with @Row(readOnly=true)
        table.insertAll(testRows);
        commit();
    }
    
    
    @AfterClass
    public void tearDown() throws Exception
    {
        closeDatabase();
    }
    
    
    Table<SormulaTest4RO> freshTableInstance() throws SormulaException
    {
        // don't use getTable() to avoid affecting other tests
        return new Table<SormulaTest4RO>(getDatabase(), SormulaTest4RO.class); 
    }


    @Test
    public void insertReadOnly1() throws Exception
    {
        begin();
        
        try
        {
            // test database is not read-only, table is
            getDatabase().setReadOnly(false);
            Table<SormulaTest4RO> table = freshTableInstance();
            table.setReadOnly(true);
            table.insert(new SormulaTest4RO(666, 1, "Insert read-only"));
            throw new Exception("insert using read-only operation");
        }
        catch (ReadOnlyException e)
        {
            // insert should fail 
        }
        
        commit();
    }


    @Test
    public void insertReadOnly2() throws Exception
    {
        begin();
        
        try
        {
            // test database is not read-only, table is
            getDatabase().setReadOnly(false);
            Table<SormulaTest4RO> table = freshTableInstance(); // use read-only value from table annotation
            table.insert(new SormulaTest4RO(666, 1, "Insert read-only"));
            throw new Exception("insert using read-only operation");
        }
        catch (ReadOnlyException e)
        {
            // insert should fail 
        }
        
        commit();
    }


    @Test
    public void updateReadOnly() throws Exception
    {
        begin();
        
        try
        {
            // test database is read-only, table is not
            getDatabase().setReadOnly(true);
            Table<SormulaTest4RO> table = freshTableInstance();
            table.setReadOnly(false);
            table.updateAll(table.selectAll());
            throw new Exception("update using read-only operation");
        }
        catch (ReadOnlyException e)
        {
            // update should fail 
        }
        
        commit();
    }

    
    @Test
    public void deleteReadOnly() throws Exception
    {
        begin();
        
        // test read-only at operation level
        getDatabase().setReadOnly(false); // causes operation to be read-write when created
        Table<SormulaTest4RO> table = freshTableInstance();
        table.setReadOnly(false); // causes operation to be read-write when created

        try (DeleteOperation<SormulaTest4RO> delete = new DeleteOperation<>(table))
        {
            assert !delete.isReadOnly() : "operation should be read-only";
            
            delete.setReadOnly(true);
            try
            {
                delete.delete(table.selectAll().get(0));
                throw new Exception("delete using read-only operation");
            }
            catch (ReadOnlyException e)
            {
                // delete should fail
            }
        }
        
        commit();
    }
}