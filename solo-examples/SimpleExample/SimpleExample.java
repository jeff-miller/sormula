/* sormula - Simple object relational mapping
 * Copyright (C) 2011-2015 Jeff Miller
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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.sormula.Database;
import org.sormula.Table;


/**
 * A simple example to show basic sormula features. Everything needed for this example is
 * in the directory that contains this class. The database used by this example is
 * db.script. db.script may be viewed with any text editor.
 * <p>
 * Compile with compile.bat<br> 
 * Run with run.bat
 * </p>
 *  
 * @author Jeff Miller
 */
public class SimpleExample
{
    Connection connection;
    
    
    public static void main(String[] args) throws Exception
    {
        System.out.println("begin");
        SimpleExample example = new SimpleExample();
        example.createTable();
        example.removeInventory(1234, 1);
        example.removeInventoryDAO(999, 9);
        example.selectByRange(1, 10);
        example.selectByRange2(1, 10);
        example.clearInventory("xyz");
        example.selectIn();
        example.close();
        System.out.println("end");
    }
    
    
    public SimpleExample() throws Exception
    {
        connection = DriverManager.getConnection("jdbc:hsqldb:file:db;shutdown=true");
    }
    
    
    public void close() throws Exception
    {
        connection.close();
    }
    
    
    public void createTable() throws Exception
    {
    	try (Statement statement = connection.createStatement())
    	{
	        System.out.println("create table");
	        statement.execute("CREATE TABLE INVENTORY(PARTNUMBER INTEGER, QUANTITY INTEGER, MANFID VARCHAR(40))");
    	}
    	catch (SQLException e)
    	{
    		// assume exception because table already exists
    		System.out.println(e);
    	}

    	// insert test rows
    	ArrayList<Inventory> testRows = new ArrayList<>();
    	testRows.add(newRow(1233, 105, "abcd"));
    	testRows.add(newRow( 777,   7, "abcd"));
    	testRows.add(newRow( 555,   5, "abcd"));
    	testRows.add(newRow(1234,  64, "abcd"));
    	testRows.add(newRow( 999,  99, "xyz"));
    	testRows.add(newRow( 998,  88, "xyx"));
    	
        try (Database database = new Database(connection))
        {
	        Table<Inventory> inventoryTable = database.getTable(Inventory.class);
	        inventoryTable.deleteAll(); // start with empty table
	        inventoryTable.insertAll(testRows);
        }
    }
    private Inventory newRow(int partNumber, int quantity, String manufacturerId)
    {
    	Inventory inventory = new Inventory();
    	inventory.setPartNumber(partNumber);
    	inventory.setQuantity(quantity);
    	inventory.setManufacturerId(manufacturerId);
    	return inventory;
    }
    
    
    /**
     * Reduces inventory quantity for a part.
     * 
     * @param partNumber id of part to affect 
     * @param delta reduce inventory quantity by this amount
     */
    public void removeInventory(int partNumber, int delta) throws Exception
    {
        System.out.println("removeInventory");
    	
        try (Database database = new Database(connection))
        {
	        Table<Inventory> inventoryTable = database.getTable(Inventory.class);
	        
	        // get part by primary key
	        Inventory inventory = inventoryTable.select(partNumber);
	        
	        // update
	        inventory.setQuantity(inventory.getQuantity() - delta);
	        inventoryTable.update(inventory);
        }
    }    
    
    
    /**
     * Reduces inventory quantity for a part.
     * 
     * @param partNumber id of part to affect 
     * @param delta reduce inventory quantity by this amount
     */
    public void removeInventoryDAO(int partNumber, int delta) throws Exception
    {
        System.out.println("removeInventoryDAO");
        
        try (Database database = new Database(connection))
        {
	        InventoryDAO inventoryDAO = new InventoryDAO(database);
	        
	        // get part by primary key
	        Inventory inventory = inventoryDAO.select(partNumber);
	        
	        // update
	        inventory.setQuantity(inventory.getQuantity() - delta);
	        inventoryDAO.update(inventory);
        }
    }    
    
    
    /**
     * Clears inventory for a manufacturer.
     *  
     * @param manufacturerId affect all rows with this manufacturer id
     */
    public void clearInventory(String manufacturerId) throws Exception
    {
        System.out.println("clearInventory");
    	
        try (Database database = new Database(connection))
        {
	        Table<Inventory> inventoryTable = database.getTable(Inventory.class);
	        
	        // for all inventory of manufacturer 
	        // "manf" is name of where annotation in Inventory.java
	        List<Inventory> list = inventoryTable.selectAllWhere("manf", manufacturerId);
	        for (Inventory inventory: list)
	        {
	            inventory.setQuantity(0);
	        }
	        
	        // update
	        inventoryTable.updateAll(list);
        }
    }    
    
    
    /**
     * Select all inventory with quantity in a range. Part and quantity are logged for
     * each row selected.
     * 
     * @param minimumQuantity select rows with quantity of at least this amount
     * @param maximumQuantity select rows with quantity no more than this amount
     * @see QuantityRangeSelect
     */
    public void selectByRange(int minimumQuantity, int maximumQuantity) throws Exception
    {
        System.out.println("selectByRange " + minimumQuantity + " " + maximumQuantity);
    	
        try (Database database = new Database(connection))
        {
	        Table<Inventory> inventoryTable = database.getTable(Inventory.class);
	        
	        // execute
	        List<Inventory> results = inventoryTable.selectAllCustom(
	        		"where quantity between ? and ?", minimumQuantity, maximumQuantity);
	        
	        // show results
	        for (Inventory inventory: results)
	        {
	            System.out.println(inventory.getPartNumber() + " quantity=" + inventory.getQuantity());
	        }
        }
    }
    
    
    /**
     * Select all inventory with quantity in a range. Part and quantity are logged for
     * each row selected.
     * 
     * @param minimumQuantity select rows with quantity of at least this amount
     * @param maximumQuantity select rows with quantity no more than this amount
     * @see QuantityRangeSelect
     */
    public void selectByRange2(int minimumQuantity, int maximumQuantity) throws Exception
    {
        System.out.println("selectByRange2 " + minimumQuantity + " " + maximumQuantity);
    	
        try (Database database = new Database(connection))
        {
	        Table<Inventory> inventoryTable = database.getTable(Inventory.class);
	        
	        // select operation for range
	        @SuppressWarnings("resource") // selectAll method closes
			QuantityRangeSelect operation = new QuantityRangeSelect(inventoryTable);
	        operation.setRange(minimumQuantity, maximumQuantity);
	        
	        // show results
	        for (Inventory inventory: operation.selectAll())
	        {
	            System.out.println(inventory.getPartNumber() + " quantity=" + inventory.getQuantity());
	        }
        }
    }
    
    
    /**
     * Select inventory where part numbers are in a list.
     */
    public void selectIn() throws Exception
    {
        ArrayList<Integer> partNumbers = new ArrayList<>();
        partNumbers.add(999);
        partNumbers.add(777);
        partNumbers.add(1234);
        System.out.println("selectIn partNumbers=" + partNumbers);
        
        try (Database database = new Database(connection))
        {
	        Table<Inventory> inventoryTable = database.getTable(Inventory.class);
	        
	        // select operation for list of part numbers
	        // SELECT PARTNUMBER, QUANTITY, MANFID FROM INVENTORY WHERE PARTNUMBER IN (?, ?, ?)
	        for (Inventory inventory: inventoryTable.selectAllWhere("partNumberIn", partNumbers))
	        {
	            System.out.println(inventory.getPartNumber());
	        }
        }
    }
}
