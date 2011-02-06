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

import org.sormula.SormulaException;
import org.sormula.annotation.OrderBys;
import org.sormula.operation.ListSelectOperation;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests {@linkplain OrderBys} annoation.
 * 
 * @author Jeff Miller
 */
@Test(groups="operation.order", dependsOnGroups="operation.insert")
public class OrderByTest extends OperationTest<SormulaTest4>
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
    public void simpleOrder() throws SormulaException
    {
    	begin();
        ListSelectOperation<SormulaTest4> operation = getTable().createSelectAllOperation();
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
        
        operation.close();
        commit();
    }
    
    
    @Test
    public void complexOrder() throws SormulaException
    {
    	begin();
        ListSelectOperation<SormulaTest4> operation = getTable().createSelectAllOperation();
        operation.setOrderBy("ob2");
        operation.execute();
        
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
        
        operation.close();
        commit();
    }
}
