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
package org.sormula.operation.cascade.lazy;

import javax.sql.DataSource;

import org.sormula.Database;
import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.annotation.Transient;
import org.sormula.annotation.cascade.SelectCascade;


/**
 * A lazy cascade selector that uses a {@link DataSource} to create the {@link Database} when
 * performing the lazy select. Source rows must be selected with an operation that uses
 * a database created with {@link Database#Database(DataSource)} or 
 * {@link Database#Database(DataSource, String)}.
 * <p>
 * This class is slightly less efficient because a {@link Database} and {@link Table} are created when selector 
 * needs to be used. It is more flexible since it can be used any time even when original connection has been closed.
 * <p>
 * DurableLazySelector is good for scenario's when source row may be serialzed to disk, for example, in
 * web application where source row is stored in web session. 
 * 
 * @author Jeff Miller
 * @since 1.8
 * 
 * @param <R> type of source row class
 */
public class DurableLazySelector<R> extends AbstractLazySelector<R>
{
    private static final long serialVersionUID = 1L;

    @Transient
    DataSource dataSource;
    
    @Transient
    String schema;
    
    
    /**
     * Constructs for use when DurableLazySelector is base class of row that will contain lazy select fields. Typically 
     * the derived class is the one side of a one-to-many relationship or derived class has a reference to the 
     * other class in a one-to-one relationship. {@link #setUseTransaction(boolean)} is true by default.
     */
    public DurableLazySelector()
    {
    }
    
    
    /**
     * Constructs for use as delegate for row that will contain lazy select fields. 
     * {@link #setUseTransaction(boolean)} is true by default.
     * 
     * @param source row that contains fields with {@link SelectCascade#lazy()} is true; typically source
     * is the one side of a one-to-many relationship or source has a reference to the other class in a 
     * one-to-one relationship
     */
    public DurableLazySelector(R source)
    {
        super(source);
    }
    
    
    /**
     * {@link DataSource} and schema are obtained from database parameter.
     * 
     * @param database database that was used to select source row
     * @throws LazyCascadeException if {@link Database#getDataSource()} is null
     */
    @Override
    public void pendingLazySelects(Database database) throws LazyCascadeException
    {
        super.pendingLazySelects(database);
        dataSource = database.getDataSource();
        schema = database.getSchema();
        
        if (dataSource == null)
        {
            throw new LazyCascadeException("data source is null for database; Use Database(DataSource) or Database(DataSource, String)");
        }
    }
    
    
    /**
     * Gets the {@link DataSource} from database used by {@link #pendingLazySelects(Database)}.
     * 
     * @return data source to use for lazy selects
     */
    public DataSource getDataSource()
    {
        return dataSource;
    }


    /**
     * Gets the schema from database used by {@link #pendingLazySelects(Database)}.
     * 
     * @return schema to use for lazy selects
     */
    public String getSchema()
    {
        return schema;
    }


    /**
     * Creates a new instance of database from datasource and schema obtained in 
     * {@link #pendingLazySelects(Database)}.
     * 
     * @return database to use for lazy select
     */
    @Override
    protected Database initDatabase() throws LazyCascadeException
    {
        try
        {
            // create from data source
            return new Database(dataSource, schema);
        }
        catch (SormulaException e)
        {
            throw new LazyCascadeException("error creating database", e);
        }
    }
}
