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
package org.sormula.examples.filter;

import java.sql.Connection;

import org.sormula.Database;
import org.sormula.SormulaException;
import org.sormula.examples.ExampleBase;
import org.sormula.examples.manytomany.inherited.ManyToManyInsert1;
import org.sormula.operation.ArrayListSelectOperation;
import org.sormula.operation.filter.SelectCascadeFilter;


/**
 * Selects orders, order items, and products using a {@link SelectCascadeFilter}.
 */
public class FilterSelect extends ExampleBase
{
    Database database;
    
    // TODO add lambda filter example
    
    public static void main(String[] args) throws Exception
    {
        new ManyToManyInsert1(); // create table and rows (same tables and data as many to many example)
        new FilterSelect();
    }
    
    
    public FilterSelect() throws Exception
    {
        // init
        openDatabase();
        Connection connection = getConnection();
        database = new Database(connection, getSchema());
        
        // examples
        selectOrders("A");
        selectOrders("D");
        
        // clean up
        database.close();
        closeDatabase();
    }
    
    
    void selectOrders(String productId) throws SormulaException
    {
        System.out.println("\nOrders that contain product " + productId + ":");
        @SuppressWarnings("resource") // selectAll method invokes close
        ArrayListSelectOperation<Order> selectOrders = new ArrayListSelectOperation<>(database.getTable(Order.class), "");
        selectOrders.setSelectCascadeFilters(new ProductFilter(productId));
        
        // for all orders
        for (Order o : selectOrders.selectAll())
        {
            System.out.println("\nOrder " + o.getOrderId());
            
            // for all order items
            for (OrderItem oi : o.getOrderItems())
            {
                System.out.println("  " + oi.getItemNumber() + " " + oi.getProduct().getDescription());
            }
        }
    }
}
