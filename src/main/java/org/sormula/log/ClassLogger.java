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
 *  
 * @since 1.0
 * @see Logger
 * @author Jeff Miller
 */
public class ClassLogger implements Logger
{
    static final String classLoggerClassName = ClassLogger.class.getName();
    Logger log;
    
    
    /**
     * Constructs with logical log name of package name of the caller
     * of this constructor.  For example, com.mycompany.something.MyClass creates
     * new EasyLog, the logical log name used would be com.mycompany.something as
     * if it called LogFactory.getLog("com.mycompany.something").  
     */
    public ClassLogger()
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
        
        if (log == null) {
            // not found, use generic
            log = LoggerFactory.getLogger("unknown");
        }        
    }

    
    // slf4j logger delegate methods

	public void debug(Marker arg0, String arg1, Object arg2, Object arg3) {
		log.debug(arg0, arg1, arg2, arg3);
	}


	public void debug(Marker arg0, String arg1, Object arg2) {
		log.debug(arg0, arg1, arg2);
	}


	public void debug(Marker arg0, String arg1, Object[] arg2) {
		log.debug(arg0, arg1, arg2);
	}


	public void debug(Marker arg0, String arg1, Throwable arg2) {
		log.debug(arg0, arg1, arg2);
	}


	public void debug(Marker arg0, String arg1) {
		log.debug(arg0, arg1);
	}


	public void debug(String arg0, Object arg1, Object arg2) {
		log.debug(arg0, arg1, arg2);
	}


	public void debug(String arg0, Object arg1) {
		log.debug(arg0, arg1);
	}


	public void debug(String arg0, Object[] arg1) {
		log.debug(arg0, arg1);
	}


	public void debug(String arg0, Throwable arg1) {
		log.debug(arg0, arg1);
	}


	public void debug(String arg0) {
		log.debug(arg0);
	}


	public void error(Marker arg0, String arg1, Object arg2, Object arg3) {
		log.error(arg0, arg1, arg2, arg3);
	}


	public void error(Marker arg0, String arg1, Object arg2) {
		log.error(arg0, arg1, arg2);
	}


	public void error(Marker arg0, String arg1, Object[] arg2) {
		log.error(arg0, arg1, arg2);
	}


	public void error(Marker arg0, String arg1, Throwable arg2) {
		log.error(arg0, arg1, arg2);
	}


	public void error(Marker arg0, String arg1) {
		log.error(arg0, arg1);
	}


	public void error(String arg0, Object arg1, Object arg2) {
		log.error(arg0, arg1, arg2);
	}


	public void error(String arg0, Object arg1) {
		log.error(arg0, arg1);
	}


	public void error(String arg0, Object[] arg1) {
		log.error(arg0, arg1);
	}


	public void error(String arg0, Throwable arg1) {
		log.error(arg0, arg1);
	}


	public void error(String arg0) {
		log.error(arg0);
	}


	public String getName() {
		return log.getName();
	}


	public void info(Marker arg0, String arg1, Object arg2, Object arg3) {
		log.info(arg0, arg1, arg2, arg3);
	}


	public void info(Marker arg0, String arg1, Object arg2) {
		log.info(arg0, arg1, arg2);
	}


	public void info(Marker arg0, String arg1, Object[] arg2) {
		log.info(arg0, arg1, arg2);
	}


	public void info(Marker arg0, String arg1, Throwable arg2) {
		log.info(arg0, arg1, arg2);
	}


	public void info(Marker arg0, String arg1) {
		log.info(arg0, arg1);
	}


	public void info(String arg0, Object arg1, Object arg2) {
		log.info(arg0, arg1, arg2);
	}


	public void info(String arg0, Object arg1) {
		log.info(arg0, arg1);
	}


	public void info(String arg0, Object[] arg1) {
		log.info(arg0, arg1);
	}


	public void info(String arg0, Throwable arg1) {
		log.info(arg0, arg1);
	}


	public void info(String arg0) {
		log.info(arg0);
	}


	public boolean isDebugEnabled() {
		return log.isDebugEnabled();
	}


	public boolean isDebugEnabled(Marker arg0) {
		return log.isDebugEnabled(arg0);
	}


	public boolean isErrorEnabled() {
		return log.isErrorEnabled();
	}


	public boolean isErrorEnabled(Marker arg0) {
		return log.isErrorEnabled(arg0);
	}


	public boolean isInfoEnabled() {
		return log.isInfoEnabled();
	}


	public boolean isInfoEnabled(Marker arg0) {
		return log.isInfoEnabled(arg0);
	}


	public boolean isTraceEnabled() {
		return log.isTraceEnabled();
	}


	public boolean isTraceEnabled(Marker arg0) {
		return log.isTraceEnabled(arg0);
	}


	public boolean isWarnEnabled() {
		return log.isWarnEnabled();
	}


	public boolean isWarnEnabled(Marker arg0) {
		return log.isWarnEnabled(arg0);
	}


	public void trace(Marker arg0, String arg1, Object arg2, Object arg3) {
		log.trace(arg0, arg1, arg2, arg3);
	}


	public void trace(Marker arg0, String arg1, Object arg2) {
		log.trace(arg0, arg1, arg2);
	}


	public void trace(Marker arg0, String arg1, Object[] arg2) {
		log.trace(arg0, arg1, arg2);
	}


	public void trace(Marker arg0, String arg1, Throwable arg2) {
		log.trace(arg0, arg1, arg2);
	}


	public void trace(Marker arg0, String arg1) {
		log.trace(arg0, arg1);
	}


	public void trace(String arg0, Object arg1, Object arg2) {
		log.trace(arg0, arg1, arg2);
	}


	public void trace(String arg0, Object arg1) {
		log.trace(arg0, arg1);
	}


	public void trace(String arg0, Object[] arg1) {
		log.trace(arg0, arg1);
	}


	public void trace(String arg0, Throwable arg1) {
		log.trace(arg0, arg1);
	}


	public void trace(String arg0) {
		log.trace(arg0);
	}


	public void warn(Marker arg0, String arg1, Object arg2, Object arg3) {
		log.warn(arg0, arg1, arg2, arg3);
	}


	public void warn(Marker arg0, String arg1, Object arg2) {
		log.warn(arg0, arg1, arg2);
	}


	public void warn(Marker arg0, String arg1, Object[] arg2) {
		log.warn(arg0, arg1, arg2);
	}


	public void warn(Marker arg0, String arg1, Throwable arg2) {
		log.warn(arg0, arg1, arg2);
	}


	public void warn(Marker arg0, String arg1) {
		log.warn(arg0, arg1);
	}


	public void warn(String arg0, Object arg1, Object arg2) {
		log.warn(arg0, arg1, arg2);
	}


	public void warn(String arg0, Object arg1) {
		log.warn(arg0, arg1);
	}


	public void warn(String arg0, Object[] arg1) {
		log.warn(arg0, arg1);
	}


	public void warn(String arg0, Throwable arg1) {
		log.warn(arg0, arg1);
	}


	public void warn(String arg0) {
		log.warn(arg0);
	}
}
