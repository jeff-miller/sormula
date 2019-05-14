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
import org.sormula.log.SormulaLogger;
import org.sormula.log.SormulaLoggerFactory;
import org.sormula.operation.ArrayListSelectOperation;
import org.sormula.operation.builder.SelectOperationBuilder;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests for {@link SelectOperationBuilder} classes.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="builder.select")
public class SelectOperationBuilderTest extends DatabaseTest<SelectOperationBuilderTestRow>
{
    SormulaLogger log = SormulaLoggerFactory.getClassLogger();
    
    
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        
        createTable(SelectOperationBuilderTestRow.class, 
            "CREATE TABLE " + getSchemaPrefix() + SelectOperationBuilderTestRow.class.getSimpleName() + " (" +
            " id INTEGER NOT NULL PRIMARY KEY," +
            " type SMALLINT," +
            " description VARCHAR(30)" +
            ")"
        );
        
        createTestRows();
    }
    
    
    private void createTestRows() throws SormulaException
    {
        List<SelectOperationBuilderTestRow> testRows = new ArrayList<>();
        
        for (int id = 1; id < 100; ++id)   testRows.add(new SelectOperationBuilderTestRow(id, 1, id + " type 1"));
        for (int id = 501; id < 550; ++id) testRows.add(new SelectOperationBuilderTestRow(id, 5, id + " type 5"));
        for (int id = 771; id < 779; ++id) testRows.add(new SelectOperationBuilderTestRow(id, 7, id + " type 7"));
        
        begin();
        getTable().insertAll(testRows);
        commit();
    }
    
    
    @AfterClass
    public void tearDown() throws Exception
    {
        closeDatabase();
    }
    
    
    @Test
    public void testArrayListSelectionBuilder() throws SormulaException
    {
        begin();
        try (ArrayListSelectOperation<SelectOperationBuilderTestRow> operation =
                ArrayListSelectOperation.builder(getTable(), "forType")
                .parameters(5)
                .build())
        {
            operation.selectAll().forEach(row -> {assert row.getType() == 5 : "row is not type 5";});
        }
        commit();
    }
}
