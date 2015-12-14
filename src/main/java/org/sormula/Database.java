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
package org.sormula;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.sormula.annotation.ExplicitTypeAnnotationReader;
import org.sormula.annotation.ImplicitType;
import org.sormula.log.ClassLogger;
import org.sormula.operation.ModifyOperation;
import org.sormula.operation.SqlOperation;
import org.sormula.operation.cascade.lazy.AbstractLazySelector;
import org.sormula.operation.cascade.lazy.DurableLazySelector;
import org.sormula.operation.monitor.OperationTime;
import org.sormula.translator.NameTranslator;
import org.sormula.translator.TypeTranslator;
import org.sormula.translator.TypeTranslatorMap;
import org.sormula.translator.standard.BigDecimalTranslator;
import org.sormula.translator.standard.BooleanTranslator;
import org.sormula.translator.standard.ByteTranslator;
import org.sormula.translator.standard.DateTranslator;
import org.sormula.translator.standard.DoubleTranslator;
import org.sormula.translator.standard.FloatTranslator;
import org.sormula.translator.standard.GregorianCalendarTranslator;
import org.sormula.translator.standard.InstantTranslator;
import org.sormula.translator.standard.IntegerTranslator;
import org.sormula.translator.standard.LocalDateTranslator;
import org.sormula.translator.standard.LocalTimeTranslator;
import org.sormula.translator.standard.LongTranslator;
import org.sormula.translator.standard.ObjectTranslator;
import org.sormula.translator.standard.ShortTranslator;
import org.sormula.translator.standard.SqlDateTranslator;
import org.sormula.translator.standard.SqlTimeTranslator;
import org.sormula.translator.standard.SqlTimestampTranslator;
import org.sormula.translator.standard.StringTranslator;


/**
 * Source of {@link Table} objects for reading/writing from/to database. For single threaded use 
 * only. Construct new instances for each transaction and/or thread.
 * <p>
 * Example - Construct database from JDBC connection:
 * <blockquote><pre>
 * Connection connection = ... // jdbc connection
 * Database database = new Database(connection);
 * Table&lt;MyRow&gt; table = database.getTable(MyRow.class);
 * table.selectAll();
 * </pre></blockquote>
 * <p>
 * Example - Construct database from JNDI:
 * <blockquote><pre>
 * Database database = new Database("java:comp/env/mydatasource");
 * Table&lt;MyRow&gt; table = database.getTable(MyRow.class);
 * table.selectAll();
 * </pre></blockquote>
 * 
 * @since 1.0
 * @author Jeff Miller
 */
public class Database implements TypeTranslatorMap, AutoCloseable
{
    private static final ClassLogger log = new ClassLogger();
    String dataSourceName;
    DataSource dataSource;
    Connection connection;
    String schema;
    Map<String, Table<?>> tableMap; // key is row class canonical name
    Transaction transaction;
    List<Class<? extends NameTranslator>> nameTranslatorClasses;
    Map<String, OperationTime> operationTimeMap;
    OperationTime totalOperationTime;
    boolean timings;
    boolean readOnly;
    Map<String, TypeTranslator<?>> typeTranslatorMap; // key is row class canonical name
    
    
    /**
     * Constructs for JNDI name and no schema. All sql table names will not include a schema prefix.
     * <p>
     * {@link DataSource} will be obtained from JNDI look up of dataSourceName. Typically web containers
     * require data source to be configured with a name like "someds" but the full path to the
     * data source contains an implied context of "java:comp/env". So the constructor parameter, 
     * dataSourceName, would be "java:comp/env/someds".
     * <p>
     * Connection will be obtained with {@link DataSource#getConnection()} and connection will be 
     * closed when {@link #close()} is invoked.
     * <p>
     * Use this constructor when a {@link DataSource} is required, for example when
     * {@link DurableLazySelector} is used.
     * 
     * @param dataSourceName JNDI full path to data source definition; example "java:comp/env/someds"
     * @throws SormulaException if error
     * @since 1.9 and 2.3
     */
    public Database(String dataSourceName) throws SormulaException
    {
        this(dataSourceName, "");
    }
    

