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
package org.sormula.operation.filter;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.sormula.log.ClassLogger;
import org.sormula.operation.ScalarSelectOperation;


/**
 * Base class for select cascade fitlers. Using reflection to determine the accept method to invoke
 * based upon the row runtime class type. Simplifies filter implementation by allowing you to
 * create a method for each row type to filter.
 * <p>
 * By default all rows are accepted. For each row runtime type of T, create a method like:<br>
 * <code>public boolean accept(ScalarSelectOperation<?> source, T row, boolean cascadesCompleted)</code><br>
 * Return true to keep row, false to eliminate row from results.
 * 
 * @since 3.1
 * @author Jeff Miller
 */
public abstract class AbstractSelectCascadeFilter implements SelectCascadeFilter<Object>
{
    private static final ClassLogger log = new ClassLogger();
    private static final String parameter1ClassName = ScalarSelectOperation.class.getName();
    private static final String parameter2ClassName = Object.class.getName();
    private static final String parameter3ClassName = boolean.class.getName();
    Map<String, Method> acceptMethods;
    
    
    public AbstractSelectCascadeFilter()
    {
        // build map of row type name (of 2nd parameter) to method
        acceptMethods = new HashMap<String, Method>();

        // for all methods in this class
        for (Method m : getClass().getMethods())
        {
            // look for methods signature of "boolean accept(...)"
            if (m.getName().equals("accept") && m.getReturnType().getName().equals("boolean"))
            {
                // use only methods with 3 parameters
                Class<?>[] parameterTypes = m.getParameterTypes();
                if (parameterTypes.length == 3)
                {
                    // use only methods with parameters (ScalarSelectOperation, T, boolean)
                    String key = parameterTypes[1].getName();
                    if (parameterTypes[0].getName().equals(parameter1ClassName) &&
                        !key.equals(parameter2ClassName) && // don't map java.lang.Object 
                        parameterTypes[2].getName().equals(parameter3ClassName))
                    {
                        // map 2nd parameter type to method
                        if (log.isDebugEnabled()) log.debug(m + " filter method for " + key);
                        acceptMethods.put(key, m);
                    }
                }
            }
        }
    }


    /**
     * @return Object.class to indicate that all row classes are filtered by this filter
     */
    public Class<Object> getRowClass()
    {
        return Object.class; // use filter for all types
    }

    
    /**
     * Invokes subclass accept method based upon row runtime class type. Since {@link #getRowClass()}
     * returns Object.class to indicate all rows are filtered, then this method is invoked by
     * {@link ScalarSelectOperation} to test for filtering. 
     * 
     *  @return value returned by subclass accept method; true if no subclass method exists for
     *  row runtime class 
     */
    public boolean accept(ScalarSelectOperation<Object> source, Object row, boolean cascadesCompleted)
    {
        boolean result = true; // default for missing method is true
        Method acceptMethod = acceptMethods.get(row.getClass().getName());
        
        if (acceptMethod != null)
        {
            if (log.isDebugEnabled()) log.debug("invoke " + acceptMethod + " for " + row);
            try
            {
                result = (Boolean)acceptMethod.invoke(this, source, row, cascadesCompleted);
            } 
            catch (Exception e)
            {
                result = false;
                log.error("error invoking accept method", e);
            }
        }
        else if (log.isDebugEnabled())
        {
            log.debug("no filter method for " + row);
        }
        
        return result;
    }
}
