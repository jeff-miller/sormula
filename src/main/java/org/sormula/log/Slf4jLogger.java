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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * TODO revise
 * A delegate for SLF4J logger that uses the import org.slf4j.LoggerFactory 
 * to create a logger based upon the class name that created ClassLogger. 
 * Use no-argument constructor to create a logger for your class. To use this class,
 * create an new instance and then invoke the log methods.
 * <pre>
 * package com.mycompany.something;
 * 
 * public class MyClass 
 * {
 *     final static ClassLogger log = new ClassLogger();
 * .
 * .
 * .
 *     public void SomeMethod()
 *     {
 *         log.info("hello, world");
 *     }
 * 
 * }     
 * </pre>
 * 
 * Log messages from MyClass will be logged with logical log name of
 * com.mycompany.something.MyClass
 * <p>
 * If no SLF4J jars are on the classpath, then no exceptions are thrown
 * and no logging will occur.
 * 
 * @since 1.0
 * @see Logger
 * @author Jeff Miller
 */
public class Slf4jLogger implements SormulaLogger
{
    static final String classLoggerClassName = Slf4jLogger.class.getName();
    static boolean loggerAvailable = false;
    static
    {
        try
        {
            LoggerFactory.getLogger("");
            loggerAvailable = true;
        }
        catch (NoClassDefFoundError e)
        {
            // assume no slf4j logging desired since jars are not on classpath
            System.out.println("no sormula logging since slf4j jars are not on classpath");
        }
    }
    Logger logger;
    
    
    /**
     * TODO
     * @param className
     */
    public Slf4jLogger(String className)
    {
        if (loggerAvailable)
        {
            logger = LoggerFactory.getLogger(className);
        }
    }


    @Override
    public void info(String message)
    {
        logger.info(message);
    }

    
    @Override
    public void debug(String message)
    {
        logger.debug(message);
    }

    
    @Override
    public void error(String message)
    {
        logger.error(message);
    }

    
    @Override
    public void error(String message, Throwable throwable)
    {
        logger.error(message, throwable);
    }

    
    @Override
    public boolean isDebugEnabled()
    {
        return logger.isDebugEnabled();
    }
}
