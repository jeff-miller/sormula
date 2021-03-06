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

import java.util.List;

import org.sormula.SormulaException;
import org.sormula.log.SormulaLogger;
import org.sormula.log.SormulaLoggerFactory;
import org.sormula.operation.ArrayListSelectOperation;
import org.sormula.operation.ListSelectOperation;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.Test;


/**
 * Tests some select operations using multiple columns in where condition.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups={"operation.select"}, dependsOnGroups="operation.insert")
public class SelectTestMC extends DatabaseTest<SormulaTest4MC>
{
    private static final SormulaLogger log = SormulaLoggerFactory.getClassLogger();
	
	
    @Override
    protected void open() throws Exception
    {
        super.open();
        createTable(SormulaTest4MC.class);
    }

    
    @Test
    public void selectByPrimaryKey() throws SormulaException
    {
    	begin();
    	selectTestRows(); // must perform each time since other tests are destructive
    	
        // choose random row
        SormulaTest4MC row = getRandom();
        
        // select by key
        SormulaTest4MC selected = getTable().select(row.getId(), row.getType());
        assert selected != null && row.getId() == selected.getId() : "select by primary key failed";
        
        commit();
    }
    
    
    @Test
    public void simpleSelect() throws SormulaException
    {
    	begin();
    	selectTestRows(); // must perform each time since other tests are destructive
    	
        // count expected rows
        int expectedCount = 0;
        for (SormulaTest4MC r : getAll())
        {
            if (r.getType() == 2 && r.getId() == 199)
            {
                ++expectedCount;
            }
        }
        
        if (expectedCount == 0)
    	{
        	log.error("no rows meet expected condition to test");
    	}
        
        // select all for condition w1
        List<SormulaTest4MC> selectedList;
        try (ListSelectOperation<SormulaTest4MC> operation = new ArrayListSelectOperation<>(getTable(), "w1"))
        {
            operation.setParameters(2, 199);
            operation.execute();
            selectedList = operation.readAll();
        }
        
        assert expectedCount == selectedList.size() : "simple select returned wrong number of rows";
        
        // all rows in selectedList should meet expected conditions
        for (SormulaTest4MC r : selectedList)
        {
            assert r.getType() == 2 && r.getId() == 199 : r.getId() + " row is incorrect for where condition";
        }
        
        commit();
    }
    
    
    @Test
    public void selectWithOperator() throws SormulaException
    {
    	begin();
    	selectTestRows(); // must perform each time since other tests are destructive
    	
        // count rows that contain expected condition
        int expectedCount = 0;
        for (SormulaTest4MC r : getAll())
        {
            if (r.getType() != 2 || r.getDescription().indexOf("operation") >= 0)
            {
                ++expectedCount;
            }
        }
        
        assert expectedCount > 0 : "no rows meet expected condition to test";
        
        // select all rows with condition
        List<SormulaTest4MC> selectedList;
        try (ListSelectOperation<SormulaTest4MC> operation = new ArrayListSelectOperation<>(getTable(), "w2"))
        {
            operation.setParameters(2);
            operation.execute();
            selectedList = operation.readAll();
        }
        
        assert expectedCount == selectedList.size() : "select with operator wrong number of rows";
        
        // all rows in selectedList should have expected condition
        for (SormulaTest4MC r : selectedList)
        {
            assert r.getType() != 2 || r.getDescription().indexOf("operation") >= 0 : 
                r.getId() + " row is incorrect for where condition";
        }
        
        commit();
    }
}
