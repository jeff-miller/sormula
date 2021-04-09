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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.NamingManager;
import javax.sql.DataSource;

import org.sormula.NoOpTransaction;
import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.cache.Cache;
import org.sormula.log.SormulaLogger;
import org.sormula.log.SormulaLoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;


/** 
 * Base class for all sormula tests. 
 * 
 * @author Jeff Miller
 */
public class DatabaseTest<R>
{
    static 
    {
        try
        {
            // configure logger from build.properties
            String loggerClassName = System.getProperty("logger.class", "");
            System.out.println("logger.class=" + loggerClassName);
            SormulaLoggerFactory.setLoggerClass(loggerClassName);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private static final SormulaLogger log = SormulaLoggerFactory.getClassLogger();
    
    static long testSeed;
    static JdbcProperties jdbcProperties;
    
    String schema;
    String url;
    String user;
    String password;
    DataSource dataSource;
    TestDatabase database;
    Table<R> table;
    Random random;
    boolean useTransacation;
    List<R> all;
    boolean dataSourceDatabase;
    Boolean testScrollableResultSets;

    
    @BeforeSuite(alwaysRun = true)
    public void beforeSuite()
    {
        if (log.isDebugEnabled()) log.debug("beforeSuite()");
        initJdbcProperties();
        initTestSeed();
        initJndi();
        initOutputDirectory();
        initSecurity();
    }

    
    @AfterSuite(alwaysRun = true)
    public void afterSuite()
    {
        if (log.isDebugEnabled()) log.debug("afterSuite()");
        sqlShutdown();
        driverShutdown();
    }
    
    
    protected void initJdbcProperties()
    {
        try
        {
            jdbcProperties = new JdbcProperties(true);
        }
        catch (IOException e)
        {
            log.error("error opening jdbc properties", e);
        }
    }
    
    
    protected void initTestSeed()
    {
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
    
    
    protected void initJndi()
    {
        // simulated JNDI context
        try
        {
            NamingManager.setInitialContextFactoryBuilder(new TestContextFactoryBuilder());
        }
        catch (NamingException e)
        {
            log.error("error setting initial context", e);
        }
    }

    
    protected void initOutputDirectory()
    {
        // some databases fail if parent of database file does not exist
        try
        {
            Path testOutputDirectory = Paths.get("test-output");
            if (!Files.exists(testOutputDirectory)) Files.createDirectory(testOutputDirectory);
        }
        catch (IOException e)
        {
            log.error("error creating test directory", e);
        }
    }
    
    
    protected void initSecurity()
    {
        // some databases require security permissions, turn off all java security checks since this is not confidential information
        // see DERBY-6648
        System.setSecurityManager(null);
    }
    
    
    protected void sqlShutdown()
    {
        try 
        {
            String sqlShutdown = jdbcProperties.getString("jdbc.shutdown.sql");
            if (sqlShutdown.length() > 0)
            {
                if (log.isDebugEnabled()) log.debug("execute sqlShutdown=" + sqlShutdown);
                
                boolean workAround = false;
                if (url == null && user == null && password == null) 
                {
                    // sometimes testng does not invoke beforeClass method
                    // open database here as a work-around
                    open();
                    workAround = true;
                }
                
                Connection connection = getConnection();
                Statement statement = connection.createStatement();
                statement.execute(sqlShutdown);
                statement.close();
                if (useTransacation) connection.commit();
                if (log.isDebugEnabled()) log.debug("close shutdown connection");
                connection.close();
                
                if (workAround) 
                {
                    close();
                }
            }
        } 
        catch (Exception e) 
        {
            log.error("sql shutdown error", e);
        }
    }
    
    
    protected void driverShutdown()
    {
        try 
        {
            String driverShutdown = jdbcProperties.getString("jdbc.shutdown.driver");
            if (driverShutdown.length() > 0)
            {
                if (log.isDebugEnabled()) log.debug("execute driverShutdown=" + driverShutdown);
                DriverManager.getConnection(driverShutdown);
            }
        } 
        catch (SQLException e) 
        {
            log.error("driver shutdown error", e);
        }
    }
    
    
    /**
     * All tests will inherit this method. Don't add BeforeClass annotation to specific tests.
     * 
     * @throws Exception if error
     * @see #open()
     */
    @BeforeClass(alwaysRun = true)
    public void beforeClass() throws Exception
    {
        if (log.isDebugEnabled()) log.debug("beforeClass() for " + getClass().getName());
        open();
    }
    
    
    /**
     * Default before class method. Override this method to change. Do not override {@link #beforeClass()}.
     * 
     * @throws Exception if error
     */
    protected void open() throws Exception
    {
        openDatabase();
    }
    
    
    /**
     * All tests will inherit this method. Don't add AfterClass annotation to specific tests.
     * 
     * @throws Exception if error
     * @see #close()
     */
    @AfterClass(alwaysRun = true)
    public void afterClass() throws Exception
    {
        if (log.isDebugEnabled()) log.debug("afterClass() for " + getClass().getName());
        close();
    }
    
    
    /**
     * Default after class method. Override this method to change. Do not override {@link #afterClass()}.
     * 
     * @throws Exception if error
     */
    protected void close() throws Exception
    {
        closeDatabase(); 
    }
    
    
    public boolean isTestScrollableResultSets()
    {
        if (testScrollableResultSets == null) throw new RuntimeException("database must be open to know scrollable property");
        return testScrollableResultSets;
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
    
    
    public boolean isTestRollback()
    {
        return jdbcProperties.getBoolean("testRollback");
    }
    
    
    public boolean isTestIdentity()
    {
        String testIdentity = jdbcProperties.getString("testIdentity").trim();
        if (testIdentity.equalsIgnoreCase("false")) return false;
        else return true; // could be true or custom identity column type
    }
    
    
    public boolean isTestIdentityOverride()
    {
        return jdbcProperties.getBoolean("testIdentityOverride");
    }
    
    
    public boolean isForeignKey()
    {
        return jdbcProperties.getBoolean("foreignKey");
    }
    
    
    // batch cascades and batch saves will work properly only if database returns update count
    // test batch cascades and batch saves if true
    public boolean isBatchReturnsUpdateCount()
    {
        return jdbcProperties.getBoolean("batchReturnsUpdateCount");
    }

    
    public String getIdentityColumnDDL()
    {
        String testIdentity = jdbcProperties.getString("testIdentity").trim();
        if (testIdentity.equalsIgnoreCase("true")) return "INTEGER GENERATED ALWAYS AS IDENTITY(START WITH 1)";
        else if (testIdentity.equalsIgnoreCase("default")) return "INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1)";
        else return testIdentity; // custom
    }
    
    
    public int getSecondsPrecision()
    {
        String secondsPrecision = jdbcProperties.getString("secondsPrecision").trim();
        if (secondsPrecision.length() > 0) return Integer.parseInt(secondsPrecision);
        else return 0;
    }
    
    
    public String getSecondsPrecisionDDL()
    {
        int secondsPrecision = getSecondsPrecision();
        if (secondsPrecision > 0) return "(" + secondsPrecision + ")";
        else return "";
    }

    
    public String getTimestampNullKeyword()
    {
        return jdbcProperties.getString("timestampNullKeyword").trim();
    }
    
    
    public String getBooleanDDL()
    {
        return jdbcProperties.getString("booleanDDL").trim();
    }
    public boolean isBooleanDDL()
    {
        String booleanDDL = getBooleanDDL().toUpperCase();
        return booleanDDL.startsWith("BIT") || booleanDDL.startsWith("BOOLEAN");
    }
    

    public boolean isUseTransacation()
    {
        return useTransacation;
    }


    public void openDatabase() throws Exception
    {
        openDatabase(false);
    }
    public void openDatabase(boolean useDataSource) throws Exception
    {
        if (useDataSource) openDatabase("");
        else openDatabase(null);
    }
    public void openDatabase(String dataSourceName) throws Exception
    {
        // get connection
        String jdbcDriver = jdbcProperties.getString("jdbc.driver");
        if (jdbcDriver.length() > 0) Class.forName(jdbcDriver); // optional for most drivers since jdk1.6 
        schema = jdbcProperties.getString("jdbc.schema");
        url = jdbcProperties.getString("jdbc.url");
        user = jdbcProperties.getString("jdbc.user");
        password = jdbcProperties.getString("jdbc.password");
        useTransacation = jdbcProperties.getBoolean("jdbc.transaction");

        // create sormula database
        if (dataSourceName == null)
        {
            // use connection
            if (log.isDebugEnabled()) log.debug("open sormula database with connection");
            dataSourceDatabase = false;
            Connection connection = getConnection();
            database = new TestDatabase(connection, schema);
        }
        else if (dataSourceName.equals(""))
        {
            // use data source directly (not through JNDI)
            if (log.isDebugEnabled()) log.debug("open sormula database with TestDataSource");
            dataSourceDatabase = true;
            dataSource = new TestDataSource(this); // simulated data source
            database = new TestDatabase(dataSource, schema);
        }
        else 
        {
            // use data source from JNDI
            if (log.isDebugEnabled()) log.debug("open sormula database via JNDI");
            dataSourceDatabase = true;
            dataSource = new TestDataSource(this); // simulated data source
            InitialContext ic = new InitialContext();
            ic.bind(dataSourceName, dataSource); // put data source in context for JNDI lookups
            database = new TestDatabase(dataSourceName, schema);
        }
        database.setTimings(Boolean.parseBoolean(System.getProperty("timings")));
        
        if (!useTransacation)
        {
            // allows transaction listener notification for cached tests
            database.setTransaction(new NoOpTransaction(database.getConnection()));
        }
        
        initMetaDataProperties();
        
        // each instance must use independent Random instance
        // create here since dependent upon testSeed
        random = new Random(testSeed);
    }
    
    
    protected void initMetaDataProperties() throws SQLException
    {
        DatabaseMetaData databaseMetaData = getConnection().getMetaData();
        testScrollableResultSets = databaseMetaData.supportsResultSetType(ResultSet.TYPE_SCROLL_INSENSITIVE) ||
                databaseMetaData.supportsResultSetType(ResultSet.TYPE_SCROLL_SENSITIVE);
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
        // create Table instance
        createTable(rowClass);

        // drop table if already exists from previous test
        dropTable(table.getQualifiedTableName());
        if (log.isDebugEnabled()) log.debug("existing table " + table.getQualifiedTableName() + " was dropped before creating new version");

        if (ddl != null) createTable(ddl);
    }
    
    
    protected void createTable(Class<R> rowClass) throws Exception
    {
        table = getDatabase().getTable(rowClass);
        assert table != null : "table creation error";
    }
    
    
    protected void createTable(String ddl) throws Exception
    {
        // use local connection
        Connection connection = getConnection();
        
        try
        {
            if (log.isDebugEnabled()) log.debug(ddl);
            Statement statement = connection.createStatement();
            statement.executeUpdate(ddl);
            statement.close();
            if (useTransacation) connection.commit();
        }
        catch (SQLException e)
        {
            log.error("error creating table using " + ddl, e);
        }
        finally
        {
            if (log.isDebugEnabled()) log.debug("close create table connection");
            connection.close();
        }
    }
    
    
    public void dropTable(String qualifiedTableName) throws Exception
    {
        // use local connection
        Connection connection = getConnection();
        
        try
        {
        	String ddl = "DROP TABLE " + qualifiedTableName;
        	if (log.isDebugEnabled()) log.debug(ddl);
            Statement statement = connection.createStatement();
            statement.executeUpdate(ddl);
            statement.close();
            if (useTransacation) connection.commit();
        }
        catch (SQLException e)
        {
            // ignore table does not exist exception
            if (log.isDebugEnabled()) log.debug("exception dropping " + qualifiedTableName + " :" + e.getMessage());
        }
        finally
        {
            if (log.isDebugEnabled()) log.debug("close drop table connection");
            connection.close();
        }
    }
    
    
    public void closeDatabase()
    {
        if (database != null) // skip if database is not open
        {
            database.logTimings();
            
            if (Boolean.parseBoolean(System.getProperty("cache.statistics")))
            {
                // log cache statistics
                Cache<R> cache = getTable().getCache();
                if (cache != null) cache.log();
            }
            
            try
            {
                if (!dataSourceDatabase) 
                {
                    // database instance created without data source 
                    // database.close() only closes connection if data source used
                    if (log.isDebugEnabled()) log.debug("close connection");
                    database.getConnection().close();
                }
                
                if (log.isDebugEnabled()) log.debug("close sormula database");
                database.close();
            }
            catch (SQLException e)
            {
                log.error("error closing database", e);
            }
        }
    }
    
    
    public void begin() throws SormulaException
    {
        getDatabase().getTransaction().begin();
    }
    
    
    public void commit() throws SormulaException
    {
        getDatabase().getTransaction().commit();
    }
    
    
    public void rollback() throws SormulaException
    {
        getDatabase().getTransaction().rollback();
    }


    public DataSource getDataSource()
    {
        return dataSource;
    }


    public TestDatabase getDatabase()
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
        Set<R> set = new HashSet<>(size * 2);
        
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
    private static final SormulaLogger log = SormulaLoggerFactory.getClassLogger();
    

    public JdbcProperties(boolean logProperties) throws IOException
    {
        String dbdir = System.getProperty("dbdir");
        assert dbdir != null : "No dbdir property set";
        
        // read db properties
        String jdbcPropertiesName = "jdbc/" + dbdir + "/jdbc.properties";
        try (InputStream is = new FileInputStream(jdbcPropertiesName))
        {
            load(is);
        }
        
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