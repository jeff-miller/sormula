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

import javax.sql.DataSource;

import org.sormula.annotation.Column;
import org.sormula.operation.ModifyOperation;
import org.sormula.operation.SqlOperation;
import org.sormula.translator.NameTranslator;


/**
 * A lightweight class for defining the {@link DataSource} to be used by {@link ActiveRecord}
 * and related classes.
 * 
 * @author Jeff Miller
 * @since 1.7
 */
//don't implement TypeTranslatorMap to keep class simple
public class ActiveDatabase implements Serializable
{
    private static final long serialVersionUID = 1L;
    static ActiveDatabase defaultActiveDatabase; 
    
    DataSource dataSource;
    String schema;
    boolean readOnly;
    boolean timings;
    Class<? extends NameTranslator> nameTranslatorClass;
    
    
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
     * Gets the schema.
     * 
     * @return schema supplied in constructor or empty string if none
     */
    public String getSchema()
    {
        return schema;
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
}
