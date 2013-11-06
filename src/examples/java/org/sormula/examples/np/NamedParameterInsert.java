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
import java.util.GregorianCalendar;

import org.sormula.Database;
import org.sormula.Table;
import org.sormula.examples.ExampleBase;


/**
 * Creates tables and data for named parameter example.
 */
public class NamedParameterInsert extends ExampleBase
{
    Database database;
    
    
    public static void main(String[] args) throws Exception
    {
        new NamedParameterInsert();
    }
    
    
    public NamedParameterInsert() throws Exception
    {
        openDatabase();
        
        // create tables
        String tableName = getSchemaPrefix() + "nporder";
        dropTable(tableName);
        createTable("CREATE TABLE " + tableName + 
                "(orderid INTEGER PRIMARY KEY," +
                " orderdate DATE)" 
        );
        
        tableName = getSchemaPrefix() + "nporderitem";
        dropTable(tableName);
        createTable("CREATE TABLE " + tableName + 
                "(orderid INTEGER NOT NULL," +
                " itemnumber INTEGER NOT NULL," +
                " productid VARCHAR(10) NOT NULL," +
                " PRIMARY KEY (orderid, itemnumber))"
        );
        
        tableName = getSchemaPrefix() + "npproduct";
        dropTable(tableName);
        createTable("CREATE TABLE " + tableName + 
                "(productid VARCHAR(10) PRIMARY KEY," +
                " description VARCHAR(30))" 
        );
        
        // init
        Connection connection = getConnection();
        database = new Database(connection, getSchema());
        
        // examples
        insertProducts();
        insertOrders();
        
        // clean up
        database.close();
        closeDatabase();
    }
    
    
    public void insertProducts() throws Exception
    {
        Table<Product> productTable = database.getTable(Product.class);
        
        for (String productId : new String[] {"A", "B", "C", "D", "E"})
        {
            productTable.insert(new Product(productId, "Product " + productId));
        }
    }
    
    
    public void insertOrders() throws Exception
    {
        Order order;
        Table<Order> orderTable = database.getTable(Order.class);
        
        order = new Order(1001);
        order.setOrderDate(new GregorianCalendar(2011, 0, 1).getTime());
        order.add(new OrderItem(1, "A"));
        order.add(new OrderItem(2, "B"));
        orderTable.insert(order);
        
        order = new Order(1002);
        order.setOrderDate(new GregorianCalendar(2013, 1, 2).getTime());
        order.add(new OrderItem(1, "A"));
        order.add(new OrderItem(2, "C"));
        order.add(new OrderItem(3, "D"));
        orderTable.insert(order);
        
        order = new Order(1003);
        order.setOrderDate(new GregorianCalendar(2013, 2, 3).getTime());
        order.add(new OrderItem(1, "D"));
        orderTable.insert(order);
    }
}
