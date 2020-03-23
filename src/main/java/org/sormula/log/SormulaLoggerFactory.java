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


/**
 * Factory for supplying logger classes that are used by Sormula classes. This factory
 * decouples Sormula logging from any specific logging library and allows you to use
 * the logging library of your choice or no logging library.
 * <p> 
 * Use {@link #setLoggerClass(Class)} to change the default logger to one of the implementations
 * of {@link SormulaLogger} in org.sormula.log package or any implementation of
 * {@link SormulaLogger}.
 * <p>
 * The default logger class is {@link EmptySormulaLogger}.
 * 
 * @author Jeff Miller
 * @since 4.3
 */
public class SormulaLoggerFactory
{
    private static final String factoryClassName = SormulaLoggerFactory.class.getName();
    private static Class<? extends SormulaLogger> loggerClass;
    private static Constructor<? extends SormulaLogger> loggerConstructor;
    
    static
    {
        try
        {
            setLoggerClass(EmptySormulaLogger.class);
        }
        catch (LogException e)
        {
            // should never occur
            e.printStackTrace();
        }
    }
    
    
    /**
     * Gets a logger with logical log name as package name of the caller
     * of this method. For example, org.sormula.something.SomeClass invokes
     * {@link SormulaLoggerFactory#getClassLogger()} then a SormulaLogger with 
     * the logical log name of org.sormula.something would be returned.
     * 
     * @return logger that implements SormulaLogger interface
     */
    public static SormulaLogger getClassLogger()
    {
        // search for SormulaLoggerFactory on stack
        StackTraceElement[] stes =  new Throwable().getStackTrace();
        int lastElementIndex = stes.length - 1;
        for (int i = 0; i < lastElementIndex; ++i) 
        {
            if (stes[i].getClassName().equals(factoryClassName)) 
            {
                // next on stack is the class that created me
                return getLogger(stes[i + 1].getClassName());
            }
        }
        
        // should never get here, avoid NullPointerException
        return new EmptySormulaLogger("");
    }
    
    
    /**
     * Gets a logger with a specific logical name.
     * 
     * @param name
     * @return logger that implements SormulaLogger interface
     * @since 4.4
     */
    public static SormulaLogger getLogger(String name)
    {
        try
        {
            return loggerConstructor.newInstance(name);
        }
        catch (Exception e)
        {
            // should not occur since #setLoggerClass has tested constructor already
            e.printStackTrace();
        }
        
        // should never get here, avoid NullPointerException
        return new EmptySormulaLogger("");
    }
    
    
    /**
     * Gets the logger class to use in {@link #getClassLogger()}. The default
     * is {@link Slf4jSormulaLogger} for backward compatibility since the original
     * logger used by Sormula was {@link ClassLogger} which is dependent upon SLF4J.
     * 
     * @return class of logger to return with {@link #getClassLogger()}
     */
    public static Class<? extends SormulaLogger> getLoggerClass()
    {
        return loggerClass;
    }
    
    
    /**
     * Loads class with name provided in loggerClassName and invokes {@link #setLoggerClass(Class)}.
     * 
     * @param loggerClassName the fully qualified name of the class that implements {@link SormulaLogger} 
     * @throws LogException if error, see exception message for reason
     */
    public static void setLoggerClass(String loggerClassName) throws LogException
    {
        if (loggerClassName.length() > 0)
        {
            try
            {
                @SuppressWarnings("unchecked")
                Class<? extends SormulaLogger> loggerClass = (Class<? extends SormulaLogger>) Class.forName(loggerClassName);
                SormulaLoggerFactory.setLoggerClass(loggerClass);
            }
            catch (ClassNotFoundException e)
            {
                throw new LogException(loggerClassName + " is not on the classpath", e);
            }
        }
    }


    /**
     * Sets the logger class to be used by Sormula. New instances of the logger class will
     * be created for use by Sormula. The logger class must have a constructor of one parameter 
     * of type String.
     * <p>
     * Setting the logger class determines the logger for Sormula classes that are not already
     * loaded by the class loader. Sormula classes have static reference to {@link SormulaLogger}
     * objects that are initialized when the Sormula class was loaded into memory. Typically
     * {@link #setLoggerClass(Class)} should be used prior to using any Sormula classes. 
     * 
     * @param loggerClass any class that implements {@link SormulaLogger} interface
     * @throws LogException if error, see exception message for reason
     */
    public static void setLoggerClass(Class<? extends SormulaLogger> loggerClass) throws LogException
    {
        String loggerClassName = loggerClass.getName();
        
        try
        {
            Constructor<? extends SormulaLogger> loggerConstructor = loggerClass.getConstructor(String.class);
            
            // test constructor here so exceptions are detected here; avoid exceptions in getClassLogger()
            loggerConstructor.newInstance("");
            
            // no errors, keep references
            // no attempt for thread safety since typically set prior to any use of Sormula 
            // if logger is changed after some Sormula objects are used, not likely to cause thread synchronization problems
            SormulaLoggerFactory.loggerConstructor = loggerConstructor;
            SormulaLoggerFactory.loggerClass = loggerClass;
        }
        catch (NoSuchMethodException e)
        {
            throw new LogException(loggerClassName + " must have constructor " + loggerClassName + "(String)", e);
        }
        catch (IllegalAccessException e)
        {
            throw new LogException(loggerClassName + " is not accessible", e);
        }
        catch (InstantiationException e)
        {
            throw new LogException(loggerClassName + " must be concrete not abstract", e);
        }
        catch (Exception e)
        {
            throw new LogException(loggerClassName + " constructor error", e);
        }
    }
}