    /**
     * Constructs for JNDI name and schema. All sql table names will be prefixed with schema name
     * in form of "schema.table".
     * <p>
     * {@link DataSource} will be obtained from JNDI look up of dataSourceName. Typically web containers
     * require data source to be configured with a name like "someds" but the full path to the
     * data source contains an implied context of "java:comp/env". So the constructor parameter, 
     * dataSourceName, would be "java:comp/env/someds".
     * <p>
     * Connection will be obtained with {@link DataSource#getConnection()} and connection will be 
     * closed when {@link #close()} is invoked.
     * <p>
     * Use this constructor when a {@link DataSource} is required, for example when
     * {@link DurableLazySelector} is used.
     * 
     * @param dataSourceName JNDI full path to data source definition; example "java:comp/env/someds"
     * @param schema name of schema to be prefixed to table name in all table names in sql statements;
     * {@link Connection#getCatalog()} is typically the schema name but catalog methods are inconsistently
     * supported by jdbc drivers
     * @throws SormulaException if error
     * @since 1.9 and 2.3
     */
    public Database(String dataSourceName, String schema) throws SormulaException
    {
        this.dataSourceName = dataSourceName;
        
        try
        {
            Context context = new InitialContext();
            dataSource = (DataSource)context.lookup(dataSourceName);
            if (dataSource != null) init(dataSource.getConnection(), schema);
            else throw new SormulaException("no data source found for dataSourceName=" + dataSourceName);
        }
        catch (NamingException e)
        {
            throw new SormulaException("erroring getting data source", e);
        }
        catch (SQLException e)
        {
            throw new SormulaException("erroring getting connection from data source", e);
        }
    }

    
    /**
     * Constructs for no schema. All sql table names will not include a schema prefix.
     * <p>
     * Connection will be obtained with {@link DataSource#getConnection()} and connection will be 
     * closed when {@link #close()} is invoked.
     * <p>
     * Use this constructor when a {@link DataSource} is required, for example when
     * {@link DurableLazySelector} is used.
     * 
     * @param dataSource obtain JDBC connection from this data source
     * @throws SormulaException if error
     * @since 1.8 and 2.2
     */
    public Database(DataSource dataSource) throws SormulaException
    {
        this(dataSource, "");
    }
    

    /**
     * Constructs for schema. All sql table names will be prefixed with schema name
     * in form of "schema.table".
     * <p>
     * Connection will be obtained with {@link DataSource#getConnection()} and connection will be 
     * closed when {@link #close()} is invoked.
     * <p>
     * Use this constructor when a {@link DataSource} is required, for example when
     * {@link DurableLazySelector} is used.
     * 
     * @param dataSource obtain JDBC connection from this data source
     * @param schema name of schema to be prefixed to table name in all table names in sql statements;
     * {@link Connection#getCatalog()} is typically the schema name but catalog methods are inconsistently
     * supported by jdbc drivers
     * @throws SormulaException if error
     * @since 1.8 and 2.2
     */
    public Database(DataSource dataSource, String schema) throws SormulaException
    {
        this.dataSource = dataSource;
        try
        {
            init(dataSource.getConnection(), schema);
        }
        catch (SQLException e)
        {
            throw new SormulaException("erroring getting connection from data source", e);
        }
    }

    
    /**
     * Gets the JNDI name to use to look up {@link DataSource}. This name was supplied in constructors:
     * {@link #Database(String)} and {@link #Database(String, String)}.
     * 
     * @return data source JNDI name or null if none
     * @since 1.9 and 2.3
     */
    public String getDataSourceName()
    {
        return dataSourceName;
    }


    /**
     * Gets data source supplied in constructors, {@link #Database(DataSource)} and
     * {@link #Database(DataSource, String)}.
     * 
     * @return data source or null if none was supplied during construction
     */
    public DataSource getDataSource()
    {
        return dataSource;
    }


