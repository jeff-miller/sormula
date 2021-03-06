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
package org.sormula.active;

import java.io.Serializable;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.sormula.Database;
import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.active.builder.ActiveDatabaseBuilder;
import org.sormula.annotation.Column;
import org.sormula.operation.ModifyOperation;
import org.sormula.operation.SqlOperation;
import org.sormula.translator.NameTranslator;


/**
 * A lightweight class for defining the {@link DataSource} to be used by {@link ActiveRecord}
 * and related classes. This class may be serialized but the active transaction will not
 * be serialized.
 * 
 * @author Jeff Miller
 * @since 1.7 and 2.1
 */
//don't implement TypeTranslatorMap to keep class simple
public class ActiveDatabase implements Serializable
{
    private static final long serialVersionUID = 1L;
    static ActiveDatabase defaultActiveDatabase; 

    String dataSourceName;
    DataSource dataSource;
    String schema;
    boolean readOnly;
    boolean timings;
    boolean autoGeneratedKeys;
    List<Class<? extends NameTranslator>> nameTranslatorClasses;
    
    // note: must be transient since can't serialize jdbc connection
    transient ActiveTransaction activeTransaction;
    
    
    /**
     * Creates builder for a data source.
     * 
     * @param dataSource data source
     * @return {@link ActiveDatabaseBuilder}
     * @since 4.4
     * @see ActiveDatabase#ActiveDatabase(DataSource)
     */
    public static ActiveDatabaseBuilder builder(DataSource dataSource)
    {
        return new ActiveDatabaseBuilder(dataSource);
    }
    
    
    /**
     * Creates builder for a data source name.
     * 
     * @param dataSourceName data source name
     * @return {@link ActiveDatabaseBuilder}
     * @since 4.4
     * @see ActiveDatabase#ActiveDatabase(String)
     */
    public static ActiveDatabaseBuilder builder(String dataSourceName)
    {
        return new ActiveDatabaseBuilder(dataSourceName);
    }


    /**
     * Gets the active database to use when none is specified. Only one default active database
     * may be used per class loader. {@link ActiveTable#ActiveTable(Class)} and an {@link ActiveRecord}
     * that is not attached to a database will use the default active database.
     * 
     * @return default active database or null if {@link #setDefault(ActiveDatabase)} has not been
     * used to set the default
     */
    public static ActiveDatabase getDefault()
    {
        return defaultActiveDatabase;
    }


    /**
     * Sets the default active database. See {@link #getDefault()} for explanation.
     * 
     * @param activeDatabase default active databases or null to remove current default
     */
    public static void setDefault(ActiveDatabase activeDatabase)
    {
        ActiveDatabase.defaultActiveDatabase = activeDatabase;
    }


    /**
     * Constructs for a data source name and empty schema.     
     * <p>
     * {@link DataSource} will be obtained from JNDI look up of dataSourceName. Typically web containers
     * require data source to be configured with a name like "someds" but the full path to the
     * data source contains an implied context of "java:comp/env". So the constructor parameter, 
     * dataSourceName, would be "java:comp/env/someds".
     * 
     * @param dataSourceName name to use in JNDI look up of data source
     * @throws ActiveException if error looking up data source
     * @since 1.9 and 2.3
     */
    public ActiveDatabase(String dataSourceName) throws ActiveException
    {
        this(dataSourceName, "");
    }
    
    
    /**
     * Constructs for a data source name and schema.
     * <p>
     * {@link DataSource} will be obtained from JNDI look up of dataSourceName. Typically web containers
     * require data source to be configured with a name like "someds" but the full path to the
     * data source contains an implied context of "java:comp/env". So the constructor parameter, 
     * dataSourceName, would be "java:comp/env/someds".
     * 
     * @param dataSourceName name to use in JNDI look up of data source
     * @param schema schema prefix for table names in sql
     * @throws ActiveException if error looking up data source
     * @since 1.9 and 2.3
     */
    public ActiveDatabase(String dataSourceName, String schema) throws ActiveException
    {
        this.dataSourceName = dataSourceName;
        this.schema = schema;
        nameTranslatorClasses = new ArrayList<>(4);
        autoGeneratedKeys = true;
        
        try
        {
            Context context = new InitialContext();
            dataSource = (DataSource)context.lookup(dataSourceName);
            if (dataSource == null) throw new ActiveException("no data source found for dataSourceName=" + dataSourceName);
        }
        catch (NamingException e)
        {
            throw new ActiveException("erroring getting data source", e);
        }
    }


    /**
     * Constructs for a data source and empty schema.
     * 
     * @param dataSource data source for active records
     */
    public ActiveDatabase(DataSource dataSource)
    {
        this(dataSource, "");
    }
    
    
    /**
     * Constructs for a data source and schema.
     * 
     * @param dataSource data source for active records
     * @param schema schema prefix for table names in sql
     */
    public ActiveDatabase(DataSource dataSource, String schema)
    {
        this.dataSource = dataSource;
        this.schema = schema;
        nameTranslatorClasses = new ArrayList<>(4);
        autoGeneratedKeys = true;
    }


