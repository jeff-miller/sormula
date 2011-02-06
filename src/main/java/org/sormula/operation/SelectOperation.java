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
package org.sormula.operation;

import org.sormula.Table;
import org.sormula.translator.RowTranslator;


/**
 * SQL select operation returning a collection of rows.
 * 
 * @since 1.0
 * @author Jeff Miller
 * @param <R> class type which contains members for columns of a row in a table
 * @param <C> collection type returned
 */
public abstract class SelectOperation<R, C> extends ScalarSelectOperation<R>
{
    int defaultReadAllSize = 20;
    C selectedRows;
    
    
    /**
     * Constructs for standard sql select statement as:
     * SELECT c1, c2, c3, ... FROM <table>
     * 
     * @param table select from this table
     * @throws OperationException if error
     */
    public SelectOperation(Table<R> table) throws OperationException
    {
        super(table);
    }
    
    
    /**
     * @return default size to allocate for Collection to hold row objects created by {@link #createReadAllCollection()}
     */
    public int getDefaultReadAllSize()
    {
        return defaultReadAllSize;
    }
    
    
    /**
     * Sets default initial capacity for collection that is to contain the selected rows. For
     * large result sets, setting the default capcity may reduce time to build the collection.
     * 
     * @param defaultReadAllSize initial collection capacity; default is 20
     */
    public void setDefaultReadAllSize(int defaultReadAllSize)
    {
        this.defaultReadAllSize = defaultReadAllSize;
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
        
        try
        {
            RowTranslator<R> rowTranslator = table.getRowTranslator();
            
            while (resultSet.next())
            {
                R row = rowTranslator.newInstance();
                preReadCascade(row);
                preRead(row);
                rowTranslator.read(resultSet, 1, row);
                postRead(row);
                postReadCascade(row);
                add(row);
            }
        }
        catch (Exception e)
        {
            throw new OperationException("readAll() error", e);
        }
        
        return selectedRows;
    }
    
    
    /**
     * @return collection of rows that have been selected; null if {@link #readAll()} has not been
     * invoked
     */
    public C getSelectedRows() 
    {
		return selectedRows;
	}


	/**
     * Implement to create collection to use by readAll.
     * 
     * @return collection to use for {@link #readAll()}
     */
    protected abstract C createReadAllCollection();
    
    
    /**
     * Implement to add row to readAll results.
     * 
     * @param row row to add
     * @return true if added ok
     * @throws OperationException if error
     */
    protected abstract boolean add(R row) throws OperationException;
}
