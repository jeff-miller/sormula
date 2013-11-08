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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.annotation.Where;
import org.sormula.annotation.WhereField;
import org.sormula.annotation.Wheres;
import org.sormula.log.ClassLogger;
import org.sormula.operation.ArrayListSelectOperation;
import org.sormula.operation.LinkedHashMapSelectOperation;
import org.sormula.operation.ListSelectOperation;
import org.sormula.operation.OperationException;
import org.sormula.operation.SelectIterator;
import org.sormula.operation.aggregate.SelectAggregateOperation;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests all select operations and {@link Wheres} annotations.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="operation.select", dependsOnGroups="operation.insert")
public class SelectTest extends DatabaseTest<SormulaTest4>
{
    private static final ClassLogger log = new ClassLogger();
    
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        createTable(SormulaTest4.class);
    }
    
    
    @AfterClass
    public void tearDown() throws Exception
    {
        closeDatabase();
    }

    
    @Test
    public void selectCount() throws SormulaException
    {
    	begin();
    	selectTestRows(); // must perform each time since other tests are destructive
        assert getAll().size() == getTable().selectCount() : "select count failed";
        commit();
    }

    
    @Test
    public void selectInConstant() throws SormulaException
    {
        begin();
        Table<SormulaTest4> table = getTable();
        
        // insert test rows
        assert table.insert(new SormulaTest4(6001, 0, "6001")) == 1 : "test row was not inserted";
        assert table.insert(new SormulaTest4(6002, 0, "6002")) == 1 : "test row was not inserted";
        
        // test IN with constant operand
        assert new ArrayListSelectOperation<>(table, "idIn2").selectAll().size() == 2 :
            "IN (6001, 6002) operator did not work";
        
        commit();
    }

    
    @Test
    public void selectLimit() throws SormulaException
    {
        begin();
        int maxRows = getTable().selectCount() / 2;
        assert maxRows > 0 : "no rows to test";
        ArrayListSelectOperation<SormulaTest4> s = new ArrayListSelectOperation<>(getTable(), "");
        s.setMaximumRowsRead(maxRows);
        assert maxRows == s.selectAll().size() : "setMaximumRowsRead failed";
        commit();
    }

    
    @Test
    public void selectMaximumRows() throws SormulaException
    {
        begin();
        List<SormulaTest4> selected = getTable().selectAllWhere("maximumRowsTest", 2);
        assert selected.size() <= 10 : "maximumRows annotation failed returned " + selected.size();
        commit();
    }

    
    @Test
    public void selectAggregate() throws SormulaException
    {
        begin();
        selectTestRows(); // must perform each time since other tests are destructive
        
        // sum with Java
        final int type = 3;
        int sum = 0;
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        int count = 0;
        for (SormulaTest4 s: getAll())
        {
            // limit to one type to test where condition
            if (s.getType() == type)
            {
                ++count;
                sum += s.getId();
                min = Math.min(min, s.getId());
                max = Math.max(max, s.getId());
            }
        }
        
        // sum with SQL using explict operation
        try (SelectAggregateOperation<SormulaTest4, Integer> selectSum = 
                new SelectAggregateOperation<>(getTable(), "SUM", "id"))
        {
            selectSum.setWhere("byType");
            selectSum.setParameters(type);
            selectSum.execute();
            
            // sums should be same
            assert sum == selectSum.readAggregate() : "select aggregate sum failed";
        }

        // test with table methods
        assert min == getTable().<Integer>selectMin("id", "byType", type) : "select aggregate min failed";
        assert max == getTable().<Integer>selectMax("id", "byType", type) : "select aggregate max failed";
        
        if (log.isDebugEnabled())
        {
            log.debug("J avg="+((float)sum/count));
            log.debug("T avg="+getTable().<Integer>selectAvg("id", "byType", type));
        }
        assert (Integer)sum/count == getTable().<Integer>selectAvg("id", "byType", type) : "select aggregate avg failed";
        
        commit();
    }

    
    @Test
    public void selectByPrimaryKey() throws SormulaException
    {
    	begin();
    	selectTestRows(); // must perform each time since other tests are destructive

    	// choose random row
        SormulaTest4 row = getRandom();
        
        // select by primary key
        SormulaTest4 selected = getTable().select(row.getId());
        
        assert selected != null && row.getId() == selected.getId() : "select by primary key failed";
        
        commit();
    }
    
    
    @Test
    public void simpleSelect() throws SormulaException
    {
    	begin();
    	selectTestRows(); // must perform each time since other tests are destructive

    	// count type 3 rows
        int expectedCount = 0;
        for (SormulaTest4 r : getAll())
        {
            if (r.getType() == 3)
            {
                ++expectedCount;
            }
        }
        
        assert expectedCount > 0 : "no rows meet expected condition to test";
        
        // select all type 3 rows
        List<SormulaTest4> selectedList = new ArrayListSelectOperation<>(getTable(), "byType").selectAll(3);

        assert expectedCount == selectedList.size() : "simple select returned wrong number of rows";
        
        // all rows in selectedList should have type == 3
        for (SormulaTest4 r : selectedList)
        {
            assert r.getType() == 3 : r.getId() + " row is incorrect for where condition";
        }
        
        commit();
    }
    
    
    @Test
    public void selectByOperation() throws SormulaException
    {
    	begin();
    	selectTestRows(); // must perform each time since other tests are destructive

        // count rows that contain "operation"
        int expectedCount = 0;
        for (SormulaTest4 r : getAll())
        {
            if (r.getDescription().indexOf("operation") >= 0)
            {
                ++expectedCount;
            }
        }
        
        assert expectedCount > 0 : "no rows meet expected condition to test";
        
        // select all rows with "operation" in description
        List<SormulaTest4> selectedList;
        try (DescriptionSelect operation = new DescriptionSelect(getTable()))
        {
            selectedList = operation.readAll();
        }
        
        assert expectedCount == selectedList.size() : "select by operation wrong number of rows";
        
        // all rows in selectedList should have operation in description
        for (SormulaTest4 r : selectedList)
        {
            assert r.getDescription().indexOf("operation") >= 0 : r.getId() + " row is incorrect for where condition";
        }
        
        commit();
    }
    
    
    @Test
    public void iteratorTest() throws SormulaException
    {
        begin();
        
        // get all type 3 rows in id order
        List<SormulaTest4> type3List = getTable().selectAllWhereOrdered("byType", "obId", 3);

        try (ArrayListSelectOperation<SormulaTest4> itop = new ArrayListSelectOperation<>(getTable(), "byType"))
        {
            // set up iterator to iterate type 3 rows
            itop.setParameters(3);
            itop.setOrderBy("obId");
            // optional itop.execute(); 

            // iterate through type 3, compare to known list
            Iterator<SormulaTest4> type3Iterator = type3List.iterator();
            
            for (SormulaTest4 r : itop)
            {
                assert r != null : "SelectIterator returned null";
                
                SormulaTest4 test = type3Iterator.next();
                assert test != null : "SelectIterator has more rows than reference list";
                
                assert r.getId() == test.getId() : "iterator error " + r.getId() + 
                        " out of order with " + test.getId();
            }
            
            assert !type3Iterator.hasNext() : "reference list has more rows than SelectIterator";
        }
        
        commit();
    }
    
    
    @Test
    public void iteratorTest2() throws SormulaException
    {
        begin();
        
        // get all type 3 rows in id order
        List<SormulaTest4> type3List = getTable().selectAllWhereOrdered("byType", "obId", 3);

        try (ArrayListSelectOperation<SormulaTest4> itop = new ArrayListSelectOperation<>(getTable(), "byType"))
        {
            // set up iterator to iterate type 3 rows
            itop.setParameters(3);
            itop.setOrderBy("obId");
            // optional itop.execute(); 
        
            // iterate through type 3, compare to known list
            Iterator<SormulaTest4> type3Iterator = type3List.iterator();
            SelectIterator<SormulaTest4> selectIterator = new SelectIterator<>(itop);
            
            while (selectIterator.hasNext()) // test with explicit iterator
            {
                SormulaTest4 r = selectIterator.next();
                assert r != null : "SelectIterator returned null";
                
                SormulaTest4 test = type3Iterator.next();
                assert test != null : "SelectIterator has more rows than reference list";
                
                assert r.getId() == test.getId() : "iterator error " + r.getId() + 
                        " out of order with " + test.getId();
            }
            
            assert !type3Iterator.hasNext() : "reference list has more rows than SelectIterator";
        }
        
        commit();
    }
    
    
    @Test
    public void customSql() throws SormulaException
    {
    	begin();
    	selectTestRows(); // must perform each time since other tests are destructive

        // expected count 
        int expectedCount = 0;
        for (SormulaTest4 r : getAll())
        {
            if (r.getType() == 2 || r.getType() == 4 || r.getType() == 999)
            {
                ++expectedCount;
            }
        }
        
        assert expectedCount > 0 : "customSql no rows meet expected condition to test";

        // select with custom sql
        List<SormulaTest4> selectedList;
        try (ArrayListSelectOperation<SormulaTest4> operation = new ArrayListSelectOperation<SormulaTest4>(getTable(), "")
            {
                @Override
                protected String getSql()
                {
                    return getBaseSql() + " where type in(2,4,999)";
                }
            })
        {        
            operation.execute();
            selectedList = operation.readAll();
        }
        
        // confirm
        assert expectedCount == selectedList.size() : "customSql operation wrong number of rows";
        for (SormulaTest4 r : selectedList)
        {
            assert r.getType() == 2 || r.getType() == 4 || r.getType() == 999 : 
                r.getId() + " row is incorrect for where condition";
        }
        
        commit();
    }
    
    
    @Test
    public void selectLinkedHashMap() throws SormulaException
    {
        begin();
        Map<Integer, SormulaTest4> result;
        
        try (LinkedHashMapSelectOperation<Integer, SormulaTest4> operation = 
            new LinkedHashMapSelectOperation<>(getTable(), "" /*select all*/))
        {
            operation.setGetKeyMethodName("getId");
            
            // select into map
            operation.setOrderBy("ob2"); // by description
            operation.execute();
            result = operation.readAll();
        }
        
        assert result.size() > 0 : "no rows selected";
        
        String previousDescription = "";
        for (SormulaTest4 r: result.values())
        {
            assert r.getDescription().compareTo(previousDescription) >= 0 : 
                r.getId() + " row is not in ascending order by description";
            
            assert result.get(r.getId()) != null : r.getId() + " is not in map";
        }
        
        commit();
    }
    
    
    @Test
    public void selectIn() throws SormulaException
    {
        begin();
        selectTestRows();
        
        // perform with different test sizes but same operation to test that correctly prepare 
        try (ListSelectOperation<SormulaTest4> operation = new ArrayListSelectOperation<>(getTable(), "idIn"))
        {
            selectIn(operation, 5);
            selectIn(operation, 7);
        }
        
        commit();
    }
    
    
    protected void selectIn(ListSelectOperation<SormulaTest4> operation, int testFactor) throws SormulaException
    {
        // choose id's divisible by testFactor for in clause
        Set<Integer> idSet = new HashSet<>(getAll().size());
        for (SormulaTest4 r : getAll())
        {
            if (r.getId() % testFactor == 0)
            {
                idSet.add(r.getId());
            }
        }

        assert idSet.size() > 0 : "no rows meet expected condition to test";
        
        // select all rows where id in idSet
        if (log.isDebugEnabled()) log.debug("select in " + idSet);
        operation.setParameters(idSet);
        operation.execute();
        List<SormulaTest4> selectedList = operation.readAll();
        
        assert idSet.size() == selectedList.size() : "select 'in' operator results in wrong number of rows";
        
        // all rows in selectedList should have id in idSet
        for (SormulaTest4 r : selectedList)
        {
            assert idSet.contains(r.getId()) : r.getId() + " row is incorrect for where condition";
        }
    }
}


/**
 * {@link Where} and {@link Wheres} annotations may be used on the operations instead of on 
 * the row class.
 */
@Where(name="desclike", whereFields=@WhereField(name="description", comparisonOperator="like", operand="'%operation%'"))
class DescriptionSelect extends ArrayListSelectOperation<SormulaTest4>
{
    public DescriptionSelect(Table<SormulaTest4> table) throws OperationException
    {
        super(table, "desclike");
        execute();
    }
}