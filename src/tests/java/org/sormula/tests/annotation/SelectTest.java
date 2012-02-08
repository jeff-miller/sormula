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
package org.sormula.tests.annotation;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sormula.Database;
import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.log.ClassLogger;
import org.sormula.operation.ArrayListSelectOperation;
import org.sormula.operation.LinkedHashMapSelectOperation;
import org.sormula.operation.ListSelectOperation;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests selects when annotations are on table class not row or operation class.
 * No update or delete tests are performed in this package since they would not
 * add anything to annotation testing. Update and delete tests are performed in
 * other packages.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="annotation.select", dependsOnGroups="annotation.insert")
public class SelectTest extends DatabaseTest<SormulaTestA>
{
    private static final ClassLogger log = new ClassLogger();
    TestDB db;
    
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        
        // use database with annotations, getDatabase() override
        Database standardTestDb = super.getDatabase();
        db = new TestDB(standardTestDb.getConnection(), standardTestDb.getSchema());

        createTable(SormulaTestA.class, null);
    }
    
    
    @AfterClass
    public void tearDown() throws Exception
    {
        closeDatabase();
    }
    
    
    @Override
    public Database getDatabase()
    {
        return db;
    }

    
    @Test
    public void selectInConstant() throws SormulaException
    {
        begin();
        Table<SormulaTestA> table = getTable();
        
        // insert test rows
        assert table.insert(new SormulaTestA(6001, 0, "6001")) == 1 : "test row was not inserted";
        assert table.insert(new SormulaTestA(6002, 0, "6002")) == 1 : "test row was not inserted";
        
        // test IN with constant operand
        assert new ArrayListSelectOperation<>(table, "idIn2").selectAll().size() == 2 :
            "IN (6001, 6002) operator did not work";
        
        commit();
    }
    
    
    @Test
    public void simpleSelect() throws SormulaException
    {
    	begin();
    	selectTestRows(); // must perform each time since other tests are destructive

    	// count type 3 rows
        int expectedCount = 0;
        for (SormulaTestA r : getAll())
        {
            if (r.getType() == 3)
            {
                ++expectedCount;
            }
        }
        
        assert expectedCount > 0 : "no rows meet expected condition to test";
        
        // select all type 3 rows
        List<SormulaTestA> selectedList = new ArrayListSelectOperation<>(getTable(), "byType").selectAll(3);

        assert expectedCount == selectedList.size() : "simple select returned wrong number of rows";
        
        // all rows in selectedList should have type == 3
        for (SormulaTestA r : selectedList)
        {
            assert r.getType() == 3 : r.getId() + " row is incorrect for where condition";
        }
        
        commit();
    }
    
    
    @Test
    public void selectByOperation() throws SormulaException
    {
        begin();

        // execute to make sure ob1 found, no need to test ordering
        try (ArrayListSelectOperation<SormulaTestA> operation = new ArrayListSelectOperation<>(getTable(), ""))
        {
            operation.setOrderBy("ob1");
            operation.selectAll();
        }
        
        commit();
    }
    
    
    
    @Test
    public void selectLinkedHashMap() throws SormulaException
    {
        begin();
        Map<Integer, SormulaTestA> result;
        
        try (LinkedHashMapSelectOperation<Integer, SormulaTestA> operation = 
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
        for (SormulaTestA r: result.values())
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
        try (ListSelectOperation<SormulaTestA> operation = new ArrayListSelectOperation<>(getTable(), "idIn"))
        {
            selectIn(operation, 3);
            selectIn(operation, 4);
        }

        commit();
    }
    
    
    protected void selectIn(ListSelectOperation<SormulaTestA> operation, int testFactor) throws SormulaException
    {
        // choose id's divisible by testFactor for in clause
        Set<Integer> idSet = new HashSet<>(getAll().size());
        for (SormulaTestA r : getAll())
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
        List<SormulaTestA> selectedList = operation.readAll();
        
        assert idSet.size() == selectedList.size() : "select 'in' operator results in wrong number of rows";
        
        // all rows in selectedList should have id in idSet
        for (SormulaTestA r : selectedList)
        {
            assert idSet.contains(r.getId()) : r.getId() + " row is incorrect for where condition";
        }
    }
}
