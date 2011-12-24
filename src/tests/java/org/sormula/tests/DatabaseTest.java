/* sormula - Simple object relational mapping
 * Copyright (C) 2011 Jeff Miller
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
package org.sormula.tests;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import org.sormula.Database;
import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.log.ClassLogger;


/** 
 * Base class for all sormula tests. 
 * 
 * @author Jeff Miller
 */
public class DatabaseTest<R>
{
    private static final ClassLogger log = new ClassLogger();
    
    static long testSeed;
    
    static
    {
        // always log JDBC driver properties
        try
        {
            new JdbcProperties(true);
        }
        catch (Exception e)
        {
            log.error("error reading JDBC properties", e);
        }
        
    	// allow seed to be supplied for repeatable tests
    	String seed = System.getProperty("seed", "");
    	if (seed.length() == 0)
    	{
    		testSeed = System.currentTimeMillis();
    		log.info("using random seed=" + testSeed);
    	}
    	else
    	{
    		testSeed = Long.parseLong(seed);
    		log.info("using property seed=" + testSeed);
    	}
    }
    
    Database database;
    Statement statement;
    Table<R> table;
    Random random = new Random(testSeed);
    boolean useTransacation;
    String sqlShutdown;
    String driverShutdown;
    List<R> all;
    
    
    public void openDatabase() throws Exception
    {
        Properties properties = new JdbcProperties(log.isDebugEnabled());
        
        // get connection
        Connection connection;
        Class.forName(properties.getProperty("jdbc.driver"));
        String url = properties.getProperty("jdbc.url");
        String user = properties.getProperty("jdbc.user", "");
        String password = properties.getProperty("jdbc.password", "");
        
        if (user.length() == 0 && password.length() == 0)
        {
            // no user and password
            connection = DriverManager.getConnection(url);
        }
        else
        {
            connection = DriverManager.getConnection(url, user, password); 
        }
        
        useTransacation = Boolean.parseBoolean(properties.getProperty("jdbc.transaction", "false"));
        
        // shutdown commands
        sqlShutdown = properties.getProperty("jdbc.shutdown.sql", "");
        driverShutdown = properties.getProperty("jdbc.shutdown.driver", "");
        
        // create sormula database
        assert connection != null : "db connection is null";
        database = new Database(connection, properties.getProperty("jdbc.schema", ""));
        database.setTimings(Boolean.parseBoolean(System.getProperty("timings")));
        
        // statement for ddl and other
        statement = connection.createStatement();
    }
    
    
    public void createTable(Class<R> rowClass, String ddl) throws Exception
    {
        table = getDatabase().getTable(rowClass);
        assert table != null : "table creation error";
        
        if (ddl != null)
        {
            // drop table if already exists from previous test
            try
            {
                dropTable();
                log.debug("existing table for " + rowClass.getCanonicalName() + " was dropped before creating new version");
            }
            catch (SQLException e)
            {
                // ignore table does not exist exception
                log.debug("exception dropping table for " + rowClass.getCanonicalName() +
                        " :" + e.getMessage());
            }
            
            log.debug(ddl);
            statement.executeUpdate(ddl);
        }
    }
    
    
    public void dropTable() throws SQLException
    {
    	String ddl = "DROP TABLE " + table.getQualifiedTableName();
    	log.debug(ddl);
        statement.executeUpdate(ddl);
    }
    
    
    public void closeDatabase()
    {
        getDatabase().logTimings();
        
        try
        {
            if (sqlShutdown.length() > 0)
            {
                statement.execute(sqlShutdown);
            }
            
            statement.close();
            database.close();
            
            if (driverShutdown.length() > 0)
            {
                DriverManager.getConnection(driverShutdown);
            }
        }
        catch (SQLException e)
        {
            log.warn("error closing database", e);
        }
    }
    
    
    public void begin() throws SormulaException
    {
        if (useTransacation)
        {
            getDatabase().getTransaction().begin();
        }
    }
    
    
    public void commit() throws SormulaException
    {
        if (useTransacation)
        {
            getDatabase().getTransaction().commit();
        }
    }


    public Database getDatabase()
    {
        return database;
    }


    public Table<R> getTable()
    {
        return table;
    }


    public Statement getStatement()
    {
        return statement;
    }
    
    
    public int randomInt(int upper)
    {
        return random.nextInt(upper);
    }
    
    
    public String getSchemaPrefix()
    {
        if (database.getSchema().length() == 0)
        {
            return "";
        }
        else
        {
            return database.getSchema() + ".";
        }
    }
    
    
    public void selectTestRows() throws SormulaException
    {
        all = getTable().selectAll();
    }
    
    
    public List<R> getAll()
    {
        return all;
    }
    
    
    public R getRandom()
    {
        return all.get(randomInt(all.size()));
    }
    
    
    public Set<R> getRandomSet()
    {
        int size = 10;
        Set<R> set = new HashSet<R>(size * 2);
        
        // choose random set
        for (int i = 0; i < size; ++i)
        {
            set.add(getRandom());
        }
        
        return set;
    }
}




class JdbcProperties extends Properties
{
    private static final long serialVersionUID = 1L;
    private static final ClassLogger log = new ClassLogger();
    

    public JdbcProperties(boolean logProperties) throws Exception
    {
        String dbdir = System.getProperty("dbdir");
        assert dbdir != null : "No dbdir property set";
        
        // read db properties
        String jdbcPropertiesName = "jdbc/" + dbdir + "/jdbc.properties";
        InputStream is = new FileInputStream(jdbcPropertiesName);
        load(is);
        is.close();
        
        if (logProperties)
        {
            log.info("propertes from " + jdbcPropertiesName);
            list(System.out);
        }
    }
}