    /**
     * Constructs for no schema. All sql table names will not include a schema prefix.
     * 
     * @param connection JDBC connection
     */
    public Database(Connection connection)
    {
        this(connection, "");
    }
    
    
    /**
     * Constructs for schema. All sql table names will be prefixed with schema name
     * in form of "schema.table".
     * 
     * @param connection JDBC connection
     * @param schema name of schema to be prefixed to table name in all table names in sql statements;
     * {@link Connection#getCatalog()} is typically the schema name but catalog methods are inconsistently
     * supported by jdbc drivers
     */
    public Database(Connection connection, String schema)
    {
        try
        {
            init(connection, schema);
        }
        catch (SormulaException e)
        {
            // unlikely class construction error, don't throw to keep backward compatibility with version 1.5
            // since previous versions did not have constructor signature with "throws SormulaException"
            // if error creating a TypeTranslator, then it will be apparent with first use
            log.error("error initializing types", e);
        }
    }
    
    
    /**
     * Invoked by constructor to initialize.
     * 
     * @param connection JDBC connection
     * @param schema name of schema to be prefixed to table name in all table names in sql statements;
     * {@link Connection#getCatalog()} is typically the schema name but jdbc drivers are not consistent
     * @throws SormulaException if error
     */
    protected void init(Connection connection, String schema) throws SormulaException
    {
        this.connection = connection;
        this.schema = schema;
        tableMap = new HashMap<>();
        transaction = initTransaction(connection);
        nameTranslatorClasses = new ArrayList<>(4);
        operationTimeMap = new HashMap<>();
        totalOperationTime = new OperationTime("Database totals");
        totalOperationTime.setDescription("All operations for database");
        initTypeTranslatorMap();
    }

    
    /**
     * Invoked by constructor to initialize default type translators and type translators
     * annotated on {@link Database} class.
     * <table>
     * <caption>Default Translators</caption>
     * <tr><th>Field Type</th><th>Translator</th></tr>
     * <tr><td>boolean / java.lang.Boolean</td><td>{@link BooleanTranslator}</td></tr>
     * <tr><td>byte / java.lang.Byte</td><td>{@link ByteTranslator}</td></tr>
     * <tr><td>double / java.lang.Double</td><td>{@link DoubleTranslator}</td></tr>
     * <tr><td>float / java.lang.Float</td><td>{@link FloatTranslator}</td></tr>
     * <tr><td>int / java.lang.Integer</td><td>{@link IntegerTranslator}</td></tr>
     * <tr><td>long / java.lang.Long</td><td>{@link LongTranslator}</td></tr>
     * <tr><td>short / java.lang.Short</td><td>{@link ShortTranslator}</td></tr>
     * <tr><td>java.math.BigDecimal</td><td>{@link BigDecimalTranslator}</td></tr>
     * <tr><td>java.lang.String</td><td>{@link StringTranslator}</td></tr>
     * <tr><td>java.util.Date</td><td>{@link DateTranslator}</td></tr>
     * <tr><td>java.sql.Date</td><td>{@link SqlDateTranslator}</td></tr>
     * <tr><td>java.sql.Time</td><td>{@link SqlTimeTranslator}</td></tr>
     * <tr><td>java.sql.Timestamp</td><td>{@link SqlTimestampTranslator}</td></tr>
     * <tr><td>java.util.GregorianCalendar</td><td>{@link GregorianCalendarTranslator}</td></tr>
     * <tr><td>java.time.LocalDate</td><td>{@link LocalDateTranslator}</td></tr>
     * <tr><td>java.time.LocalTime</td><td>{@link LocalTimeTranslator}</td></tr>
     * <tr><td>java.time.Instant</td><td>{@link InstantTranslator}</td></tr>
     * </table>
     * @throws SormulaException if error
     */
    protected void initTypeTranslatorMap() throws SormulaException
    {
        typeTranslatorMap = new HashMap<>(50);
        
        // standard primitives (used by RowTranslator#initColumnTranslators)
        putTypeTranslator("boolean", new BooleanTranslator());
        putTypeTranslator("byte", new ByteTranslator());
        putTypeTranslator("double", new DoubleTranslator());
        putTypeTranslator("float", new FloatTranslator());
        putTypeTranslator("int", new IntegerTranslator());
        putTypeTranslator("long", new LongTranslator());
        putTypeTranslator("short", new ShortTranslator());

        // standard types
        putTypeTranslator(BigDecimal.class, new BigDecimalTranslator());
        putTypeTranslator(Boolean.class, new BooleanTranslator());
        putTypeTranslator(Byte.class, new ByteTranslator());
        putTypeTranslator(Double.class, new DoubleTranslator());
        putTypeTranslator(Float.class, new FloatTranslator());
        putTypeTranslator(Integer.class, new IntegerTranslator());
        putTypeTranslator(Long.class, new LongTranslator());
        putTypeTranslator(Short.class, new ShortTranslator());
        putTypeTranslator(Object.class, new ObjectTranslator());
        putTypeTranslator(String.class, new StringTranslator());
        putTypeTranslator(java.util.Date.class, new DateTranslator());
        putTypeTranslator(java.sql.Date.class, new SqlDateTranslator());
        putTypeTranslator(java.sql.Time.class, new SqlTimeTranslator());
        putTypeTranslator(java.sql.Timestamp.class, new SqlTimestampTranslator());
        putTypeTranslator(GregorianCalendar.class, new GregorianCalendarTranslator());
        putTypeTranslator(LocalDate.class, new LocalDateTranslator());
        putTypeTranslator(LocalTime.class, new LocalTimeTranslator());
        putTypeTranslator(Instant.class, new InstantTranslator());
        
        // custom types
        try
        {
            new ExplicitTypeAnnotationReader(this, this.getClass()).install();
        }
        catch (Exception e)
        {
            throw new SormulaException("error getting ExplicitType from database " + 
                    getClass().getCanonicalName(), e);
        }
    }
    
    
    /**
     * Constructs a transaction to use. Default is {@link Transaction}. Subclasses can
     * override to use transaction other than the default.
     * 
     * @param connection JDBC connection to database
     * @return transaction for all operations using this database
     * @throws SormulaException if error
     */
    protected Transaction initTransaction(Connection connection) throws SormulaException
    {
        return new Transaction(connection);
    }
    
    
    /**
     * Gets transaction for connection. A {@link Transaction} is optional for most situations. {@link Transaction}
     * is provided for basic JDBC transaction support if desired. Since {@link Database} is
     * single threaded, only one {@link Transaction} object exists per instance of {@link Database}.
     * <p>
     * {@link AbstractLazySelector} and subclasses start a new transaction if {@link Transaction#isActive()} is 
     * false. Use a custom subclass of {@link AbstractLazySelector} and override begin, commit, and rollback 
     * methods to avoid this default behavior. 
     * 
     * @return transaction for this database
     */
    public Transaction getTransaction() 
    {
		return transaction;
	}


