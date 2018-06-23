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
 * A {@link SormulaLogger} that logs using the SLF4J logging facade library.
 * <p>
 * If no SLF4J jars are on the classpath, then no exceptions are thrown
 * and no logging will occur.
 * 
 * @since 4.3
 * @author Jeff Miller
 */
public class Slf4jSormulaLogger implements SormulaLogger
{
    static final String classLoggerClassName = Slf4jSormulaLogger.class.getName();
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
    
    
    public Slf4jSormulaLogger(String name)
    {
        if (loggerAvailable)
        {
            logger = LoggerFactory.getLogger(name);
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
