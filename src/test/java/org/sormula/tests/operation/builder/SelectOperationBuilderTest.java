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

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sormula.SormulaException;
import org.sormula.operation.ArrayListSelectOperation;
import org.sormula.operation.HashMapSelectOperation;
import org.sormula.operation.LinkedHashMapSelectOperation;
import org.sormula.operation.ScalarSelectOperation;
import org.sormula.operation.builder.SelectOperationBuilder;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.Test;


/**
 * Tests for {@link SelectOperationBuilder} and subclasses.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="builder.select")
public class SelectOperationBuilderTest extends DatabaseTest<SelectOperationBuilderTestRow>
{
    @Override
    protected void open() throws Exception
    {
        super.open();
        
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
    
    
    @Test
    public void builderSqlOperation() throws SormulaException
    {
        // tests builder methods in SqlOperationBuilder
        begin();
        try (ArrayListSelectOperation<SelectOperationBuilderTestRow> operation =
                ArrayListSelectOperation.builder(getTable())
                .cached(true)
                .cascade(false) // true by default so test false
                .customSql("")
                .includeIdentityColumns(false) // true by default so test false
                .namedParameterMap(new HashMap<String, Object>())
                .parameter("test1", 1)
                .parameter("test2", "two")
                .readOnly(true)
                .requiredCascades("a", "b", "c")
                .timings(true)
                .timingId("z")
                .build())
        {
            assert operation.isCached() : "cached not set";
            assert !operation.isCascade() : "cascade not set";
            assert operation.getCustomSql() != null : "custom sql not set";
            assert !operation.isIncludeIdentityColumns() : "include identity columns not set";
            assert operation.getNamedParameterMap() != null : "named parameter map not set";
            
            Object test1Parameter = operation.getParameter("test1");
            assert test1Parameter != null && test1Parameter.equals(1) : "named parameter 1 not set";
            Object test2Parameter = operation.getParameter("test2");
            assert test2Parameter != null && test2Parameter.equals("two") : "named parameter 2 not set";
            
            assert operation.isReadOnly() : "readOnly not set";
            
            String[] requiredCascades = operation.getRequiredCascades();
            assert requiredCascades.length == 3 && 
                    requiredCascades[0].equals("a") &&
                    requiredCascades[1].equals("b") &&
                    requiredCascades[2].equals("c")
                    : "required cascades not set";
            
            assert operation.isTimings() : "timings not set";
            assert operation.getTimingId() != null && operation.getTimingId().equals("z") : "timingId not set";
            
            operation.setTimings(false); // turn off before select to avoid cluttering test output
            operation.selectAll();
        }
        commit();
    }
    
    
    @Test
    public void builderSelectOperation() throws SormulaException
    {
        // tests builder methods in SelectOperationBuilder
        begin();
        try (ArrayListSelectOperation<SelectOperationBuilderTestRow> operation =
                ArrayListSelectOperation.builder(getTable())
                .defaultReadAllSize(42)
                .fetchSize(99)
                .resultSetType(ResultSet.TYPE_SCROLL_INSENSITIVE)
                .build())
        {
            assert operation.getDefaultReadAllSize() == 42 : "invalid defaultReadAllSize";
            assert operation.getFetchSize() == 99 : "invalid fetchSize";
            assert operation.getResultSetType() == ResultSet.TYPE_SCROLL_INSENSITIVE : "invalid resultSetType";
            operation.selectAll();
        }
        commit();
    }
    
    
    @Test
    public void builderScalarSelectOperation() throws SormulaException
    {
        // tests builder methods in ScalarSelectOperationBuilder
        SelectOperationBuilderTestRow parametersFromRow = new SelectOperationBuilderTestRow();
        parametersFromRow.setType(1);
        begin();
        try (ScalarSelectOperation<SelectOperationBuilderTestRow> operation =
                ScalarSelectOperation.builderScalar(getTable())
                .where("forType")
                .rowParameters(parametersFromRow)
                .orderBy("idDescending")
                .maximumRowsRead(13)
                .build())
        {
            assert operation.getOrderByName().equals("idDescending") : "invalid order by";

            int rowCount = 0;
            int previousId = Integer.MAX_VALUE;
            for (SelectOperationBuilderTestRow row : operation)
            {
                ++rowCount;
                assert row.getType() == 1 : "row is not type 1";
                assert row.getId() < previousId : "rows are not in descending order by id";
                previousId = row.getId();
            }
            
            assert rowCount <= 13 : "too many rows read";
        }
        commit();
    }
    
    
    @Test
    public void builderArrayListSelectOperation() throws SormulaException
    {
        begin();
        try (ArrayListSelectOperation<SelectOperationBuilderTestRow> operation =
                ArrayListSelectOperation.builder(getTable())
                .where("forType")
                .parameters(5)
                .build())
        {
            operation.selectAll().forEach(row -> {assert row.getType() == 5 : "row is not type 5";});
        }
        commit();
    }
    
    
    @Test
    public void builderMapSelectOperation() throws SormulaException
    {
        // tests builder methods in MapSelectOperationBuilder
        begin();
        
        String keyMethodName = "getId";
        try (HashMapSelectOperation<Integer, SelectOperationBuilderTestRow> operation =
                HashMapSelectOperation.<Integer, SelectOperationBuilderTestRow>builder(getTable())
                .getKeyMethodName(keyMethodName)
                .build())
        {
            assert operation.getGetKeyMethodName() == keyMethodName : "invalid key method name";
            operation.selectAll();
        }
        
        try (HashMapSelectOperation<Integer, SelectOperationBuilderTestRow> operation =
                HashMapSelectOperation.<Integer, SelectOperationBuilderTestRow>builder(getTable())
                .keyFunction(r -> r.getId())
                .build())
        {
            assert operation.getKeyFunction() != null : "key function was not set";
        }
        commit();
    }
    
    
    @Test
    public void builderHashMapSelectOperation() throws SormulaException
    {
        // tests builder methods of HashMapSelectOperationBuilder
        begin();
        try (HashMapSelectOperation<Integer, SelectOperationBuilderTestRow> operation =
                HashMapSelectOperation.<Integer, SelectOperationBuilderTestRow>builder(getTable())
                .keyFunction(r -> r.getId())
                .build())
        {
            Map<Integer, SelectOperationBuilderTestRow> resultMap = operation.selectAll();
            List<SelectOperationBuilderTestRow> resultList = getTable().selectAll();
            
            assert resultMap.size() == resultList.size() : "map size is incorrect";
            resultList.forEach(r -> 
            {
                int listId = r.getId();
                SelectOperationBuilderTestRow mapTestRow = resultMap.get(listId);
                assert mapTestRow != null && mapTestRow.getId() == listId : "map is missing id=" + listId;
            });
        }
        commit();
    }
    
    
    @Test
    public void builderLinkedHashMapSelectOperation() throws SormulaException
    {
        // tests builder methods of LinkedHashMapSelectOperationBuilder
        begin();
        try (LinkedHashMapSelectOperation<Integer, SelectOperationBuilderTestRow> operation =
                LinkedHashMapSelectOperation.<Integer, SelectOperationBuilderTestRow>builder(getTable())
                .keyFunction(SelectOperationBuilderTestRow::getId)
                .orderBy("da")
                .build())
        {
            Map<Integer, SelectOperationBuilderTestRow> resultMap = operation.selectAll();
            
            String previousDescription = "";
            for (SelectOperationBuilderTestRow r: resultMap.values())
            {
                assert r.getDescription().compareTo(previousDescription) >= 0 : 
                    r.getId() + " row is not in ascending order by description";
                
                assert resultMap.get(r.getId()) != null : r.getId() + " is not in map";
            }
        }
        commit();
    }
}
