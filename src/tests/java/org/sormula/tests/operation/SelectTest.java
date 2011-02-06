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

import java.util.List;

import org.sormula.SormulaException;
import org.sormula.annotation.Wheres;
import org.sormula.operation.ArrayListSelectOperation;
import org.sormula.operation.ListSelectOperation;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests all select operations and {@linkplain Wheres} annotations.
 * 
 * @author Jeff Miller
 */
@Test(groups="operation.select", dependsOnGroups="operation.insert")
public class SelectTest extends OperationTest<SormulaTest4>
{
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
    	selectTestRows();
        assert all.size() == getTable().selectCount() : "select count failed";
        commit();
    }

    
    @Test
    public void selectByPrimaryKey() throws SormulaException
    {
    	begin();
    	selectTestRows();

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
    	selectTestRows();

    	// count type 3 rows
        int expectedCount = 0;
        for (SormulaTest4 r : all)
        {
            if (r.getType() == 3)
            {
                ++expectedCount;
            }
        }
        
        assert expectedCount > 0 : "no rows meet expected condition to test";
        
        // select all type 3 rows
        ListSelectOperation<SormulaTest4> operation = getTable().createSelectOperation("byType");
        operation.setParameters(3);
        operation.execute();
        List<SormulaTest4> selectedList = operation.readAll();
        operation.close();
        
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
    	selectTestRows();

        // count rows that contain "operation"
        int expectedCount = 0;
        for (SormulaTest4 r : all)
        {
            if (r.getDescription().indexOf("operation") >= 0)
            {
                ++expectedCount;
            }
        }
        
        assert expectedCount > 0 : "no rows meet expected condition to test";
        
        // select all rows with "operation" in description
        ListSelectOperation<SormulaTest4> operation = getTable().createSelectOperation("descriptionLike");
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
    	selectTestRows();

        // expected count 
        int expectedCount = 0;
        for (SormulaTest4 r : all)
        {
            if (r.getType() == 2 || r.getType() == 4 || r.getType() == 999)
            {
                ++expectedCount;
            }
        }
        
        assert expectedCount > 0 : "customSql no rows meet expected condition to test";

        // select with custom sql
        ArrayListSelectOperation<SormulaTest4> operation = new ArrayListSelectOperation<SormulaTest4>(getTable())
        {
            @Override
            protected String getSql()
            {
                return super.getBaseSql() + " where type in(2,4,999)";
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
}
