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
package org.sormula.examples.np;

import java.sql.Connection;
import java.util.Date;
import java.util.GregorianCalendar;

import org.sormula.Database;
import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.examples.ExampleBase;
import org.sormula.operation.ArrayListSelectOperation;


/**
 * Selects orders, order items, and products using named parameters.
 */
public class NamedParameterSelect extends ExampleBase
{
    Database database;
    
    
    public static void main(String[] args) throws Exception
    {
        new NamedParameterInsert(); // create table and rows
        new NamedParameterSelect();
    }
    
    
    public NamedParameterSelect() throws Exception
    {
        // init
        openDatabase();
        Connection connection = getConnection();
        database = new Database(connection, getSchema());
        
        // examples
        selectOrders();
        selectOrders(new GregorianCalendar(2013, 0, 1).getTime());
        
        // clean up
        database.close();
        closeDatabase();
    }
    
    
    void selectOrders() throws SormulaException
    {
        System.out.println("\nOrders and their products obtained with named parameter $description:");
        
        Table<Order> ordertTable = database.getTable(Order.class);
        try (ArrayListSelectOperation<Order> selectOperation = new ArrayListSelectOperation<>(ordertTable, "" /* all */))
        {
            // selects only products with description of "Product D"
            // $description in OrderItem @OneToOneCascade(...sourceParameterFieldNames={"productId", "$description"
            selectOperation.setParameter("description", "Product D");  
            
            // for all orders
            for (Order o : selectOperation.selectAll())
            {
                System.out.println("\nOrder " + o.getOrderId());
                
                // for all order items
                for (OrderItem oi : o.getOrderItems())
                {
                    System.out.print("  " + oi.getItemNumber() + " ");
                    if (oi.getProduct() == null) System.out.println("?");
                    else System.out.println(oi.getProduct().getDescription());
                }
            }
        }
    }
    
    
    void selectOrders(Date minimumAge) throws SormulaException
    {
        System.out.println("\nOrders as old as " + minimumAge + ":");
        
        Table<Order> ordertTable = database.getTable(Order.class);
        try (ArrayListSelectOperation<Order> selectOperation = new ArrayListSelectOperation<>(ordertTable, "asOldAs"))
        {
            selectOperation.setParameter("orderDate", minimumAge); // operand for @WhereField(name="orderDate", ...
            
            for (Order o : selectOperation.selectAll())
            {
                System.out.println("Order " + o.getOrderId() + " " + o.getOrderDate());
            }
        }
    }
}
