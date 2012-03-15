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


// TODO allow explicit type annotations that are permited on Database
/**
 * A lightweight class for defining the {@link DataSource} to be used by {@link ActiveRecord}
 * and related classes.
 * 
 * @author Jeff Miller
 * @since 1.7
 */
public class ActiveDatabase implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    DataSource dataSource;
    String schema;
    
    
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
}
