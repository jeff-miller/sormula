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


/**
 * Sormula logs to this interface. Implementing this interface enables use of any logger 
 * API without dependency upon a logging facade library. 
 * 
 * @author Jeff Miller
 * @since 4.3
 * 
 * @see SormulaLoggerFactory#setLoggerClass(Class)
 */
public interface SormulaLogger
{
    /**
     * Logs informational message.
     * 
     * @param message message to log
     */
    void info(String message);

    
    /**
     * Logs debug level message.
     * 
     * @param message message to log
     */
    void debug(String message);
    
    
    /**
     * Logs error message.
     * 
     * @param message message to log
     */
    void error(String message);
    
    
    /**
     * Logs error message and cause.
     * 
     * @param message message to log
     * @param throwable error/exception cause
     */
    void error(String message, Throwable throwable);
    
    
    /**
     * Reports when messages are logged with {@link #debug(String)}.
     * 
     * @return true to log messages with {@link #debug(String)}; false nothing will be logged with {@link #debug(String)}
     */
    default boolean isDebugEnabled()
    {
        return false;
    }
}
