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
package org.sormula;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.sormula.annotation.Column;
import org.sormula.log.ClassLogger;
import org.sormula.operation.SqlOperation;
import org.sormula.operation.monitor.OperationTime;
import org.sormula.translator.NameTranslator;


/**
 * Source of {@linkplain Table} objects for reading/writing from/to database. For single threaded use 
 * only. Construct new instances for each transaction and/or thread.
 * <p>
 * Example - Construct database from JDBC connection:
 * <blockquote><pre>
 * Connection connection = ... // jdbc connection
 * Database database = new Database(connection);
 * Table&lt;MyRow&gt; table = database.getTable(MyRow.class);
 * table.selectAll();
 * <pre></blockquote>
 * 
 * @since 1.0
 * @author Jeff Miller
 */
public class Database
{
    private static final ClassLogger log = new ClassLogger();
    Connection connection;
    String schema;
    Map<String, Table<?>> tableMap; // key is row class canonical name
    Transaction transaction;
    Class<? extends NameTranslator> nameTranslatorClass;
    Map<String, OperationTime> operationTimeMap;
    OperationTime totalOperationTime;
    boolean timings;
    
    
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
     * @param schema name of schema to be prefixed to table name in all table names in sql statements
     */
    public Database(Connection connection, String schema)
    {
        this.connection = connection;
        this.schema = schema;
        tableMap = new HashMap<String, Table<?>>();
        transaction = new Transaction(connection);
        operationTimeMap = new HashMap<String, OperationTime>();
        totalOperationTime = new OperationTime("Database totals");
        totalOperationTime.setDescription("All operations for database");
    }

    
    /**
     * Gets transaction for connection. A {@link Transaction} is not required. {@link Transaction}
     * is provided for basic JDBC transaction support if desired. Since {@link Database} is
     * single threaded, only one {@link Transaction} object exists per instance of {@link Database}.
     * 
     * @return transaction for this database
     */
    public Transaction getTransaction() 
    {
		return transaction;
	}


	/**
     * Dereferences all objects used. Does not close connection. This method is not required. Use
     * to accelerate memory clean up.
     */
    public void close()
    {
        connection = null;
        schema = null;
        tableMap = null;
    }
    

    /**
     * Gets connection to use for sql operations. Used by {@linkplain SqlOperation} subclasses
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
     * Gets table object for reading/writing row objects of type R from/to
     * table. Table objects are cahced in map by canonical class name. If
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
        Table<?> table = tableMap.get(rowClass.getCanonicalName());
        
        if (table == null)
        {
            // default
            table = new Table<R>(this, rowClass);
            addTable(table);
        }

        @SuppressWarnings("unchecked") // tableMap contains different types but always same type for rowClass name
        Table<R> t = (Table<R>)table;
        return t;
    }
    
    
    /**
     * Adds a table object to cache to be used for row objects of type
     * {@linkplain Table#getClass()}. Use this method to save table in cache. This method provides
     * a way to ensure that a custom subclass of {@link Table} is returned from {@link #getTable(Class)}
     * for the table row class. 
     * 
     * @param table table object to add to cache (table row class cannonical name is key)
     */
    public void addTable(Table<?> table)
    {
        tableMap.put(table.getRowTranslator().getRowClass().getCanonicalName(), table);
    }


    /**
     * Gets the default name translator class for tables when none is specified.
     * 
     * @return default name translator class
     */
	public Class<? extends NameTranslator> getNameTranslatorClass() 
	{
		return nameTranslatorClass;
	}


	/**
	 * Sets the name translator class to use for a table if no translator is specified by
	 * {@link Column#translator()}. A new instance is created for each table.
	 * 
	 * @param nameTranslatorClass default name translator class
	 */
	public void setNameTranslatorClass(Class<? extends NameTranslator> nameTranslatorClass) 
	{
		this.nameTranslatorClass = nameTranslatorClass;
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
	 * operation timings by default (can be overriden by each {@link SqlOperation}) 
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
    	    ArrayList<String> timingIdList = new ArrayList<String>(operationTimeMap.keySet());
    	    Collections.sort(timingIdList);
    	    
    	    for (String timingId: timingIdList)
    	    {
    	        OperationTime ot = operationTimeMap.get(timingId);
    	        ot.logTimings(); 
    	    }
    	    
    	    totalOperationTime.logTimings();
	    }
	}
}
