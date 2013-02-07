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
import java.sql.Connection;
import java.sql.DriverManager;

import org.sormula.Database;
import org.sormula.Table;


/**
 * An example to show how to use sormula with nothing but a POJO {@link Inventory}
 * and the sormula.jar. There is no SQL, no configuration files, and no annotations. 
 * Annotations may be used to override default configuration. Sormula never needs 
 * a configuration file. 
 * <p>
 * No annotations are required for insert, update, delete, and select by primary key 
 * if the POJO (row class) conforms to the following:
 * <ul>
 * <li>Table name and class name are the same</li>
 * <li>Column names and class field names are the same</li>
 * <li>First field corresponds to primary column</li>
 * <li>All fields in class are columns in table</li>
 * </ul>
 * </p><p>
 * Everything needed for this example is in the one directory that contains 
 * this class. The database used by this example is exampleDB.script. exampleDB.script 
 * may be viewed with any text editor.
 * <p>
 * Compile with compile.bat<br> 
 * Run with run.bat
 * </p>
 *   
 * @author Jeff Miller
 */
public class ZeroConfigExample
{
    Connection connection;
    Database database;
    Table<Inventory> inventoryTable;
    
    
    public static void main(String[] args) throws Exception
    {
    	System.out.println("begin");
    	ZeroConfigExample example = new ZeroConfigExample();
    	example.deleteAll(); // start with empty database
    	example.insert(1234);
    	example.insert(5555);
    	example.insert(1111);
    	example.update(5555);
    	example.delete(1111);
    	example.close();
    	System.out.println("end");
    }
    
    
    public ZeroConfigExample() throws Exception
    {
        Class.forName("org.hsqldb.jdbc.JDBCDriver");
        connection = DriverManager.getConnection("jdbc:hsqldb:file:exampleDB;shutdown=true");
        database = new Database(connection);
        inventoryTable = database.getTable(Inventory.class);
    }
    
    
    public void close() throws Exception
    {
        connection.close();
        database.close();
    }
    
    
    public void insert(int partNumber) throws Exception
    {
        System.out.println("insert");
        Inventory inventory = new Inventory();
        inventory.setPartNumber(partNumber);
        inventory.setManufacturerId("Acme");
        inventory.setQuantity(99);
        inventoryTable.insert(inventory);
    }    
    
    
    public void update(int partNumber) throws Exception
    {
        System.out.println("update");
        Inventory inventory = inventoryTable.select(partNumber);
        inventory.setQuantity(1000);
        inventoryTable.update(inventory);
    }    
    
    
    public void delete(int partNumber) throws Exception
    {
        System.out.println("delete");
        Inventory inventory = inventoryTable.select(partNumber);
        inventoryTable.delete(inventory);
    }    
    
    
    public void deleteAll() throws Exception
    {
        System.out.println("clear database");
        inventoryTable.deleteAll();
    }
}
