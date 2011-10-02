/* sormula - Simple object relational mapping
 * Copyright (C) 2011 Jeff Miller
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
import org.slf4j.Marker;


/**
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
public class ClassLogger
{
    static final String classLoggerClassName = ClassLogger.class.getName();
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
    Logger log;
    
    
    /**
     * Constructs with logical log name of package name of the caller
     * of this constructor.  For example, com.mycompany.something.MyClass creates
     * new EasyLog, the logical log name used would be com.mycompany.something as
     * if it called LogFactory.getLog("com.mycompany.something").  
     */
    public ClassLogger()
    {
        if (loggerAvailable)
        {
            // search for easylog on stack
            StackTraceElement[] stes =  new Throwable().getStackTrace();
            int e = stes.length - 1;
            for (int i = 0; i < e; ++i) {
                if (stes[i].getClassName().equals(classLoggerClassName)) {
                    // next on stack is the class that created me
                    log = LoggerFactory.getLogger(stes[i + 1].getClassName());
                    break;
                }
            }
        }
    }

    
    // slf4j logger delegate methods

	public void debug(Marker arg0, String arg1, Object arg2, Object arg3) {
		if (loggerAvailable) log.debug(arg0, arg1, arg2, arg3);
	}


	public void debug(Marker arg0, String arg1, Object arg2) {
	    if (loggerAvailable) log.debug(arg0, arg1, arg2);
	}


	public void debug(Marker arg0, String arg1, Object[] arg2) {
	    if (loggerAvailable) log.debug(arg0, arg1, arg2);
	}


	public void debug(Marker arg0, String arg1, Throwable arg2) {
		log.debug(arg0, arg1, arg2);
	}


	public void debug(Marker arg0, String arg1) {
	    if (loggerAvailable) log.debug(arg0, arg1);
	}


	public void debug(String arg0, Object arg1, Object arg2) {
	    if (loggerAvailable) log.debug(arg0, arg1, arg2);
	}


	public void debug(String arg0, Object arg1) {
	    if (loggerAvailable) log.debug(arg0, arg1);
	}


	public void debug(String arg0, Object[] arg1) {
	    if (loggerAvailable) log.debug(arg0, arg1);
	}


	public void debug(String arg0, Throwable arg1) {
	    if (loggerAvailable) log.debug(arg0, arg1);
	}


	public void debug(String arg0) {
	    if (loggerAvailable) log.debug(arg0);
	}


	public void error(Marker arg0, String arg1, Object arg2, Object arg3) {
	    if (loggerAvailable) log.error(arg0, arg1, arg2, arg3);
	}


	public void error(Marker arg0, String arg1, Object arg2) {
	    if (loggerAvailable) log.error(arg0, arg1, arg2);
	}


	public void error(Marker arg0, String arg1, Object[] arg2) {
	    if (loggerAvailable) log.error(arg0, arg1, arg2);
	}


	public void error(Marker arg0, String arg1, Throwable arg2) {
	    if (loggerAvailable) log.error(arg0, arg1, arg2);
	}


	public void error(Marker arg0, String arg1) {
	    if (loggerAvailable) log.error(arg0, arg1);
	}


	public void error(String arg0, Object arg1, Object arg2) {
	    if (loggerAvailable) log.error(arg0, arg1, arg2);
	}


	public void error(String arg0, Object arg1) {
	    if (loggerAvailable) log.error(arg0, arg1);
	}


	public void error(String arg0, Object[] arg1) {
	    if (loggerAvailable) log.error(arg0, arg1);
	}


	public void error(String arg0, Throwable arg1) {
	    if (loggerAvailable) log.error(arg0, arg1);
	}


	public void error(String arg0) {
	    if (loggerAvailable) log.error(arg0);
	}


	public String getName() {
	    if (loggerAvailable) return log.getName();
	    else return "none";
	}


	public void info(Marker arg0, String arg1, Object arg2, Object arg3) {
	    if (loggerAvailable) log.info(arg0, arg1, arg2, arg3);
	}


	public void info(Marker arg0, String arg1, Object arg2) {
	    if (loggerAvailable) log.info(arg0, arg1, arg2);
	}


	public void info(Marker arg0, String arg1, Object[] arg2) {
	    if (loggerAvailable) log.info(arg0, arg1, arg2);
	}


	public void info(Marker arg0, String arg1, Throwable arg2) {
	    if (loggerAvailable) log.info(arg0, arg1, arg2);
	}


	public void info(Marker arg0, String arg1) {
	    if (loggerAvailable) log.info(arg0, arg1);
	}


	public void info(String arg0, Object arg1, Object arg2) {
	    if (loggerAvailable) log.info(arg0, arg1, arg2);
	}


	public void info(String arg0, Object arg1) {
	    if (loggerAvailable) log.info(arg0, arg1);
	}


	public void info(String arg0, Object[] arg1) {
	    if (loggerAvailable) log.info(arg0, arg1);
	}


	public void info(String arg0, Throwable arg1) {
	    if (loggerAvailable) log.info(arg0, arg1);
	}


	public void info(String arg0) {
	    if (loggerAvailable) log.info(arg0);
	}


	public boolean isDebugEnabled() {
		return loggerAvailable && log.isDebugEnabled();
	}


	public boolean isDebugEnabled(Marker arg0) {
		return loggerAvailable && log.isDebugEnabled(arg0);
	}


	public boolean isErrorEnabled() {
		return loggerAvailable && log.isErrorEnabled();
	}


	public boolean isErrorEnabled(Marker arg0) {
		return loggerAvailable && log.isErrorEnabled(arg0);
	}


	public boolean isInfoEnabled() {
		return loggerAvailable && log.isInfoEnabled();
	}


	public boolean isInfoEnabled(Marker arg0) {
		return loggerAvailable && log.isInfoEnabled(arg0);
	}


	public boolean isTraceEnabled() {
		return loggerAvailable && log.isTraceEnabled();
	}


	public boolean isTraceEnabled(Marker arg0) {
		return loggerAvailable && log.isTraceEnabled(arg0);
	}


	public boolean isWarnEnabled() {
		return loggerAvailable && log.isWarnEnabled();
	}


	public boolean isWarnEnabled(Marker arg0) {
		return loggerAvailable && log.isWarnEnabled(arg0);
	}


	public void trace(Marker arg0, String arg1, Object arg2, Object arg3) {
		if (loggerAvailable) log.trace(arg0, arg1, arg2, arg3);
	}


	public void trace(Marker arg0, String arg1, Object arg2) {
		if (loggerAvailable) log.trace(arg0, arg1, arg2);
	}


	public void trace(Marker arg0, String arg1, Object[] arg2) {
		if (loggerAvailable) log.trace(arg0, arg1, arg2);
	}


	public void trace(Marker arg0, String arg1, Throwable arg2) {
		if (loggerAvailable) log.trace(arg0, arg1, arg2);
	}


	public void trace(Marker arg0, String arg1) {
		if (loggerAvailable) log.trace(arg0, arg1);
	}


	public void trace(String arg0, Object arg1, Object arg2) {
		if (loggerAvailable) log.trace(arg0, arg1, arg2);
	}


	public void trace(String arg0, Object arg1) {
		if (loggerAvailable) log.trace(arg0, arg1);
	}


	public void trace(String arg0, Object[] arg1) {
		if (loggerAvailable) log.trace(arg0, arg1);
	}


	public void trace(String arg0, Throwable arg1) {
		if (loggerAvailable) log.trace(arg0, arg1);
	}


	public void trace(String arg0) {
		if (loggerAvailable) log.trace(arg0);
	}


	public void warn(Marker arg0, String arg1, Object arg2, Object arg3) {
		if (loggerAvailable) log.warn(arg0, arg1, arg2, arg3);
	}


	public void warn(Marker arg0, String arg1, Object arg2) {
		if (loggerAvailable) log.warn(arg0, arg1, arg2);
	}


	public void warn(Marker arg0, String arg1, Object[] arg2) {
		if (loggerAvailable) log.warn(arg0, arg1, arg2);
	}


	public void warn(Marker arg0, String arg1, Throwable arg2) {
		if (loggerAvailable) log.warn(arg0, arg1, arg2);
	}


	public void warn(Marker arg0, String arg1) {
		if (loggerAvailable) log.warn(arg0, arg1);
	}


	public void warn(String arg0, Object arg1, Object arg2) {
		if (loggerAvailable) log.warn(arg0, arg1, arg2);
	}


	public void warn(String arg0, Object arg1) {
		if (loggerAvailable) log.warn(arg0, arg1);
	}


	public void warn(String arg0, Object[] arg1) {
		if (loggerAvailable) log.warn(arg0, arg1);
	}


	public void warn(String arg0, Throwable arg1) {
		if (loggerAvailable) log.warn(arg0, arg1);
	}


	public void warn(String arg0) {
		if (loggerAvailable) log.warn(arg0);
	}
}
