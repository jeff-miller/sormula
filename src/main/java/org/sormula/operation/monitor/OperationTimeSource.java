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
package org.sormula.operation.monitor;



/**
 * Information about stack location and frequency of occurance for an {@link OperationTime}.
 * 
 * @author Jeff Miller
 * @since 1.5
 */
public class OperationTimeSource 
{
    int id;
    StackTraceElement location;
    int count;
    
    
    /**
     * Constructs for a stack location. The id is initialized from
     * {@link StackTraceElement#hashCode()}. The count is initialized to 1.
     * 
     * @param location location on the JVM stack where timing was initiated
     */
    public OperationTimeSource(StackTraceElement location)
    {
        id = location.hashCode();
        this.location = location;
        count = 1;
    }


    /**
     * Unique id for this stack location.
     * 
     * @return {@link StackTraceElement#hashCode()}
     */
    public int getId()
    {
        return id;
    }


    /**
     * @return location supplied in constructor
     */
    public StackTraceElement getLocation()
    {
        return location;
    }


    /**
     * @return the number of times that timing has occurred
     */
    public int getCount()
    {
        return count;
    }
    
    
    /**
     * Increments the count by 1.
     */
    public void incrementCount()
    {
        ++count;
    }
}
