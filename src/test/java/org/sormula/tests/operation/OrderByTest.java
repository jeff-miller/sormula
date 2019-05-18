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

import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.annotation.OrderBy;
import org.sormula.annotation.OrderByField;
import org.sormula.operation.ArrayListSelectOperation;
import org.sormula.operation.ListSelectOperation;
import org.sormula.operation.OperationException;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.Test;


/**
 * Tests {@link OrderBy} annotation.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="operation.order", dependsOnGroups="operation.insert")
public class OrderByTest extends DatabaseTest<SormulaTest4>
{
    @Override
    protected void open() throws Exception
    {
        super.open();
        createTable(SormulaTest4.class);
    }

    
    @Test
    public void simpleOrder() throws SormulaException
    {
    	begin();
        
    	try (ListSelectOperation<SormulaTest4> operation = new ArrayListSelectOperation<>(getTable(), ""))
    	{
            operation.setOrderBy("ob1");
            operation.execute();
            
            // test if results in proper order
            SormulaTest4 previousRow = null;
            for (SormulaTest4 row: operation.readAll())
            {
                if (previousRow != null)
                {
                    assert row.getType() >= previousRow.getType() : "simple order rows are not in ascending order";
                }
                
                previousRow = row;
            }
    	}
        
        commit();
    }
    
    
    @Test
    public void complexOrder() throws SormulaException
    {
    	begin();
    	
    	try (ComplexOrderQuery operation = new ComplexOrderQuery(getTable()))
    	{
            // test if results in proper order
            SormulaTest4 previousRow = null;
            for (SormulaTest4 row: operation.readAll())
            {
                if (previousRow != null)
                {
                    if  (row.getType() == previousRow.getType())
                    {
                        // types are equal, test next order by column, id
                        assert row.getId() >= previousRow.getId() : "complex order id is not in ascending order";
                    }
                    else
                    {
                        assert row.getType() <= previousRow.getType(): "complex order type is not in descending order";
                    }
                }
                
                previousRow = row;
            }
    	}
        
        commit();
    }
}


/**
 * {@link @OrderBy} annotation may be used on the operations instead of on the row class.
 */
@OrderBy(name="ob2", orderByFields={
        @OrderByField(name="type", descending=true),
        @OrderByField(name="id")})
class ComplexOrderQuery extends ArrayListSelectOperation<SormulaTest4>
{
    public ComplexOrderQuery(Table<SormulaTest4> table) throws OperationException
    {
        super(table, ""/*no where*/);
        setOrderBy("ob2");
        execute();
    }
}
