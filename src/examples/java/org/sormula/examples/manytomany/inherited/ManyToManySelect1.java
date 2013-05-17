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
package org.sormula.examples.manytomany.inherited;

import java.sql.Connection;

import org.sormula.Database;
import org.sormula.SormulaException;
import org.sormula.examples.ExampleBase;


/**
 * Selects orders, order items, and products from both ends of the many to many relationship.
 * {@link #selectOrders()} selects starting with order through order items to a product.
 * {@link #selectOrdersForProducts()} selects starting with products through order 
 * items to an order. 
 */
public class ManyToManySelect1 extends ExampleBase
{
    Database database;
    
    
    public static void main(String[] args) throws Exception
    {
        new ManyToManyInsert1(); // create table and rows
        new ManyToManySelect1();
    }
    
    
    public ManyToManySelect1() throws Exception
    {
        // init
        openDatabase();
        Connection connection = getConnection();
        database = new Database(connection, getSchema());
        
        // examples
        selectOrders();
        selectOrdersForProducts();
        
        // clean up
        database.close();
        closeDatabase();
    }
    
    
    void selectOrders() throws SormulaException
    {
        System.out.println("\nOrders and their products obtained with Order class:");
        
        // for all orders
        for (Order o : database.getTable(Order.class).selectAll())
        {
            System.out.println("\nOrder " + o.getOrderId());
            
            // for all order items
            for (OrderItem oi : o.getOrderItems())
            {
                System.out.println("  " + oi.getItemNumber() + " " + oi.getProduct().getDescription());
            }
        }
    }
    
    
    void selectOrdersForProducts() throws SormulaException
    {
        System.out.println("\nProducts and their orders obtained with ProductOrderItems class:");
        
        // for all products
        for (ProductOrderItems p : database.getTable(ProductOrderItems.class).selectAll())
        {
            System.out.println("\nProduct " + p.getDescription() + " is in orders:");
            
            // for all order items
            for (OrderItemOrder oi : p.getOrderItems())
            {
                System.out.println("  " + oi.getOrderId() + " " + oi.getOrder().getFormattedOrderDate());
            }
        }
    }
}
