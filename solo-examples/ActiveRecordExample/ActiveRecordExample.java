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
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
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
 * example is db.script. db.script may be viewed with any text editor.
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
        ActiveRecordExample example = new ActiveRecordExample();
        example.createTable();
        example.removeInventory(1234, 1);
        example.clearInventory("xyz");
        example.selectByRange(1, 10);
        example.selectByRange2(1, 10);
        example.selectIn();
        example.closeDatabase();
        System.out.println("end");
    }
    
    
    public ActiveRecordExample() throws Exception 
    {
        dataSource = new BasicDataSource();
        ActiveDatabase.setDefault(new ActiveDatabase(dataSource));
    }
    
    
    public void createTable() throws Exception
    {
    	try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement())
    	{
	        System.out.println("create table");
	        statement.execute("CREATE TABLE INVENTORY(PARTNUMBER INTEGER, QUANTITY INTEGER, MANFID VARCHAR(40))");
    	}
    	catch (SQLException e)
    	{
    		// assume exception because table already exists
    		System.out.println(e);
    	}

    	// insert test records
    	ArrayList<Inventory> testRecords = new ArrayList<>();
    	testRecords.add(newRecord(1233, 105, "abcd"));
    	testRecords.add(newRecord( 777,   7, "abcd"));
    	testRecords.add(newRecord( 555,   5, "abcd"));
    	testRecords.add(newRecord(1234,  64, "abcd"));
    	testRecords.add(newRecord( 999,  99, "xyz"));
    	testRecords.add(newRecord( 998,  88, "xyx"));
    	
        // perform within transaction for the default active database
        ActiveTransaction transaction = new ActiveTransaction();
        try
        {
            transaction.begin();
            Inventory.table.deleteAll(); // start with empty table
            Inventory.table.insertAll(testRecords);
            transaction.commit();
        }
        catch (ActiveException e)
        {
            transaction.rollback();
            e.printStackTrace();
        }
    }
    private Inventory newRecord(int partNumber, int quantity, String manufacturerId)
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
     * @param minimumQuantity select rows with quantity of at least this amount
     * @param maximumQuantity select rows with quantity no more than this amount
     * @see QuantityRangeSelect
     */
    public void selectByRange(int minimumQuantity, int maximumQuantity) throws Exception
    {
        System.out.println("selectByRange " + minimumQuantity + " " + maximumQuantity);

        // select
        List<Inventory> results = Inventory.table.selectAllCustom(
                "where quantity between ? and ?", minimumQuantity, maximumQuantity);
        
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
     * @param minimumQuantity select rows with quantity of at least this amount
     * @param maximumQuantity select rows with quantity no more than this amount
     * @see QuantityRangeSelect
     */
    public void selectByRange2(int minimumQuantity, int maximumQuantity) throws Exception
    {
        System.out.println("selectByRange2 " + minimumQuantity + " " + maximumQuantity);
        
        // use a specific active data base (data source) instead of default
        ActiveTable<Inventory> table = new ActiveTable<>(new ActiveDatabase(dataSource), Inventory.class);

        // select
        List<Inventory> results = table.selectAllCustom(
                "where quantity between ? and ?", minimumQuantity, maximumQuantity);
        
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
        ArrayList<Integer> partNumbers = new ArrayList<>();
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
    
    
    public void closeDatabase() throws Exception
    {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement())
        {
            System.out.println("close database");
            statement.execute("shutdown compact;");
        }
    }
}


class BasicDataSource implements DataSource
{
    public BasicDataSource() throws Exception
    {
    }

    @Override
    public Connection getConnection() throws SQLException
    {
        return DriverManager.getConnection("jdbc:hsqldb:file:db");
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException
    {
        return null;
    }
    
    @Override
    public PrintWriter getLogWriter() throws SQLException
    {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException
    {
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException
    {
    }

    @Override
    public int getLoginTimeout() throws SQLException
    {
        return 0;
    }

    @Override
    public Logger getParentLogger() // only available in jdk 7 throws SQLFeatureNotSupportedException
    {
        return null;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException
    {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException
    {
        return false;
    }
}
