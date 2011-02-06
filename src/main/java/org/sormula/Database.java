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

import org.sormula.operation.SqlOperation;


/**
 * Source of {@linkplain Table} objects for reading/writing from/to database. For single threaded use 
 * only. Construct new instances for each transaction and/or thread.
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
     * TODO do not Closes connection and frees up all resources created by this class.
     */
    public void close()
    {
        /*
        try
        {
            connection.close();
        }
        catch (SQLException e)
        {
            log.error("error closing connection", e);
        }
        */
        
        connection = null;
        schema = null;
        tableMap = null;
    }
    

    /**
     * Gets connection to use for sql operation. Used by {@linkplain SqlOperation} subclasses
     * to obtain the connection to use for the operation methods.
     * 
     * @return jdbc connection
     */
    public Connection getConnection()
    {
        return connection;
    }


    /**
     * @return schema name supplied in constructor; empty string for no schema
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

        @SuppressWarnings("unchecked") // tableMap contains different types but always same type for rowClass
        Table<R> t = (Table<R>)table;
        return t;
    }
    
    
    /**
     * Override default table object. Adds a table object to cache to used for row objects of type
     * {@linkplain Table#getClass()}. Use this method to save table in cache. This method is optional
     * for tables created outside of this class. 
     * 
     * @param table table object to add to cache ({@linkplain Table#getClass()} cannonical name is key
     */
    public void addTable(Table<?> table)
    {
        tableMap.put(table.getRowTranslator().getRowClass().getCanonicalName(), table);
    }
}
