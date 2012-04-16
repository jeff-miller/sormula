import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.sormula.active.ActiveDatabase;
import org.sormula.active.ActiveException;
import org.sormula.active.ActiveTable;
import org.sormula.active.ActiveTransaction;


/**
 * SimpleExample converted to use active record package. Everything needed for this 
 * example is in the directory that contains this class. The database used by this 
 * example is simpleDB.script. simpleDB.script may be viewed with any text editor.
 * <p>
 * Compile with compile.bat<br> 
 * Run with run.bat
 * </p>
 *  
 * @author Jeff Miller
 */
public class ActiveRecordExample
{
    DataSource dataSource;
    
    
    public static void main(String[] args) throws Exception
    {
        System.out.println("begin");
        ActiveRecordExample simpleExample = new ActiveRecordExample();
        simpleExample.removeInventory(1234, 1);
        simpleExample.clearInventory("xyz");
        simpleExample.selectByRange(1, 10);
        simpleExample.selectByRange2(1, 10);
        simpleExample.selectIn();
        System.out.println("end");
    }
    
    
    public ActiveRecordExample() throws Exception 
    {
        dataSource = new BasicDataSource();
        ActiveDatabase.setDefault(new ActiveDatabase(dataSource));
    }
    
    
    /**
     * Reduces inventory quantity for a part.
     * 
     * @param partNumber id of part to affect 
     * @param delta reduce inventory quantity by this amount
     */
    public void removeInventory(int partNumber, int delta) 
    {
        System.out.println("removeInventory");
        
        // perform within transaction for the default active database
        ActiveTransaction transaction = new ActiveTransaction();

        try
        {
            transaction.begin();
            
            // get part by primary key
            Inventory inventory = Inventory.table.select(partNumber);
            
            // update
            inventory.setQuantity(inventory.getQuantity() - delta);
            inventory.update();
            transaction.commit();
        }
        catch (ActiveException e)
        {
            transaction.rollback();
            e.printStackTrace();
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
    	
        // perform within transaction for an active database
        ActiveTransaction transaction = new ActiveTransaction(new ActiveDatabase(dataSource));
        
        try
        {
            transaction.begin();
            
            // select for a specific manufacturer ("manf" is name of where annotation in Inventory.java)
            List<Inventory> list = Inventory.table.selectAllWhere("manf", manufacturerId);
            
            // for all inventory of manufacturer
            for (Inventory inventory: list)
            {
                inventory.setQuantity(0);
            }
            
            // update
            Inventory.table.updateAll(list);
            
            transaction.commit();
        }
        catch (ActiveException e)
        {
            transaction.rollback();
            e.printStackTrace();
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
    public void selectByRange(int minimumQuanity, int maximumQuantity) throws Exception
    {
        System.out.println("selectByRange " + minimumQuanity + " " + maximumQuantity);

        // select
        List<Inventory> results = Inventory.table.selectAllCustom(
                "where quantity between ? and ?", minimumQuanity, maximumQuantity);
        
        // show results
        for (Inventory inventory: results)
        {
            System.out.println(inventory.getPartNumber() + " quantity=" + inventory.getQuantity());
        }
    }
    
    
    /**
     * Select all inventory with quantity in a range. Part and quantity are logged for
     * each row selected. Uses explicit active database instead of a default.
     * 
     * @param minimumQuanity select rows with quantity of at least this amount
     * @param maximumQuantity select rows with quantity no more than this amount
     * @see QuantityRangeSelect
     */
    public void selectByRange2(int minimumQuanity, int maximumQuantity) throws Exception
    {
        System.out.println("selectByRange2 " + minimumQuanity + " " + maximumQuantity);
        
        // use a specific active data base (data source) instead of default
        ActiveTable<Inventory> table = new ActiveTable<Inventory>(
                new ActiveDatabase(dataSource), Inventory.class);

        // select
        List<Inventory> results = table.selectAllCustom(
                "where quantity between ? and ?", minimumQuanity, maximumQuantity);
        
        // show results
        for (Inventory inventory: results)
        {
            System.out.println(inventory.getPartNumber() + " quantity=" + inventory.getQuantity());
        }
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
        
        // select 
        List<Inventory> results = Inventory.table.selectAllWhere("partNumberIn", partNumbers);

        // show results
        for (Inventory inventory: results)
        {
            System.out.println(inventory.getPartNumber());
        }
    }
}


class BasicDataSource implements DataSource
{
    public BasicDataSource() throws Exception
    {
        Class.forName("org.hsqldb.jdbc.JDBCDriver");
    }

    public Connection getConnection() throws SQLException
    {
        return DriverManager.getConnection("jdbc:hsqldb:file:simpleDB;shutdown=true");
    }

    public Connection getConnection(String username, String password) throws SQLException
    {
        return null;
    }
    
    public PrintWriter getLogWriter() throws SQLException
    {
        return null;
    }

    public void setLogWriter(PrintWriter out) throws SQLException
    {
    }

    public void setLoginTimeout(int seconds) throws SQLException
    {
    }

    public int getLoginTimeout() throws SQLException
    {
        return 0;
    }

    public Logger getParentLogger() throws SQLFeatureNotSupportedException
    {
        return null;
    }

    public <T> T unwrap(Class<T> iface) throws SQLException
    {
        return null;
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException
    {
        return false;
    }
}
