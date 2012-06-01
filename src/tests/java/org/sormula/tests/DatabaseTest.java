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
package org.sormula.tests;

import java.io.FileInputStream;
import java.io.IOException;
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

import javax.sql.DataSource;

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
    
    JdbcProperties jdbcProperties;
    String schema;
    String url;
    String user;
    String password;
    DataSource dataSource;
    Database database;
    Table<R> table;
    String qualifiedTableName;
    Random random = new Random(testSeed);
    boolean useTransacation;
    String sqlShutdown;
    String driverShutdown;
    List<R> all;
    boolean dataSourceDatabase;
    
    
    public DatabaseTest() 
    {
        try
        {
            jdbcProperties = new JdbcProperties(log.isDebugEnabled());
        }
        catch (IOException e)
        {
            log.error("error opening jdbc properties", e);
        }
    }
    
    
    public boolean isTestBigDecimal()
    {
        return jdbcProperties.getBoolean("testBigDecimal");
    }
    
    
    public boolean isTestLong()
    {
        return jdbcProperties.getBoolean("testLong");
    }
    
    
    public boolean isTestTime()
    {
        return jdbcProperties.getBoolean("testTime");
    }
    
    
    public boolean isTestIdentity()
    {
        return jdbcProperties.getBoolean("testIdentity");
    }


    public void openDatabase() throws Exception
    {
        openDatabase(false);
    }
    public void openDatabase(boolean useDataSource) throws Exception
    {
        // get connection
        Class.forName(jdbcProperties.getString("jdbc.driver"));
        schema = jdbcProperties.getString("jdbc.schema");
        url = jdbcProperties.getString("jdbc.url");
        user = jdbcProperties.getString("jdbc.user");
        password = jdbcProperties.getString("jdbc.password");
        useTransacation = jdbcProperties.getBoolean("jdbc.transaction");
        
        // shutdown commands
        sqlShutdown = jdbcProperties.getString("jdbc.shutdown.sql");
        driverShutdown = jdbcProperties.getString("jdbc.shutdown.driver");

        // simulated data source
        dataSource = new TestDataSource(this);
        
        // create sormula database
        if (useDataSource)
        {
            dataSourceDatabase = true;
            database = new Database(dataSource, schema);
        }
        else
        {
            dataSourceDatabase = false;
            Connection connection = getConnection();
            database = new Database(connection, schema);
        }
        database.setTimings(Boolean.parseBoolean(System.getProperty("timings")));
    }
    
    
    public Connection getConnection() throws SQLException
    {
        Connection connection;
        
        if (user.length() == 0 && password.length() == 0)
        {
            // no user and password
            connection = DriverManager.getConnection(url);
        }
        else
        {
            connection = DriverManager.getConnection(url, user, password); 
        }
        
        assert connection != null : "db connection is null";
        
        // set auto commit according to transaction property
        connection.setAutoCommit(!useTransacation);
        
        return connection;
    }
    
    
    public void createTable(Class<R> rowClass, String ddl) throws Exception
    {
        createTable(rowClass);
        qualifiedTableName = table.getQualifiedTableName();
        if (ddl != null) createTable(ddl);
    }
    
    
    protected void createTable(Class<R> rowClass) throws Exception
    {
        table = getDatabase().getTable(rowClass);
        assert table != null : "table creation error";
    }
    
    
    protected void createTable(String ddl) throws Exception
    {
        // drop table if already exists from previous test
        try
        {
            dropTable();
            log.debug("existing table " + qualifiedTableName + " was dropped before creating new version");
        }
        catch (SQLException e)
        {
            // ignore table does not exist exception
            log.debug("exception dropping " + qualifiedTableName + " :" + e.getMessage());
        }
        
        log.debug(ddl);
        Connection connection = getConnection();
        Statement statement = connection.createStatement();
        statement.executeUpdate(ddl);
        statement.close();
        if (useTransacation) connection.commit();
        connection.close();
    }
    
    
    public void dropTable() throws SQLException
    {
    	String ddl = "DROP TABLE " + qualifiedTableName;
    	log.debug(ddl);
        Connection connection = getConnection();
        Statement statement = connection.createStatement();
        statement.executeUpdate(ddl);
        statement.close();
        if (useTransacation) connection.commit();
        connection.close();
    }
    
    
    public void closeDatabase()
    {
        database.logTimings();
        
        try
        {
            if (!dataSourceDatabase) 
            {
                // database instance created without data source 
                // database.close() only closes connection if data source used
                database.getConnection().close();
            }
            
            database.close();
            
            if (sqlShutdown.length() > 0)
            {
                Connection connection = getConnection();
                Statement statement = connection.createStatement();
                statement.execute(sqlShutdown);
                statement.close();
                if (useTransacation) connection.commit();
                connection.close();
            }
            
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


    public DataSource getDataSource()
    {
        return dataSource;
    }


    public Database getDatabase()
    {
        return database;
    }


    public Table<R> getTable()
    {
        return table;
    }


    public int randomInt(int upper)
    {
        return random.nextInt(upper);
    }
    
    
    public String getSchema()
    {
        return schema;
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
    
    
    public void setAll(List<R> all)
    {
        this.all = all;
    }


    public R getRandom()
    {
        return all.get(randomInt(all.size()));
    }
    
    
    public Set<R> getRandomSet()
    {
        int size = Math.min(10, all.size());
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
    

    public JdbcProperties(boolean logProperties) throws IOException
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
    
    
    public String getString(String name)
    {
        return getProperty(name, "");
    }
    
    
    public boolean getBoolean(String name)
    {
        return Boolean.parseBoolean(getProperty(name, "false"));
    }
}