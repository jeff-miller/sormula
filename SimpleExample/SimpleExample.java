import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;

import org.sormula.Database;
import org.sormula.Table;
import org.sormula.operation.ArrayListSelectOperation;
import org.sormula.operation.FullListSelect;
import org.sormula.operation.ListSelectOperation;


/**
 * A simple example to show basic sormula features. Everything needed for this example is
 * in the one directory that contains this class. The database used by this example is
 * simpleDB.script. simpleDB.script may be viewed with any text editor.
 * 
 * Compile with compile.bat 
 * Run with run.bat
 *  
 * @author Jeff Miller
 */
public class SimpleExample
{
    Connection connection;
    
    
    public static void main(String[] args) throws Exception
    {
        System.out.println("begin");
        SimpleExample simpleExample = new SimpleExample();
        simpleExample.removeInventory(1234, 1);
        simpleExample.clearInventory("xyz");
        simpleExample.clearInventory2("xyz");
        simpleExample.selectByRange(1, 10);
        simpleExample.selectByRange2(1, 10);
        simpleExample.selectIn();
        simpleExample.close();
        System.out.println("end");
    }
    
    
    public SimpleExample() throws Exception
    {
        Class.forName("org.hsqldb.jdbc.JDBCDriver");
        connection = DriverManager.getConnection("jdbc:hsqldb:file:simpleDB;shutdown=true");
    }
    
    
    public Connection getConnection()
    {
        return connection;
    }


    public void close() throws Exception
    {
        connection.close();
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
    	
        // set up
        Database database = new Database(getConnection());
        Table<Inventory> inventoryTable = database.getTable(Inventory.class);
        
        // get part by primary key
        Inventory inventory = inventoryTable.select(partNumber);
        
        // update
        inventory.setQuantity(inventory.getQuantity() - delta);
        inventoryTable.update(inventory);
    }    
    
    
    /**
     * Clears inventory for a manufacturer.
     *  
     * @param manufacturerId affect all rows with this manufacturer id
     */
    public void clearInventory(String manufacturerId) throws Exception
    {
        System.out.println("clearInventory");
    	
        // set up
        Database database = new Database(getConnection());
        Table<Inventory> inventoryTable = database.getTable(Inventory.class);
        List<Inventory> clearList = new ArrayList<Inventory>();
        
        // select for a specific manufacturer ("manf" is name of where annotation in Inventory.java)
        ListSelectOperation<Inventory> operation = new ArrayListSelectOperation<Inventory>(
                inventoryTable, "manf");
        operation.setParameters(manufacturerId);
        operation.execute();
        
        // for all inventory of manufacturer
        for (Inventory inventory: operation.readAll())
        {
            // remember for update
            inventory.setQuantity(0);
            clearList.add(inventory);
        }
        
        // update
        inventoryTable.updateAll(clearList);
        
        // clean up
        operation.close();
    }    
    
    
    /**
     * Clears inventory for a manufacturer. Uses {@link FullListSelect} operation 
     * for less Java.
     *  
     * @param manufacturerId affect all rows with this manufacturer id
     */
    public void clearInventory2(String manufacturerId) throws Exception
    {
        System.out.println("clearInventory2");
    	
        // set up
        Database database = new Database(getConnection());
        Table<Inventory> inventoryTable = database.getTable(Inventory.class);
        List<Inventory> clearList = new ArrayList<Inventory>();
        
        // select operation for a specific manufacturer ("manf" is name of where annotation in Inventory.java)
        FullListSelect<Inventory> fs = new FullListSelect<Inventory>(inventoryTable, "manf");
        		
        // for all inventory of manufacturer
        for (Inventory inventory: fs.executeAll(manufacturerId))
        {
            // remember for update
            inventory.setQuantity(0);
            clearList.add(inventory);
        }
        
        // update
        inventoryTable.updateAll(clearList);
    }
    
    
    /**
     * Select all inventory with quantity in a range. Part and quantity are logged for
     * each row selected.
     * 
     * @param minimumQuanity select rows with quantity of at least this amount
     * @param maximumQuantity select rows with quantity no more than this amount
     * @see QuantityRangeSelect
     */
    public void selectByRange(int minimumQuanity, int maximumQuantity) throws Exception
    {
        System.out.println("selectByRange " + minimumQuanity + " " + maximumQuantity);
    	
        // set up
        Database database = new Database(getConnection());
        Table<Inventory> inventoryTable = database.getTable(Inventory.class);
        
        // execute
        List<Inventory> results = inventoryTable.selectAllCustom(
        		"where quantity between ? and ?", minimumQuanity, maximumQuantity);
        
        // show results
        for (Inventory inventory: results)
        {
            System.out.println(inventory.getPartNumber() + " quantity=" + inventory.getQuantity());
        }
    }
    
    
    /**
     * Select all inventory with quantity in a range. Part and quantity are logged for
     * each row selected.
     * 
     * @param minimumQuanity select rows with quantity of at least this amount
     * @param maximumQuantity select rows with quantity no more than this amount
     * @see QuantityRangeSelect
     */
    public void selectByRange2(int minimumQuanity, int maximumQuantity) throws Exception
    {
        System.out.println("selectByRange2 " + minimumQuanity + " " + maximumQuantity);
    	
        // set up
        Database database = new Database(getConnection());
        Table<Inventory> inventoryTable = database.getTable(Inventory.class);
        
        // select operation for range
        QuantityRangeSelect operation = new QuantityRangeSelect(inventoryTable);
        operation.setRange(minimumQuanity, maximumQuantity);
        operation.execute();
        
        // show results
        for (Inventory inventory: operation.readAll())
        {
            System.out.println(inventory.getPartNumber() + " quantity=" + inventory.getQuantity());
        }
        
        // clean up
        operation.close();
    }
    
    
    /**
     * Select inventory where part numbers are in a list.
     */
    public void selectIn() throws Exception
    {
        ArrayList<Integer> partNumbers = new ArrayList<Integer>();
        partNumbers.add(999);
        partNumbers.add(777);
        partNumbers.add(1234);
        System.out.println("selectIn partNumbers=" + partNumbers);
        
        // set up
        Database database = new Database(getConnection());
        Table<Inventory> inventoryTable = database.getTable(Inventory.class);
        
        // select operation for list
        ArrayListSelectOperation<Inventory> operation =
            new ArrayListSelectOperation<Inventory>(inventoryTable, "partNumberIn");
        operation.setParameters(partNumbers);
        // SELECT PARTNUMBER, QUANTITY, MANFID FROM INVENTORY WHERE PARTNUMBER IN (?, ?, ?)
        operation.execute();
        
        // show results
        for (Inventory inventory: operation.readAll())
        {
            System.out.println(inventory.getPartNumber());
        }
        
        // clean up
        operation.close();
    }
}
