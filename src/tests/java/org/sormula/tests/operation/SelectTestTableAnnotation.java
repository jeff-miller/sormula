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
import java.util.Map;

import org.sormula.Database;
import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.annotation.OrderBy;
import org.sormula.annotation.OrderBys;
import org.sormula.annotation.Row;
import org.sormula.annotation.Where;
import org.sormula.annotation.Wheres;
import org.sormula.operation.ArrayListSelectOperation;
import org.sormula.operation.LinkedHashMapSelectOperation;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests all select operations with {@linkplain Wheres} annotations on table class.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="operation.select", dependsOnGroups="operation.insert")
public class SelectTestTableAnnotation extends DatabaseTest<SormulaTest4>
{
    CustomTable customTable;
    
    
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        
        // use CustomTable for all SormulaTest4 operations
        customTable = new CustomTable(getDatabase(), SormulaTest4.class);
        getDatabase().addTable(customTable);
    }
    
    
    
    @Override
    public CustomTable getTable()
    {
        return customTable;
    }



    @AfterClass
    public void tearDown() throws Exception
    {
        closeDatabase();
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
        List<SormulaTest4> selectedList = new ArrayListSelectOperation<SormulaTest4>(
                getTable(), "byCustomTableType").selectAll(3);

        assert expectedCount == selectedList.size() : "simple select returned wrong number of rows";
        
        // all rows in selectedList should have type == 3
        for (SormulaTest4 r : selectedList)
        {
            assert r.getType() == 3 : r.getId() + " row is incorrect for where condition";
        }
        
        commit();
    }
    
    
    @Test
    public void selectLinkedHashMap() throws SormulaException
    {
        begin();
        
        LinkedHashMapSelectOperation<Integer, SormulaTest4> operation = 
            new LinkedHashMapSelectOperation<Integer, SormulaTest4>(getTable(), "" /*select all*/);
        operation.setGetKeyMethodName("getId");
        
        // select into map
        operation.setOrderBy("ob2CustomTable"); // by description
        operation.execute();
        Map<Integer, SormulaTest4> result = operation.readAll();
        operation.close();
        
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
}

@Row(tableName="SormulaTest4")
@Where(name="byCustomTableType", fieldNames="type")
@OrderBys({
    @OrderBy(name="ob2CustomTable", ascending="description")
})
class CustomTable extends Table<SormulaTest4>
{
    public CustomTable(Database database, Class<SormulaTest4> rowClass) throws SormulaException
    {
        super(database, rowClass);
    }
}