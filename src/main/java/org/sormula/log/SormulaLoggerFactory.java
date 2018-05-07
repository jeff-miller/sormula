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
package org.sormula.log;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.sormula.SormulaException;


/**
 * TODO
 * @author Jeff Miller
 * @since 4.3
 */
public class SormulaLoggerFactory
{
    private static final String factoryClassName = SormulaLoggerFactory.class.getName();
    private static String loggerClassName;
    private static Constructor<? extends SormulaLogger> loggerConstructor;
    
    // default logger is empty logger
    static
    {
        try
        {
            setLoggerClassName(SormulaEmptyLogger.class.getName());
        }
        catch (SormulaException e)
        {
            // should never occur
            e.printStackTrace();
        }
    }
    
    
    public static SormulaLogger getClassLogger()
    {
        System.out.println("factoryClassName=");
        System.out.println(factoryClassName);
        // search for SormulaLoggerFactory on stack
        StackTraceElement[] stes =  new Throwable().getStackTrace();
        int lastElementIndex = stes.length - 1;
        for (int i = 0; i < lastElementIndex; ++i) 
        {
            System.out.println(stes[i].getClassName());
            if (stes[i].getClassName().equals(factoryClassName)) 
            {
                // next on stack is the class that created me
                try
                {
                    return loggerConstructor.newInstance(stes[i + 1].getClassName());
                }
                catch (Exception e)
                {
                    // should not occur, don't use checked exception
                    // TODO or convert to runtime exception?
                    e.printStackTrace();
                    break;
                }
            }
        }
        
        // should never get here, avoid NullPointerException
        return new SormulaEmptyLogger("");
    }
    
    
    public static String getLoggerClassName()
    {
        return loggerClassName;
    }


    // TODO change parameter from String to Class<? extends SormulaLogger> ?
    public static void setLoggerClassName(String loggerClassName) throws SormulaException // TODO LoggerException?
    {
        try
        {
            Class<?> loggerClass = Class.forName(loggerClassName);
            
            if (SormulaLogger.class.isAssignableFrom(loggerClass))
            {
                @SuppressWarnings("unchecked")
                Class<? extends SormulaLogger> sormulaLoggerClass = (Class<? extends SormulaLogger>)loggerClass;
                loggerConstructor = sormulaLoggerClass.getConstructor(String.class); //TODO thread safety?
                
                // test constructor here, will not throw exceptions in getClassLogger()
                loggerConstructor.newInstance("");
            }
        }
        catch (ClassNotFoundException e)
        {
            throw new SormulaException(loggerClassName + " class not in classpath", e);
        }
        catch (NoSuchMethodException e)
        {
            throw new SormulaException(loggerClassName + " must have constructor " + loggerClassName + "(String)", e);
        }
        catch (InvocationTargetException e)
        {
            throw new SormulaException(loggerClassName + " constructor error", e);
        }
        catch (IllegalAccessException e)
        {
            throw new SormulaException(loggerClassName + " is not accessible", e);
        }
        catch (InstantiationException e)
        {
            throw new SormulaException(loggerClassName + " must be concrete not abstract", e);
        }
    }
}
