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
package org.sormula.examples;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Properties;

import org.sormula.SormulaException;
import org.sormula.log.ClassLogger;


/** 
 * Base class for all sormula examples.
 * 
 * @author Jeff Miller
 */
public class ExampleBase
{
    private static final ClassLogger log = new ClassLogger();
    
    Connection connection;
    String schema;
    String sqlShutdown;
    String driverShutdown;
    String url;
    String user;
    String password;
    
    
    /**
     * Gets the connection that was created with {@link #openDatabase()}. Connection will be closed
     * when {@link #closeDatabase()} is invoked. Connection is defined
     * in /jdbc/DB/jdbc.properties file.
     * 
     * @return jdbc connection  
     */
    protected Connection getConnection()
    {
        return connection;
    }
    
    
    /**
     * Creates a new database connection. Connection must be closed by caller. Connection is defined
     * in /jdbc/DB/jdbc.properties file. The {@link #openDatabase()} must be invoked prior to using this
     * method.
     * 
     * @return jdbc connection 
     */
    public Connection createConnection()
    {
        Connection c;
        
        try
        {
            if (user.length() == 0 && password.length() == 0)
            {
                // no user and password
                log.info("get connection url=" + url);
                c = DriverManager.getConnection(url);
            }
            else
            {
                log.info("get connection url=" + url + " user=" + user + " password=" + password);
                c = DriverManager.getConnection(url, user, password); 
            }
        }
        catch (SQLException e)
        {
            log.error("error creating connection for " + url);
            c = null;
        }
        
        return c;
    }
    
    
    protected String getSchema()
    {
        return schema;
    }
    
    
    protected String getSchemaPrefix()
    {
        if (schema.length() == 0)
        {
            return "";
        }
        else
        {
            return schema + ".";
        }
    }
    
    
    /**
     * Gets a jdbc connection based upon values in jdbc/ddd/jdbc.properties (ddd is
     * value of environment variable dbdir.
     *  
     * @return jdbc connection
     * @throws Exception if error
     */
    protected void openDatabase() throws Exception
    {
        String dbdir = System.getProperty("dbdir");
        
        if (dbdir != null)
        {
            // read db properties
            InputStream is = new FileInputStream("jdbc/" + dbdir + "/jdbc.properties");
            Properties properties = new Properties();
            properties.load(is);
            is.close();
            properties.list(System.out);
            
            // get connection
            String jdbcDriver = properties.getProperty("jdbc.driver");
            if (jdbcDriver != null) Class.forName(jdbcDriver); // optional for most drivers since jdk1.6 
            url = properties.getProperty("jdbc.url");
            user = properties.getProperty("jdbc.user", "");
            password = properties.getProperty("jdbc.password", "");
            
            // shutdown commands
            sqlShutdown = properties.getProperty("jdbc.shutdown.sql", "");
            driverShutdown = properties.getProperty("jdbc.shutdown.driver", "");
            
            connection = createConnection();
            schema = properties.getProperty("jdbc.schema", "");
        }
        else
        {
            throw new Exception("No dbdir property set");
        }
    }
    

    /**
     * Closes the connection. Executes addition sql or driver commands defined by
     * jdbc.shutdown.* properties.  Some databases require a driver command or a sql 
     * command to close.
     * 
     * @throws Exception
     */
    protected void closeDatabase()
    {
        try
        {
            if (sqlShutdown.length() > 0)
            {
                Statement statement = connection.createStatement();
                statement.execute(sqlShutdown);
                statement.close();
            }
            
            connection.close();
            
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
    
    
    /**
     * Creates a table to use for example.
     * 
     * @param ddl sql to create table 
     * @throws Exception if error
     */
    protected void createTable(String ddl) throws Exception
    {
        System.out.println(ddl);
        Statement statement = connection.createStatement();
        statement.execute(ddl);
        statement.close();
    }
    
    
    /**
     * Drops table. Errors that occur with drop statement are ignored since table may not exist yet.
     * 
     * @param tableName name of table to drop 
     * @throws Exception if error
     */
    protected void dropTable(String tableName) throws Exception
    {
        Statement statement = connection.createStatement();
        
        // drop old from previous example
        try
        {
            String sql = "DROP TABLE " + tableName;
            System.out.println(sql);
            statement.executeUpdate(sql);
        }
        catch (SQLException e)
        {
            // assume ok since table may not exist
        }
        
        statement.close();
    }
    
    
    protected void printAll(Collection<?> c) throws SormulaException
    {
        if (c.size() > 0)
        {
            for (Object o: c)
                System.out.println(o);
        }
        else
        {
            System.out.println("no rows in table");
        }
    }
}