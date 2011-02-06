import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sormula.Database;
import org.sormula.Table;
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
	private static final Logger log = LoggerFactory.getLogger(SimpleExample.class);
    Connection connection;
    
    
    public static void main(String[] args) throws Exception
    {
    	log.info("begin");
        SimpleExample simpleExample = new SimpleExample();
        simpleExample.removeInventory(1234, 1);
        simpleExample.clearInventory("xyz");
        simpleExample.clearInventory2("xyz");
        simpleExample.close();
        log.info("end");
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
    
    
    // reduce inventory quantity for a part
    public void removeInventory(int partNumber, int quantity) throws Exception
    {
    	log.info("removeInventory");
    	
        // set up
        Database database = new Database(getConnection());
        Table<Inventory> inventoryTable = database.getTable(Inventory.class);
        
        // get part by primary key
        Inventory inventory = inventoryTable.select(partNumber);
        
        // update
        inventory.setQuantity(inventory.getQuantity() - quantity);
        inventoryTable.update(inventory);
        
        // clean up
        database.close();
    }    
    
    
    // clear inventory for a manufacturer
    public void clearInventory(String manufacturerId) throws Exception
    {
    	log.info("clearInventory");
    	
        // set up
        Database database = new Database(getConnection());
        Table<Inventory> inventoryTable = database.getTable(Inventory.class);
        List<Inventory> clearList = new ArrayList<Inventory>();
        
        // select for a specific manufacturer
        ListSelectOperation<Inventory> operation = inventoryTable.createSelectOperation("manf");
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
        database.close();
    }    
    
    
    // clear inventory for a manufacturer (alternate using FullListSelect)
    public void clearInventory2(String manufacturerId) throws Exception
    {
    	log.info("clearInventory2");
    	
        // set up
        Database database = new Database(getConnection());
        Table<Inventory> inventoryTable = database.getTable(Inventory.class);
        List<Inventory> clearList = new ArrayList<Inventory>();
        
        // select operation for a specific manufacturer
        FullListSelect<Inventory> operation = new FullListSelect<Inventory>(
        		inventoryTable.createSelectOperation("manf"));
        		
        // for all inventory of manufacturer
        for (Inventory inventory: operation.executeAll(manufacturerId))
        {
            // remember for update
            inventory.setQuantity(0);
            clearList.add(inventory);
        }
        
        // update
        inventoryTable.updateAll(clearList);
        
        // clean up
        database.close();
    }    
}
