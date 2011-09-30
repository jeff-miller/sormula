import java.sql.Connection;
import java.sql.DriverManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * <p>
 * Everything needed for this example is in the one directory that contains 
 * this class. The database used by this example is exampleDB.script. exampleDB.script 
 * may be viewed with any text editor.
 * <p>
 * Compile with compile.bat 
 * Run with run.bat
 *  
 * @author Jeff Miller
 */
public class ZeroConfigExample
{
	static final Logger log = LoggerFactory.getLogger(ZeroConfigExample.class);
    Connection connection;
    
    
    public static void main(String[] args) throws Exception
    {
    	log.info("begin");
    	ZeroConfigExample example = new ZeroConfigExample();
    	example.deleteAll(); // start with empty database
    	example.insert(1234);
    	example.insert(5555);
    	example.insert(1111);
    	example.update(5555);
    	example.delete(1111);
    	example.close();
        log.info("end");
    }
    
    
    public ZeroConfigExample() throws Exception
    {
        Class.forName("org.hsqldb.jdbc.JDBCDriver");
        connection = DriverManager.getConnection("jdbc:hsqldb:file:exampleDB;shutdown=true");
    }
    
    
    public Connection getConnection()
    {
        return connection;
    }


    public void close() throws Exception
    {
        connection.close();
    }
    
    
    public void insert(int partNumber) throws Exception
    {
    	log.info("insert");
    	
        // set up
        Database database = new Database(getConnection());
        Table<Inventory> inventoryTable = database.getTable(Inventory.class);
        
        // create row
        Inventory inventory = new Inventory();
        inventory.setPartNumber(partNumber);
        inventory.setManufacturerId("Acme");
        inventory.setQuantity(99);
        
        // insert into db
        inventoryTable.insert(inventory);
        
        // clean up
        database.close();
    }    
    
    
    public void update(int partNumber) throws Exception
    {
        log.info("update");
        
        // set up
        Database database = new Database(getConnection());
        Table<Inventory> inventoryTable = database.getTable(Inventory.class);
        
        // select row
        Inventory inventory = inventoryTable.select(partNumber);
        
        // update
        inventory.setQuantity(1000);
        inventoryTable.update(inventory);
        
        // clean up
        database.close();
    }    
    
    
    public void delete(int partNumber) throws Exception
    {
        log.info("delete");
        
        // set up
        Database database = new Database(getConnection());
        Table<Inventory> inventoryTable = database.getTable(Inventory.class);
        
        // select row
        Inventory inventory = inventoryTable.select(partNumber);
        
        // delete
        inventoryTable.delete(inventory);
        
        // clean up
        database.close();
    }    
    
    
    public void deleteAll() throws Exception
    {
        Database database = new Database(getConnection());
        Table<Inventory> inventoryTable = database.getTable(Inventory.class);
        inventoryTable.deleteAll();
    }
}
