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

import java.util.Iterator;

import org.sormula.log.ClassLogger;


/**
 * Iterator for a {@link SelectOperation}. Returned by {@link SelectOperation#iterator()} or
 * may be instantiated and used stand-alone. 
 * 
 * @since 3.0
 * @author Jeff Miller
 * @param <R> class type for row 
 */
public class SelectIterator<R> implements Iterator<R>
{
    private static final ClassLogger log = new ClassLogger();
    SelectOperation<R, ?> selectOperation;
    R next;
    
    
    /**
     * Constructs for a select operation. All parameters must be set on operation prior to
     * the first use of {@link #hasNext()}. If {@link SelectOperation#execute()} has not
     * be invoked, then it will be upon first invocation of {@link #hasNext()}.
     * 
     * @param selectOperation operation to iterate over
     */
    public SelectIterator(SelectOperation<R, ?> selectOperation)
    {
        this.selectOperation = selectOperation;
    }
    
    
    /**
     * Tests if there is a row available for {@link #next()}. This method will invoke
     * {@link SelectOperation#readNext()} to look ahead to find out if another row is 
     * available if it does not already know about next row.
     * 
     * @return true if at least one more row is availble through {@link #next()}; false if not
     */
    public boolean hasNext() 
    {
        try
        {
            if (!selectOperation.isExecuted())
            {
                // operation has not executed query, do it now
                selectOperation.execute();
            }
            
            if (next == null)
            {
                // no next row, get it
                next = selectOperation.readNext();
            }
        }
        catch (OperationException e)
        {
            // hasNext does not use throws in signature, log error
            log.error("hasNext error", e);
        }

        return next != null;
    }


    /**
     * Reads the next row from the select operation or returns the row found
     * in most recent use of {@link #hasNext()}. This method advances cursor of reads
     * so that repeated invocations will iterate through all rows.
     * 
     * @return next row or null if no more rows
     */
    public R next()
    {
        if (next == null) hasNext();
        R temp = next;
        next = null;
        return temp;
    }


    /**
     * Throws {@link UnsupportedOperationException}.
     */
    public void remove()
    {
        throw new UnsupportedOperationException();
    }
}
