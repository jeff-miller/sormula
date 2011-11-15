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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sormula.SormulaException;
import org.sormula.annotation.Wheres;
import org.sormula.log.ClassLogger;
import org.sormula.operation.ArrayListSelectOperation;
import org.sormula.operation.FullListSelect;
import org.sormula.operation.ListSelectOperation;
import org.sormula.operation.aggregate.SelectAggregateOperation;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests all select operations and {@linkplain Wheres} annotations.
 * 
 * @author Jeff Miller
 */
@Test(groups="operation.select", dependsOnGroups="operation.insert")
public class SelectTest extends DatabaseTest<SormulaTest4>
{
    private static final ClassLogger log = new ClassLogger();
    
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        createTable(SormulaTest4.class, null);
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
        SelectAggregateOperation<SormulaTest4, Integer> selectSum = 
            new SelectAggregateOperation<SormulaTest4, Integer>(getTable(), "SUM", "id");
        selectSum.setWhere("byType");
        selectSum.setParameters(type);
        selectSum.execute();
        
        // sums should be same
        assert sum == selectSum.readAggregate() : "select aggregate sum failed";
        selectSum.close();

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
        List<SormulaTest4> selectedList = new FullListSelect<SormulaTest4>(getTable(), "byType").executeAll(3);
        
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
        ListSelectOperation<SormulaTest4> operation = new ArrayListSelectOperation<SormulaTest4>(getTable(), "descriptionLike");
        operation.setParameters("%operation%");
        operation.execute();
        List<SormulaTest4> selectedList = operation.readAll();
        operation.close();
        
        assert expectedCount == selectedList.size() : "select by operation wrong number of rows";
        
        // all rows in selectedList should have operation in description
        for (SormulaTest4 r : selectedList)
        {
            assert r.getDescription().indexOf("operation") >= 0 : r.getId() + " row is incorrect for where condition";
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
        ArrayListSelectOperation<SormulaTest4> operation = new ArrayListSelectOperation<SormulaTest4>(getTable(), "")
        {
            @Override
            protected String getSql()
            {
                return getBaseSql() + " where type in(2,4,999)";
            }
        };
        
        operation.execute();
        List<SormulaTest4> selectedList = operation.readAll();
        operation.close();
        
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
    public void selectIn() throws SormulaException
    {
        begin();
        
        // perform with different test sizes but same operation to test that correctly prepare 
        ListSelectOperation<SormulaTest4> operation = new ArrayListSelectOperation<SormulaTest4>(getTable(), "idIn");
        selectIn(operation, 5);
        selectIn(operation, 7);
        operation.close();
        commit();
    }
    
    
    protected void selectIn(ListSelectOperation<SormulaTest4> operation, int testFactor) throws SormulaException
    {
        // choose id's divisible by testFactor for in clause
        Set<Integer> idSet = new HashSet<Integer>(getAll().size());
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
