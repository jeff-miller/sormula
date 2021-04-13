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
package org.sormula.tests.operation.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sormula.SormulaException;
import org.sormula.operation.ArrayListSelectOperation;
import org.sormula.operation.DeleteOperation;
import org.sormula.operation.HashMapSelectOperation;
import org.sormula.operation.InsertOperation;
import org.sormula.operation.SaveOperation;
import org.sormula.operation.UpdateOperation;
import org.sormula.operation.builder.ModifyOperationBuilder;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.Test;


/**
 * Tests for {@link ModifyOperationBuilder} and subclasses.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="builder.select")
public class ModifyOperationBuilderTest extends DatabaseTest<ModifyOperationBuilderTestRow>
{
    @Override
    protected void open() throws Exception
    {
        super.open();
        
        createTable(ModifyOperationBuilderTestRow.class, 
            "CREATE TABLE " + getSchemaPrefix() + ModifyOperationBuilderTestRow.class.getSimpleName() + " (" +
            " id INTEGER NOT NULL PRIMARY KEY," +
            " type SMALLINT," +
            " description VARCHAR(30)" +
            ")"
        );
        
        createTestRows();
    }
    
    
    private void createTestRows() throws SormulaException
    {
        List<ModifyOperationBuilderTestRow> testRows = new ArrayList<>();
        
        for (int id = 1; id < 100; ++id)   testRows.add(new ModifyOperationBuilderTestRow(id, 1, id + " delete test row"));
        for (int id = 501; id < 550; ++id) testRows.add(new ModifyOperationBuilderTestRow(id, 5, id + " save test row"));
        for (int id = 771; id < 779; ++id) testRows.add(new ModifyOperationBuilderTestRow(id, 7, id + " update test row"));
        
        begin();
        getTable().insertAll(testRows);
        commit();
    }
    
    
    @Test
    public void DeleteOperationBuilder() throws SormulaException
    {
        // tests builder methods in DeletetOperationBuilder
        begin();
        
        try (ArrayListSelectOperation<ModifyOperationBuilderTestRow> selectDeletedOperation =
                new ArrayListSelectOperation<>(getTable(), "forType"))
        {
            selectDeletedOperation.setParameters(1);
            List<ModifyOperationBuilderTestRow> rowsToDelete = selectDeletedOperation.selectAll();
            assert rowsToDelete.size() > 0 : "no rows to delete";
            
            try (DeleteOperation<ModifyOperationBuilderTestRow> testOperation =
                    DeleteOperation.builder(getTable())
                    .rows(rowsToDelete) // test rows(Collection) method
                    .build())
            {
                testOperation.execute();
                assert selectDeletedOperation.selectAll().size() == 0 : "delete failed";
            }
        }        
        
        commit();
    }
    
    
    @Test
    public void InsertOperationBuilder() throws SormulaException
    {
        // tests builder methods in InsertOperationBuilder
        begin();
        try (InsertOperation<ModifyOperationBuilderTestRow> testOperation =
                InsertOperation.builder(getTable())
                .row(new ModifyOperationBuilderTestRow(999, 9, "insert test row")) // test row(Row) method
                .build())
        {
            testOperation.execute();
            ModifyOperationBuilderTestRow expectedRow = getTable().select(999);
            assert expectedRow != null : "insert failed";
        }
        commit();
    }
    
    
    @Test
    public void UpdateOperationBuilder() throws SormulaException
    {
        // tests builder methods in UpdateOperationBuilder
        begin();
        
        try (ArrayListSelectOperation<ModifyOperationBuilderTestRow> selectUpdatedOperation =
                new ArrayListSelectOperation<>(getTable(), "forType"))
        {
            selectUpdatedOperation.setParameters(7);
            ModifyOperationBuilderTestRow[] rowsToUpdate = selectUpdatedOperation.selectAll()
                    .toArray(new ModifyOperationBuilderTestRow[0]);
            assert rowsToUpdate.length > 0 : "no rows to update";
            for (ModifyOperationBuilderTestRow row : rowsToUpdate)
            {
                row.setDescription("updated");
            }
            
            try (UpdateOperation<ModifyOperationBuilderTestRow> testOperation =
                    UpdateOperation.builder(getTable())
                    .rows(rowsToUpdate) // test rows(Row[]) method
                    .build())
            {
                testOperation.execute();
                for (ModifyOperationBuilderTestRow row : selectUpdatedOperation.selectAll())
                {
                    assert row.getDescription().equals("updated") : "update failed";
                }
            }
        }        
        
        commit();
    }
    
    
    @Test
    public void SaveOperationBuilder() throws SormulaException
    {
        // tests builder methods in SaveOperationBuilder
        begin();
        
        try (HashMapSelectOperation<Integer, ModifyOperationBuilderTestRow> selectSavedOperation =
                new HashMapSelectOperation<>(getTable(), "forType"))
        {
            selectSavedOperation.setParameters(5);
            Map<Integer, ModifyOperationBuilderTestRow> rowsToSave = selectSavedOperation.selectAll();
            assert rowsToSave.size() > 0 : "no rows to save";
            rowsToSave.put(555, new ModifyOperationBuilderTestRow(555, 5, "new row for save"));
            for (ModifyOperationBuilderTestRow row : rowsToSave.values())
            {
                row.setDescription("saved");
            }

            try (SaveOperation<ModifyOperationBuilderTestRow> testOperation =
                    SaveOperation.builder(getTable())
                    .rows(rowsToSave) // test rows(Map) method
                    .build())
            {
                testOperation.execute();
                for (ModifyOperationBuilderTestRow row : selectSavedOperation.selectAll().values())
                {
                    assert row.getDescription().equals("saved") : "save failed";
                }
            }
        }        
        
        commit();
    }
    
    
    @Test
    public void otherSaveOperationBuilder() throws SormulaException
    {
        // tests builder methods in ModifyOperationBuilder
        begin();
        try (SaveOperation<ModifyOperationBuilderTestRow> testOperation =
                SaveOperation.builder(getTable())
                .batch(true)
                .parameters("test")
                .cached(true)
                .cascade(false) // true by default so test false
                .queryTimeout(9999)  // test SqlOperationBuilder super class
                .build())
        {
            assert testOperation.isBatch() : "batch not set";
            
            Object[] parameters = testOperation.getParameters();
            assert parameters != null && parameters[0].equals("test") : "parameters not set";
            
            assert testOperation.isCached() : "cached not set";
            assert !testOperation.isCascade() : "cascade not set";
            assert testOperation.getQueryTimeout() == 9999 : "query timeout not set";
            testOperation.execute();
        }
        commit();
    }
}