    /**
     * Sets a custom implementation of {@link Transaction} subclass. This method should be
     * invoked prior to creating {@link Table} objects that use this {@link Database} and
     * prior to using {@link #getTable(Class)} or {@link #getTable(Class, boolean)}.
     * 
     * @param transaction transaction for this database
     * @since 3.0
     */
	public void setTransaction(Transaction transaction)
    {
        this.transaction = transaction;
    }

    
    /**
     * Invokes {@link Table#flush()} on all known table objects in this database.
     * 
     * @throws SormulaException if error
     */
    public void flush() throws SormulaException
    {
        for (Table<?> t: tableMap.values()) t.flush();
    }


    /**
     * Dereferences all objects used. Does not close connection if these constructors were used:
     * {@link #Database(Connection)} or {@link #Database(Connection, String)}.
     * <p>
     * Closes connection if this class created the connection. Connection is created by this class
     * when any of the data source constructors are used. This method is required when any of these
     * data source constructors are used: {@link #Database(DataSource)}, {@link #Database(DataSource, String)},
     * {@link #Database(String)}, {@link #Database(String, String)}.
     */
    public void close()
    {
        if (dataSource != null && connection != null)
        {
            // assume connection was obtained from data source
            try
            {
                connection.close();
            }
            catch (SQLException e)
            {
                // prior to v1.8, close did not use throws in signature, keep backward compatible
                log.error("error closing connection", e);
            }
        }
        
        dataSourceName = null;
        dataSource = null;
        connection = null;
        schema = null;
        tableMap = null;
        transaction = null;
        nameTranslatorClasses = null;
        operationTimeMap = null;
        totalOperationTime = null;
        typeTranslatorMap = null;
    }
    

    /**
     * Gets connection to use for sql operations. Used by {@link SqlOperation} subclasses
     * to obtain the connection to use for the operation methods.
     * 
     * @return JDBC connection
     */
    public Connection getConnection()
    {
        return connection;
    }


    /**
     * Gets the schema name supplied in constructor.
     * 
     * @return schema name or empty string for no schema
     */
    public String getSchema()
    {
        return schema;
    }
    

    /**
     * Gets read-only indicator.
     * 
     * @return true if modify operations are not permitted
     * @since 1.6 and 2.0
     * @see SqlOperation#isReadOnly()
     */
    public boolean isReadOnly()
    {
        return readOnly;
    }


