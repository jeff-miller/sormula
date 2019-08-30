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

import org.sormula.SormulaException;
import org.sormula.operation.InsertOperation;
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
    public void testInsertOperationBuilder() throws SormulaException
    {
        // tests builder methods in InsertOperationBuilder
        begin();
        try (InsertOperation<ModifyOperationBuilderTestRow> operation =
                InsertOperation.builder(getTable())
                .row(new ModifyOperationBuilderTestRow(999, 9, "insert test row"))
                .build())
        {
            operation.execute();
            ModifyOperationBuilderTestRow expectedRow = getTable().select(999);
            assert expectedRow != null : "insert failed";
        }
        commit();
    }
}
