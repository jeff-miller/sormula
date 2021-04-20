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
 * A {@link SormulaLogger} that logs to {@link System#out}.
 * 
 * @author Jeff Miller
 * @since 4.3
 */
public class ConsoleSormulaLogger implements SormulaLogger
{
    boolean debugEnabled;
    String name;
    
    
    /**
     * Constructs for a logger name. 
     * 
     * @param name name that is displayed in the output
     */
    public ConsoleSormulaLogger(String name)
    {
        this.name = name;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void info(String message)
    {
        log("INFO", message);
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public void debug(String message)
    {
        if (debugEnabled) log("DEBUG", message);
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public void error(String message)
    {
        log("ERROR", message);
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public void error(String message, Throwable throwable)
    {
        error(message);
        throwable.printStackTrace();
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDebugEnabled()
    {
        return debugEnabled;
    }


    /**
     * Sets debug status.
     * 
     * @param debugEnabled true to display debug level
     */
    public void setDebugEnabled(boolean debugEnabled)
    {
        this.debugEnabled = debugEnabled;
    }
    
    
    /**
     * Prints the log using {@link System#out}
     * @param type INFO, WARN, ERROR, DEBUG
     * @param message log message
     */
    protected void log(String type, String message)
    {
        System.out.println(type + " " + name + " " + message); 
    }
}