    /**
     * Sets read-only indicator. When true, modify operations,
     * {@link ModifyOperation} will fail with an exception. By default
     * read-only is false. Set to true as a safe-guard to prevent accidental
     * modification of database.
     * 
     * @param readOnly true to prevent modify operations
     * @since 1.6 and 2.0
     * @see SqlOperation#setReadOnly(boolean)
     */
    public void setReadOnly(boolean readOnly)
    {
        this.readOnly = readOnly;
    }
    
    
    /**
     * Gets table object for reading/writing row objects of type R from/to
     * table. Table objects are cahced in map by canonical class name. If
     * one exists, it is returned, otherwise a default one is created if create is true.
     * <p>
     * This method is optional. A table object may also be created with {@link Table} constructor.
     * 
     * @param <R> row class type
     * @param rowClass annotations on this class determine mapping from row objects to/from database 
     * @param create true to create new instance of {@link Table} if {@link #Database(Connection)} does not have one yet;
     * false to return null if no {@link Table} instance is found for rowClass
     * @return table object for reading/writing row objects of type R from/to database; null if table
     * does not exist and create is false
     * @throws SormulaException if error
     * @since 1.7 and 2.1
     */
    public <R> Table<R> getTable(Class<R> rowClass, boolean create) throws SormulaException
    {
        Table<?> table = tableMap.get(rowClass.getCanonicalName());
        
        if (table == null && create)
        {
            // default
            table = new Table<>(this, rowClass);
            addTable(table);
        }

        @SuppressWarnings("unchecked") // tableMap contains different types but always same type for rowClass name
        Table<R> t = (Table<R>)table;
        return t;
    }
    

    /**
     * Gets table object for reading/writing row objects of type R from/to
     * table. Table objects are cached in map by canonical class name. If
     * one exists, it is returned, otherwise a default one is created.
     * <p>
     * This method is optional. A table object may also be created with {@link Table} constructor.
     * 
     * @param <R> row class type
     * @param rowClass annotations on this class determine mapping from row objects to/from database 
     * @return table object for reading/writing row objects of type R from/to database
     * @throws SormulaException if error
     */
	public <R> Table<R> getTable(Class<R> rowClass) throws SormulaException
    {
	    return getTable(rowClass, true);
    }
    
    
    /**
     * Adds a table object to map to be used for row objects of type
     * {@link Table#getClass()}. Use this method to save table in map. This method provides
     * a way to ensure that a custom subclass of {@link Table} is returned from {@link #getTable(Class)}
     * for the table row class. 
     * 
     * @param table table object to add to map (table row class cannonical name is key)
     */
    public void addTable(Table<?> table)
    {
        tableMap.put(table.getRowClass().getCanonicalName(), table);
    }
	
	
    /**
     * Gets the default name translator classes for tables when none is specified for the table.
     * 
     * @return list of default name translator class; empty list if none
     * @since 1.8 and 2.2
     * @see Table#translateName(String)
     */
	public List<Class<? extends NameTranslator>> getNameTranslatorClasses()
    {
        return nameTranslatorClasses;
    }


	/**
	 * Adds a default name translator class.
	 * 
	 * @param nameTranslatorClass class to use to translate table/column names
	 * @since 1.8 and 2.2
	 * @see Table#translateName(String)
	 */
    public void addNameTranslatorClass(Class<? extends NameTranslator> nameTranslatorClass)
    {
        nameTranslatorClasses.add(nameTranslatorClass);
    }


    /**
     * Removes a name translator class.
     * 
     * @param nameTranslatorClass class to remove
     * @since 1.8 and 2.2
     * @see Table#translateName(String)
     */
    public void removeNameTranslatorClass(Class<? extends NameTranslator> nameTranslatorClass)
    {
        nameTranslatorClasses.remove(nameTranslatorClass);
    }


    /**
	 * Gets status of timings.
	 * 
	 * @return true if operations are to record execution times
	 * @since 1.5
	 */
	public boolean isTimings()
    {
        return timings;
    }


	/**
	 * Sets timings enabled for all database operations. When enabled all operations will record
	 * execution times. Use {@link #logTimings()} to write the timings to the log. Use
	 * {@link #getOperationTime(String)} or {@link #getOperationTimeMap()} to get timings
	 * that have been recorded.
	 * 
	 * @param timings true to enable all operations to record execution timings; false for no
	 * operation timings by default (can be overridden by each {@link SqlOperation}) 
	 * @since 1.5
	 */
    public void setTimings(boolean timings)
    {
        this.timings = timings;
    }


