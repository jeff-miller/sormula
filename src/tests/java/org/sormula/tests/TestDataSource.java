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
package org.sormula.tests;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.sql.DataSource;


/**
 * Simulated {@link DataSource} for tests that require a data source instead of
 * a connection. Only {@link #getConnection()} is implemented. This class is
 * used by tests in org.sormula.tests.activerecord package.
 *  
 * @author Jeff Miller
 */
public class TestDataSource implements DataSource
{
    DatabaseTest databaseTest;


    public TestDataSource(DatabaseTest databaseTest)
    {
        this.databaseTest = databaseTest;
    }

    public Connection getConnection() throws SQLException
    {
        return databaseTest.getConnection();
    }

    public Connection getConnection(String username, String password) throws SQLException
    {
        return null;
    }
    
    public PrintWriter getLogWriter() throws SQLException
    {
        return null;
    }

    public void setLogWriter(PrintWriter out) throws SQLException
    {
    }

    public void setLoginTimeout(int seconds) throws SQLException
    {
    }

    public int getLoginTimeout() throws SQLException
    {
        return 0;
    }

    /* required for jdk 7 */
    public Logger getParentLogger() /* jdk 7 only: throws SQLFeatureNotSupportedException */
    {
        return null;
    }
    
    /* required for jdk 7 */
    public <T> T unwrap(Class<T> iface) throws SQLException
    {
        return null;
    }

    /* required for jdk 7 */
    public boolean isWrapperFor(Class<?> iface) throws SQLException
    {
        return false;
    }
}