    /**
     * Gets the data source.
     * 
     * @return data source supplied in constructor
     */
    public DataSource getDataSource()
    {
        return dataSource;
    }


    /**
     * Gets the data source name.
     * 
     * @return data source name supplied in constructor
     * @since 4.4
     */
    public String getDataSourceName() 
    {
        return dataSourceName;
    }


    /**
     * Gets the schema.
     * 
     * @return schema supplied in constructor or empty string if none
     */
    public String getSchema()
    {
        return schema;
    }
    
    
    /**
     * Gets the transaction in use by this active database.
     * 
     * @return transaction or null if no transaction is in use
     * @since 1.7.1 and 2.1.1
     */
    public ActiveTransaction getActiveTransaction()
    {
        return activeTransaction;
    }

    
    /**
     * Sets the transaction in use. Set by {@link ActiveTransaction#begin()} when a transaction starts. Set
     * to null by {@link ActiveTransaction#close()} and {@link ActiveTransaction#rollback()} when the transaction
     * is over.
     * <p>
     * Only one active transaction may be in use at one time by the same instance of ActiveDatabase. Create an
     * instance of ActiveDatabse for each possible active transaction that may be in use at the same time.
     * 
     * @param activeTransaction active transaction to use
     * @throws ActiveException if a transaction is already in use
     * @since 1.7.1 and 2.1.1
     */
    public void setActiveTransaction(ActiveTransaction activeTransaction) throws ActiveException
    {
        if (activeTransaction != null && this.activeTransaction != null) 
            throw new ActiveException("an active transaction has already begun for this active database");
        this.activeTransaction = activeTransaction;
    }
    
    
    /**
     * Gets the most recent value of {@link #setAutoGeneratedKeys(boolean)}. The default is true.
     * 
     * @return true if database is to generate keys for identity columns 
     * @since 4.1
     */
    public boolean isAutoGeneratedKeys() 
    {
        return autoGeneratedKeys;
    }


    /**
     * Sets the default way identity columns are processed for newly created {@link Table} objects. Table
     * objects created will have {@link Table#setAutoGeneratedKeys(boolean)} set to 
     * {@link Database#isAutoGeneratedKeys()}. Setting to false is useful when a column
     * is defined as an identity column but the key will be manually created by the application.
     * <p>
     * Changing this value will have no affect on {@link Table} objects that have already been
     * created.
     * 
     * @param autoGeneratedKeys true if {@link Table} objects created for this database should 
     * assume that database will automatically generate key values for
     * fields defined as identity ({@link Column#identity()} is true)
     * 
     * @since 4.1
     * @see Column#identity()
     * @see Table#isAutoGeneratedKeys()
     * @see Statement#getGeneratedKeys()
     */
    public void setAutoGeneratedKeys(boolean autoGeneratedKeys) 
    {
        this.autoGeneratedKeys = autoGeneratedKeys;
    }


    /**
     * Gets read-only indicator.
     * 
     * @return true if modify operations are not permitted
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
     * @see SqlOperation#setReadOnly(boolean)
     */
    public void setReadOnly(boolean readOnly)
    {
        this.readOnly = readOnly;
    }


    /**
     * Gets the default name translators to be used when none are defined for the table.
     * 
     * @return list of name translator classes; empty list if none 
     * @since 1.8 and 2.2
     * @see Database#getNameTranslatorClasses()
     * @see Table#translateName(String)
     */
    public List<Class<? extends NameTranslator>> getNameTranslatorClasses()
    {
        return nameTranslatorClasses;
    }


    /**
     * Adds a default name translator to use when none are defined for the table. Name
     * translators are applied in order they are added.
     *  
     * @param nameTranslatorClass class that will be used to translate table/column names
     * @since 1.8 and 2.2
     * @see Database#addNameTranslatorClass(Class)
     * @see Table#translateName(String)
     */
    public void addNameTranslatorClass(Class<? extends NameTranslator> nameTranslatorClass)
    {
        nameTranslatorClasses.add(nameTranslatorClass);
    }


    /**
     * Removes a default name translator.
     * 
     * @param nameTranslatorClass class to remove
     * @since 1.8 and 2.2
     * @see Database#removeNameTranslatorClass(Class)
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
     */
    public boolean isTimings()
    {
        return timings;
    }


    /**
     * Sets timings enabled for all database operations. When enabled all operations will write
     * execution times to log upon commit.
     * 
     * @param timings true to enable all operations to record execution timings; false for no
     * operation timings by default 
     */
    public void setTimings(boolean timings)
    {
        this.timings = timings;
    }
    
    
    /**
     * Writes all uncommitted cache to the database and removes all cached records. Delegates
     * to {@link Database#flush()}.
     * 
     * @since 3.0
     */
    public void flush()
    {
        if (activeTransaction != null)
        {
            try
            {
                activeTransaction.getOperationTransaction().getOperationDatabase().flush();
            }
            catch (SormulaException e)
            {
                throw new ActiveException("flush error", e);
            }
        }
    }
}