    /**
     * Gets the operation time for a specific timing id.
     * 
     * @param timingId timing id to get
     * @return {@link OperationTime} for timing id or null if timing id is not in map
     * @since 1.5
     */
	public OperationTime getOperationTime(String timingId)
	{
	    return operationTimeMap.get(timingId);
	}
	
	
	/**
	 * Creates an {@link OperationTime} instance with this database total time,
	 * {@link #getTotalOperationTime()} as the parent time. The new instance is
	 * add to operation time map, {@link #getOperationTimeMap()}.
	 * 
	 * @param timingId timing id for created instance
	 * @param description description for created instance
	 * @return new instance of {@link OperationTime} with id and description
	 * @since 1.5
	 */
	public OperationTime createOperationTime(String timingId, String description)
	{
	    OperationTime operationTime = new OperationTime(timingId, totalOperationTime);
	    operationTime.setDescription(description);
	    operationTimeMap.put(timingId, operationTime);
	    return operationTime;
	}
	
	
	/**
	 * @return operation times for all operations that have been executed
	 * @since 1.5
	 */
	public OperationTime getTotalOperationTime()
    {
        return totalOperationTime;
    }


    /**
     * Gets operation times for all operations have been executed. Key to map is
     * timing id.
     * 
     * @return map of timing ids to operation time
     * @since 1.5
     */
	public Map<String, OperationTime> getOperationTimeMap()
    {
        return operationTimeMap;
    }


    /**
     * Logs all times from operation time map, {@link #getOperationTimeMap()} to log. Total
     * operation time, {@link #getTotalOperationTime()} is logged also.
     * @since 1.5
     */
	public void logTimings()
	{
	    if (operationTimeMap.size() > 0)
	    {
    	    log.info("logTimings:");
    	    ArrayList<String> timingIdList = new ArrayList<>(operationTimeMap.keySet());
    	    Collections.sort(timingIdList);
    	    
    	    for (String timingId: timingIdList)
    	    {
    	        OperationTime ot = operationTimeMap.get(timingId);
    	        ot.logTimings(); 
    	    }
    	    
    	    totalOperationTime.logTimings();
	    }
	}


	/**
	 * Defines the translator to use to convert a value to a prepared statement or to convert
	 * a value from a result set. This method is needed only for type translators that are not
	 * defined with {@link ImplicitType} annotation. 
	 * <p>
	 * By default, all primitive types and most subclasses of {@link TypeTranslator} in 
	 * org.sormula.translator.standard package are added during initialization of this class.
	 * Use this method to override default translators or to add a new translator.
	 * <p>
	 * These translators may be overridden for a table by {@link Table#putTypeTranslator(Class, TypeTranslator)}.
	 * 
     * @param typeClass class that translator operates upon
     * @param typeTranslator translator to use for typeClass
	 * @since 1.6 and 2.0
	 */
    public void putTypeTranslator(Class<?> typeClass, TypeTranslator<?> typeTranslator)
    {
        putTypeTranslator(typeClass.getCanonicalName(), typeTranslator);
    }

    
    /**
     * Same as {@link #putTypeTranslator(Class, TypeTranslator)} but uses class name. Useful for adding
     * primitive types like "int", "boolean", "float", etc.
     * 
     * @param typeClassName class name that translator operates upon
     * @param typeTranslator translator to use for typeClass
     * @since 1.6 and 2.0
     */
    public void putTypeTranslator(String typeClassName, TypeTranslator<?> typeTranslator)
    {
        if (log.isDebugEnabled()) log.debug("adding " + typeTranslator + " for " + typeClassName);
        typeTranslatorMap.put(typeClassName, typeTranslator);
    }

    
    /**
     * Gets the translator to use to convert a value to a prepared statement and to convert
     * a value from a result set.
     * 
     * @param typeClass class that translator operates upon
     * @return translator to use for typeClass
     * @since 1.6 and 2.0
     */
    public TypeTranslator<?> getTypeTranslator(Class<?> typeClass)
    {
        if (log.isDebugEnabled()) log.debug("getting typeTranslator for " + 
                typeClass + " is " + getTypeTranslator(typeClass.getCanonicalName()));
        return getTypeTranslator(typeClass.getCanonicalName());
    }
    
    
    /**
     * Same as {@link #getTypeTranslator(Class)} but uses class name.
     * 
     * @param typeClassName class name that translator operates upon
     * @return translator to use for typeClass
     * @since 1.6 and 2.0
     */
    public TypeTranslator<?> getTypeTranslator(String typeClassName)
    {
        return typeTranslatorMap.get(typeClassName);
    }
}
