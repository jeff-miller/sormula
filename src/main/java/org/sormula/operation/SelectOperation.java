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
package org.sormula.operation;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;

import org.sormula.Table;
import org.sormula.annotation.Where;


/**
 * SQL select operation returning a collection of rows.
 * 
 * @since 1.0
 * @author Jeff Miller
 * @param <R> class type which contains members for columns of a row in a table
 * @param <C> collection type returned
 */
public abstract class SelectOperation<R, C> extends ScalarSelectOperation<R> implements Iterable<R>
{
    int defaultReadAllSize;
    C selectedRows;
    int fetchSize;
    
    
    /**
     * Constructs standard sql to select by primary key as:
     * SELECT c1, c2, c3, ... FROM table WHERE primary key clause
     * 
     * @param table select from this table
     * @throws OperationException if error
     */
 // TODO deprecate this method and subclass methods? primary key selects only 1 row
    public SelectOperation(Table<R> table) throws OperationException
    {
        super(table);
        setDefaultReadAllSize(20);
    }
    
    
    /**
     * Constructs standard sql to by a where condition:
     * SELECT c1, c2, c3, ... FROM table WHERE...
     * 
     * @param table select from this table
     * @param whereConditionName name of where condition to use ("primaryKey" to select
     * by primary key; empty string to select all rows in table)
     * @throws OperationException if error
     */
    public SelectOperation(Table<R> table, String whereConditionName) throws OperationException
    {
        super(table, whereConditionName);
        
        if (getWhereAnnotation() == null)
        {
            // no where annotation so use default size
            setDefaultReadAllSize(20);
        }
    }
    
    
    /**
     * Creates iterator for results of this operation.
     * 
     * @return {@link SelectIterator}
     * 
     * @since 3.0
     */
    public Iterator<R> iterator()
    {
        return new SelectIterator<R>(this);
    }


    /**
     * Gets the default size to allocate for {@link Collection} C by {@link #createReadAllCollection()}.
     * 
     * @return default size to allocate for collection that contains row objects
     */
    public int getDefaultReadAllSize()
    {
        return defaultReadAllSize;
    }
    
    
    /**
     * Sets default initial capacity for collection that is to contain the selected rows. For
     * large result sets, setting the default capacity may reduce time to build the collection.
     * 
     * @param defaultReadAllSize initial collection capacity; default is 20
     */
    public void setDefaultReadAllSize(int defaultReadAllSize)
    {
        this.defaultReadAllSize = defaultReadAllSize;
    }


    /**
     * {@inheritDoc}
     * Invokes superclass method and then sets the initial capacity {@link #setDefaultReadAllSize(int)} 
     * if there is a where annotation.
     * @since 3.0
     */
    @Override
    public void setWhere(String whereConditionName) throws OperationException
    {
        super.setWhere(whereConditionName);
        
        Where whereAnnotation = getWhereAnnotation(); 
        if (whereAnnotation != null)
        {
            setDefaultReadAllSize(whereAnnotation.selectInitialCapacity());
            setFetchSize(whereAnnotation.fetchSize());
            setMaximumRowsRead(whereAnnotation.maximumRows());
        }
    }


    /**
     * Reads all rows from current result set. For large result sets, invoking {@link #setDefaultReadAllSize(int)}
     * may improve performance.
     * 
     * @return collection of rows from query
     * @throws OperationException if error
     */
    public C readAll() throws OperationException
    {
        selectedRows = createReadAllCollection();
        
        while (true)
        {
            R row = readNext();
            if (row == null) break; 
            add(row);
        }
        
        return selectedRows;
    }
    
    
    /**
     * Gets collection of rows that were selected with {@link #readAll()}.
     * 
     * @return collection of rows; null if {@link #readAll()} has not been invoked
     */
    public C getSelectedRows() 
    {
		return selectedRows;
	}
    
    
    /**
     * Set parameters, executes, reads all rows in result set, closes.
     * 
     * @param parameters query parameters as objects (see {@link #setParameters(Object...)})
     * @return {@link #readAll()}
     * @throws OperationException if error
     * @since 1.4
     */
    public C selectAll(Object... parameters) throws OperationException
    {
        setParameters(parameters);
        execute();
        C results = readAll(); 
        close();
        return results;
    }
    
    
    /**
     * Set parameters, executes, reads all rows in result set, closes.
     * 
     * @param whereParameters query parameters are read from an existing row object 
     * (see {@link #setParameters(Object...)})
     * @return {@link #readAll()}
     * @throws OperationException if error
     * @since 1.4
     */
    public C selectAll(R whereParameters) throws OperationException
    {
        setRowParameters(whereParameters);
        execute();
        C results = readAll(); 
        close();
        return results;
    }


    /**
     * Gets the JDBC fetch size set with {@link #setFetchSize(int)}. The default is zero.
     * 
     * @return result set fetch size
     * @since 3.0
     * @see PreparedStatement#setFetchSize(int)
     */
    public int getFetchSize()
    {
        return fetchSize;
    }


    /**
     * Sets the JDBC fetch size to use for prepared statement. Fetch size is set on
     * prepared statement during {@link #prepare()} with
     * {@link PreparedStatement#setFetchSize(int)}.
     * 
     * @param fetchSize
     * @since 3.0
     * @see PreparedStatement#setFetchSize(int)
     */
    public void setFetchSize(int fetchSize)
    {
        this.fetchSize = fetchSize;
    }


    /**
     * {@inheritDoc}
     * Invokes the superclass prepare and then sets the fetch size on the prepared statement.
     * @since 3.0
     */
	@Override
    protected void prepare() throws OperationException
    {
        super.prepare();
        try
        {
            getPreparedStatement().setFetchSize(fetchSize);
        }
        catch (SQLException e)
        {
            throw new OperationException("error setting fetch size", e);
        }
    }


    /**
     * Implement to create collection to use by {@link #readAll()}.
     * 
     * @return collection to use for {@link #readAll()}
     */
    protected abstract C createReadAllCollection();
    
    
    /**
     * Implement to add row to collection created {@link #createReadAllCollection()}.
     * 
     * @param row row to add
     * @return true if added ok
     * @throws OperationException if error
     */
    protected abstract boolean add(R row) throws OperationException;
}
