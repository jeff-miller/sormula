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

import org.sormula.log.ClassLogger;


/**
 * A {@link Transaction} that does not perform {@link Connection#commit()} or
 * {@link Connection#rollback()}. It does notify {@link TransactionListener} objects
 * for {@link #begin()}, {@link #commit()}, and {@link #rollback()}.
 * <p>
 * This class is useful for cached operations when connection autocommit is true (that
 * is when no sql transaction is used).
 * 
 * @author Jeff Miller
 * @since 3.0
 */
public class NoOpTransaction extends Transaction
{
	private static final ClassLogger log = new ClassLogger();
	
	
	/**
	 * Constructs for a connection.
	 * 
	 * @param connection JDBC connection
	 */
	public NoOpTransaction(Connection connection)
	{
		super(connection);
	}
	
	
    /**
	 * Starts the transaction. 
	 * 
	 * @throws SormulaException if error
	 */
	@Override
	public void begin() throws SormulaException
	{
	    if (isActive()) throw new SormulaException("transaction is already active");
        if (log.isDebugEnabled()) log.debug("begin()");
		notifyBegin();
	}
	
	
	/** 
	 * Commits the transaction by invoking {@link Connection#commit()}.
	 * 
	 * @throws SormulaException if error
	 */
	@Override
	public void commit() throws SormulaException
	{
	    if (log.isDebugEnabled()) log.debug("commit()");
	    notifyCommit();
	}
	
	
	/**
	 * Aborts the transaction by invoking {@link Connection#rollback()}.
	 * @throws SormulaException
	 */
	@Override
	public void rollback() throws SormulaException
	{
	    if (log.isDebugEnabled()) log.debug("rollback()");
	    notifyRollback();
	}
}
