import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;

import org.sormula.Database;
import org.sormula.Table;
import org.sormula.operation.ArrayListSelectOperation;
import org.sormula.operation.ListSelectOperation;


/**
 * A simple example to show basic sormula features. Everything needed for this example is
 * in the directory that contains this class. The database used by this example is
 * simpleDB.script. simpleDB.script may be viewed with any text editor.
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
        SimpleExample simpleExample = new SimpleExample();
        simpleExample.removeInventory(1234, 1);
        simpleExample.clearInventory("xyz");
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
        Database database = new Database(connection);
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
        Database database = new Database(connection);
        Table<Inventory> inventoryTable = database.getTable(Inventory.class);
        
        // select for a specific manufacturer ("manf" is name of where annotation in Inventory.java)
        ListSelectOperation<Inventory> operation = new ArrayListSelectOperation<>(inventoryTable, "manf");
        		
        // for all inventory of manufacturer
        List<Inventory> list = operation.selectAll(manufacturerId);
        for (Inventory inventory: list)
        {
            inventory.setQuantity(0);
        }
        
        // update
        inventoryTable.updateAll(list);
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
        Database database = new Database(connection);
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
        Database database = new Database(connection);
        Table<Inventory> inventoryTable = database.getTable(Inventory.class);
        
        // select operation for range
        QuantityRangeSelect operation = new QuantityRangeSelect(inventoryTable);
        operation.setRange(minimumQuanity, maximumQuantity);
        
        // show results
        for (Inventory inventory: operation.selectAll())
        {
            System.out.println(inventory.getPartNumber() + " quantity=" + inventory.getQuantity());
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
        
        // set up
        Database database = new Database(connection);
        Table<Inventory> inventoryTable = database.getTable(Inventory.class);
        
        // select operation for list 
        // SELECT PARTNUMBER, QUANTITY, MANFID FROM INVENTORY WHERE PARTNUMBER IN (?, ?, ?)
        ArrayListSelectOperation<Inventory> operation =
            new ArrayListSelectOperation<>(inventoryTable, "partNumberIn");

        // show results
        for (Inventory inventory: operation.selectAll(partNumbers))
        {
            System.out.println(inventory.getPartNumber());
        }
    }
}
