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
package org.sormula.tests.operation.aggregate.builder;

import java.util.ArrayList;
import java.util.List;

import org.sormula.SormulaException;
import org.sormula.operation.aggregate.SelectAvgOperation;
import org.sormula.operation.aggregate.SelectCountOperation;
import org.sormula.operation.aggregate.SelectMaxOperation;
import org.sormula.operation.aggregate.SelectMinOperation;
import org.sormula.operation.aggregate.SelectSumOperation;
import org.sormula.operation.aggregate.builder.SelectAggregateOperationBuilder;
import org.sormula.operation.aggregate.builder.SelectAvgOperationBuilder;
import org.sormula.operation.aggregate.builder.SelectCountOperationBuilder;
import org.sormula.operation.aggregate.builder.SelectMaxOperationBuilder;
import org.sormula.operation.aggregate.builder.SelectMinOperationBuilder;
import org.sormula.operation.aggregate.builder.SelectSumOperationBuilder;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.Test;


/**
 * Tests for {@link SelectAggregateOperationBuilder} and subclasses.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="builder.select")
public class SelectAggregateOperationBuilderTest extends DatabaseTest<SelectAggregateOperationBuilderTestRow>
{
    @Override
    protected void open() throws Exception
    {
        super.open();
        
        createTable(SelectAggregateOperationBuilderTestRow.class, 
            "CREATE TABLE " + getSchemaPrefix() + "SelectAggOperationBuilderRow (" +
            " id INTEGER NOT NULL PRIMARY KEY," +
            " type SMALLINT," +
            " description VARCHAR(30)" +
            ")"
        );
        
        createTestRows();
    }
    
    
    private void createTestRows() throws SormulaException
    {
        List<SelectAggregateOperationBuilderTestRow> testRows = new ArrayList<>();
        
        for (int id = 1; id <= 100; ++id)   testRows.add(new SelectAggregateOperationBuilderTestRow(id, 1, id + " type 1"));
        for (int id = 501; id <= 550; ++id) testRows.add(new SelectAggregateOperationBuilderTestRow(id, 5, id + " type 5"));
        for (int id = 771; id <= 779; ++id) testRows.add(new SelectAggregateOperationBuilderTestRow(id, 7, id + " type 7"));
        
        begin();
        getTable().insertAll(testRows);
        commit();
    }
    
    
    @Test
    public void buildertestSelectAvgOperation() throws SormulaException
    {
        // tests builder methods in SelectAvgOperationBuilder
        begin();
        
        SelectAvgOperationBuilder<SelectAggregateOperationBuilderTestRow, Integer> builder = 
                SelectAvgOperation.builder(getTable(), "id");
        
        try (SelectAvgOperation<SelectAggregateOperationBuilderTestRow, Integer> testOperation = builder
                .where("forType")
                .parameters(5)
                .build();
             SelectAvgOperation<SelectAggregateOperationBuilderTestRow, Integer> expectedOperation =
                 new SelectAvgOperation<>(getTable(), "id"))
        {
            testOperation.execute();
            Integer testResult = testOperation.readAggregate();
            
            expectedOperation.setWhere("forType");
            expectedOperation.setParameters(5);
            expectedOperation.execute();
            Integer expectedResult = expectedOperation.readAggregate(); 

            assert Integer.compare(testResult, expectedResult) == 0 : "average failure";
        }
        commit();
    }
    
    
    @Test
    public void builderSelectCountOperation() throws SormulaException
    {
        // tests builder methods in SelectCountOperationBuilder
        begin();
        
        SelectCountOperationBuilder<SelectAggregateOperationBuilderTestRow, Integer> builder = 
                SelectCountOperation.builder(getTable(), "id");
        
        try (SelectCountOperation<SelectAggregateOperationBuilderTestRow, Integer> testOperation = builder
                .where("forType")
                .parameters(5)
                .build();
             SelectCountOperation<SelectAggregateOperationBuilderTestRow, Integer> expectedOperation =
                 new SelectCountOperation<>(getTable(), "id"))
        {
            testOperation.execute();
            Integer testResult = testOperation.readAggregate();
            
            expectedOperation.setWhere("forType");
            expectedOperation.setParameters(5);
            expectedOperation.execute();
            Integer expectedResult = expectedOperation.readAggregate(); 

            assert Integer.compare(testResult, expectedResult) == 0 : "count failure";
        }
        commit();
    }
    
    
    @Test
    public void builderSelectMaxOperation() throws SormulaException
    {
        // tests builder methods in SelectMaxOperationBuilder
        begin();
        
        SelectMaxOperationBuilder<SelectAggregateOperationBuilderTestRow, Integer> builder = 
                SelectMaxOperation.builder(getTable(), "id");
        
        try (SelectMaxOperation<SelectAggregateOperationBuilderTestRow, Integer> testOperation = builder
                .where("forType")
                .parameters(7)
                .build();
             SelectMaxOperation<SelectAggregateOperationBuilderTestRow, Integer> expectedOperation =
                 new SelectMaxOperation<>(getTable(), "id"))
        {
            testOperation.execute();
            Integer testResult = testOperation.readAggregate();
            
            expectedOperation.setWhere("forType");
            expectedOperation.setParameters(7);
            expectedOperation.execute();
            Integer expectedResult = expectedOperation.readAggregate(); 

            assert Integer.compare(testResult, expectedResult) == 0 : "max failure";
        }
        commit();
    }
    
    
    @Test
    public void builderSelectMinOperation() throws SormulaException
    {
        // tests builder methods in SelectMinOperationBuilder
        begin();
        
        SelectMinOperationBuilder<SelectAggregateOperationBuilderTestRow, Integer> builder = 
                SelectMinOperation.builder(getTable(), "id");
        
        try (SelectMinOperation<SelectAggregateOperationBuilderTestRow, Integer> testOperation = builder
                .where("forType")
                .parameters(7)
                .build();
             SelectMinOperation<SelectAggregateOperationBuilderTestRow, Integer> expectedOperation =
                 new SelectMinOperation<>(getTable(), "id"))
        {
            testOperation.execute();
            Integer testResult = testOperation.readAggregate();
            
            expectedOperation.setWhere("forType");
            expectedOperation.setParameters(7);
            expectedOperation.execute();
            Integer expectedResult = expectedOperation.readAggregate(); 

            assert Integer.compare(testResult, expectedResult) == 0 : "min failure";
        }
        commit();
    }
    
    
    @Test
    public void builderSelectSumOperation() throws SormulaException
    {
        // tests builder methods in SelectSumOperationBuilder
        begin();
        
        SelectSumOperationBuilder<SelectAggregateOperationBuilderTestRow, Integer> builder = 
                SelectSumOperation.builder(getTable(), "id");
        
        try (SelectSumOperation<SelectAggregateOperationBuilderTestRow, Integer> testOperation = builder
                .where("forType")
                .parameters(7)
                .build();
             SelectSumOperation<SelectAggregateOperationBuilderTestRow, Integer> expectedOperation =
                 new SelectSumOperation<>(getTable(), "id"))
        {
            testOperation.execute();
            Integer testResult = testOperation.readAggregate();
            
            expectedOperation.setWhere("forType");
            expectedOperation.setParameters(7);
            expectedOperation.execute();
            Integer expectedResult = expectedOperation.readAggregate(); 

            assert Integer.compare(testResult, expectedResult) == 0 : "sum failure";
        }
        commit();
    }
}
