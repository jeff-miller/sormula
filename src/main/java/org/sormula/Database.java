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
import java.util.HashMap;
import java.util.Map;

import org.sormula.annotation.Column;
import org.sormula.operation.SqlOperation;
import org.sormula.translator.NameTranslator;


/**
 * Source of {@linkplain Table} objects for reading/writing from/to database. For single threaded use 
 * only. Construct new instances for each transaction and/or thread.
 * <p>
 * Example - Construct database from jdbc connection:
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
    Connection connection;
    String schema;
    Map<String, Table<?>> tableMap; // key is row class canonical name
    Transaction transaction;
    Class<? extends NameTranslator> nameTranslatorClass;
    
    
    /**
     * Constructs for no schema. All sql table names will not include a schema prefix.
     * 
     * @param connection jdbc connection
     */
    public Database(Connection connection)
    {
        this(connection, "");
    }
    
    
    /**
     * Constructs for schema. All sql table names will be prefixed with schema name
     * in form of "schema.table".
     * 
     * @param connection jdbc connection
     * @param schema name of schema to be prefixed to table name in all table names in sql statements
     */
    public Database(Connection connection, String schema)
    {
        this.connection = connection;
        this.schema = schema;
        tableMap = new HashMap<String, Table<?>>();
        transaction = new Transaction(connection);
    }

    
    /**
     * Gets transaction for connection. A {@link Transaction} is not required. {@link Transaction}
     * is provided for basic jdbc transaction support if desired. Since {@link Database} is
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
     * @return jdbc connection
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
}
