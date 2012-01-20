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
package org.sormula;

import java.sql.Connection;
import java.sql.SQLException;

import org.sormula.log.ClassLogger;


/**
 * Simple transaction interface for {@link Connection}. This class is not required for
 * any sormula programming and is only used by {@link Database} as a convenience. Other 
 * transaction mechanisms are compatible with sormula.
 * 
 * @author Jeff Miller
 */
public class Transaction 
{
	private static final ClassLogger log = new ClassLogger();
	
	Connection connection;
	boolean active;
	boolean originalAutoCommit;
	
	
	/**
	 * Constructs for a connection.
	 * 
	 * @param connection JDBC connection
	 */
	public Transaction(Connection connection)
	{
		this.connection = connection;
	}
	
	
	/**
	 * Gets the connection supplied in the constructor.
	 * 
	 * @return connect for this transaction
	 */
	public Connection getConnection()
	{
		return connection;
	}
	
	
	/**
	 * Reports if transaction is ongoing.
	 * 
	 * @return true if {@link #begin()} has been invoked but not {@link #commit()} or {@link #rollback()}
	 */
	public boolean isActive()
	{
		return active;
	}
	
	
	/**
	 * Starts the transaction.
	 * 
	 * @throws SormulaException if error
	 */
	public void begin() throws SormulaException
	{
		try
		{
			if (log.isDebugEnabled()) log.debug("begin");
			connection.clearWarnings();
			originalAutoCommit = connection.getAutoCommit();
			connection.setAutoCommit(false);
			active = true;
		}
		catch (SQLException e)
		{
			throw new SormulaException("begin transaction error", e);
		}
	}
	
	
	/** 
	 * Commits the transaction by invoking {@link Connection#commit()}.
	 * 
	 * @throws SormulaException if error
	 */
	public void commit() throws SormulaException
	{
		try
		{
			if (log.isDebugEnabled()) log.debug("commit");
			connection.commit();
		}
		catch (SQLException e)
		{
			throw new SormulaException("commit transaction error", e);
		}
		finally
		{
			cleanUp();
		}
	}
	
	
	/**
	 * Aborts the transaction by invoking {@link Connection#rollback()}.
	 * @throws SormulaException
	 */
	public void rollback() throws SormulaException
	{
		try
		{
			if (log.isDebugEnabled()) log.debug("rollback");
			connection.commit();
		}
		catch (SQLException e)
		{
			throw new SormulaException("rollback transaction error", e);
		}
		finally
		{
			cleanUp();
		}
	}
	
	
	protected void cleanUp()
	{
		try
		{
			active = false;
			connection.setAutoCommit(originalAutoCommit);
		}
		catch (SQLException e)
		{
			log.error("clean up error", e);
		}
	}
}
