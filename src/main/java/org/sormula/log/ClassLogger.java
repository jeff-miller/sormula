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
 * @deprecated Sormula no longer uses this class since version 4.3
 * <p>
 * Do not use this class. It will be removed in a future version. Most methods
 * delegate to corresponding methods in {@link SormulaLoggerFactory#getClassLogger()}. 
 * Methods with dependencies upon slf4j have been removed.
 * 
 * @since 1.0
 * @author Jeff Miller
 */
@Deprecated
public class ClassLogger
{
    static final String classLoggerClassName = ClassLogger.class.getName();
    private static SormulaLogger log;
    
    
    /**
     * Constructs with logical log name of package name of the caller
     * of this constructor.  For example, com.mycompany.something.MyClass creates
     * new ClassLogger, the logical log name used would be com.mycompany.something.  
     */
    public ClassLogger()
    {
        // search for ClassLogger on stack
        StackTraceElement[] stes =  new Throwable().getStackTrace();
        int e = stes.length - 1;
        for (int i = 0; i < e; ++i) {
            if (stes[i].getClassName().equals(classLoggerClassName)) {
                // next on stack is the class that created me
                log = SormulaLoggerFactory.getLogger(stes[i + 1].getClassName());
                break;
            }
        }
    }

    
	public void debug(String arg0, Object arg1, Object arg2) {
	    log.debug(arg0);
	}


	public void debug(String arg0, Object arg1) {
	    log.debug(arg0);
	}


	public void debug(String arg0, Object[] arg1) {
	    log.debug(arg0);
	}


	public void debug(String arg0, Throwable arg1) {
	    log.debug(arg0);
	}


	public void debug(String arg0) {
	    log.debug(arg0);
	}


	public void error(String arg0, Object arg1, Object arg2) {
	    log.error(arg0);
	}


	public void error(String arg0, Object arg1) {
	    log.error(arg0);
	}


	public void error(String arg0, Object[] arg1) {
	    log.error(arg0);
	}


	public void error(String arg0, Throwable arg1) {
	    log.error(arg0, arg1);
	}


	public void error(String arg0) {
	    log.error(arg0);
	}


	public String getName() {
	    return log.getClass().getCanonicalName();
	}


	public void info(String arg0, Object arg1, Object arg2) {
	    log.info(arg0);
	}


	public void info(String arg0, Object arg1) {
	    log.info(arg0);
	}


	public void info(String arg0, Object[] arg1) {
	    log.info(arg0);
	}


	public void info(String arg0, Throwable arg1) {
	    log.info(arg0);
	}


	public void info(String arg0) {
	    log.info(arg0);
	}


	public boolean isDebugEnabled() {
		return log.isDebugEnabled();
	}


	public boolean isErrorEnabled() {
	    return true;
	}


	public boolean isInfoEnabled() {
		return true;
	}


	public boolean isTraceEnabled() {
		return false;
	}


	public boolean isWarnEnabled() {
		return false;
	}


	public void trace(String arg0, Object arg1, Object arg2) {
	    log.info(arg0);
	}


	public void trace(String arg0, Object arg1) {
	    log.info(arg0);
	}


	public void trace(String arg0, Object[] arg1) {
	    log.info(arg0);
	}


	public void trace(String arg0, Throwable arg1) {
	    log.info(arg0);
	}


	public void trace(String arg0) {
	    log.info(arg0);
	}


	public void warn(String arg0, Object arg1, Object arg2) {
		log.info(arg0);
	}


	public void warn(String arg0, Object arg1) {
	    log.info(arg0);
	}


	public void warn(String arg0, Object[] arg1) {
	    log.info(arg0);
	}


	public void warn(String arg0, Throwable arg1) {
	    log.info(arg0);
	}


	public void warn(String arg0) {
	    log.info(arg0);
	}
}
