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

import org.sormula.SormulaException;


/**
 * TODO
 * @author Jeff Miller
 * @since 4.3
 */
public class SormulaLoggerFactory
{
    private static final String factoryClassName = SormulaLoggerFactory.class.getName();
    private static Class<? extends SormulaLogger> loggerClass;
    private static Constructor<? extends SormulaLogger> loggerConstructor;
    
    // default logger is empty logger TODO or ClassLogger for backward compatibility?
    static
    {
        try
        {
            //setLoggerClass(SormulaEmptyLogger.class);
            setLoggerClass(ClassLogger.class);
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
    
    
    public static Class<? extends SormulaLogger> getLoggerClass()
    {
        return loggerClass;
    }


    public static void setLoggerClass(Class<? extends SormulaLogger> loggerClass) throws SormulaException // TODO LoggerException?
    {
        String loggerClassName = loggerClass.getName();
        
        try
        {
            Constructor<? extends SormulaLogger> lc = loggerClass.getConstructor(String.class); //TODO thread safety?
            
            // test constructor here so exceptions are known here and assume no exceptions in getClassLogger()
            lc.newInstance("");
            
            // no errors, keep references
            loggerConstructor = lc;
            SormulaLoggerFactory.loggerClass = loggerClass;
        }
        catch (NoSuchMethodException e)
        {
            throw new SormulaException(loggerClassName + " must have constructor " + loggerClassName + "(String)", e);
        }
        catch (IllegalAccessException e)
        {
            throw new SormulaException(loggerClassName + " is not accessible", e);
        }
        catch (InstantiationException e)
        {
            throw new SormulaException(loggerClassName + " must be concrete not abstract", e);
        }
        catch (Exception e)
        {
            throw new SormulaException(loggerClassName + " constructor error", e);
        }
    }
}
