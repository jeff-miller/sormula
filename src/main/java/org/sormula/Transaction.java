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
import java.util.ArrayList;
import java.util.List;

import org.sormula.log.ClassLogger;


/**
 * Simple transaction interface for {@link Connection}. This class is not required for
 * most sormula programming and is only used by {@link Database} as a convenience. Other 
 * transaction mechanisms are compatible with sormula. This is required for cached tables.
 * 
 * @author Jeff Miller
 */
public class Transaction 
{
	private static final ClassLogger log = new ClassLogger();
	
	Connection connection;
	boolean active;
	boolean originalAutoCommit;
	List<TransactionListener> listenerList; 
	
	
	/**
	 * Constructs for a connection.
	 * 
	 * @param connection JDBC connection
	 */
	public Transaction(Connection connection)
	{
		this.connection = connection;
		listenerList = new ArrayList<TransactionListener>();
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
	 * Sets the active indicator.
	 * 
	 * @param active true if {@link #begin()} was invoked but neither {@link #commit()} nor
	 * {@link #rollback()} has been invoked
	 * @since 3.0
	 */
	protected void setActive(boolean active)
    {
        this.active = active;
    }


    /**
	 * Starts the transaction. 
	 * 
	 * @throws SormulaException if error
	 */
	public void begin() throws SormulaException
	{
	    if (active) throw new SormulaException("transaction is already active");
	    
        if (log.isDebugEnabled()) log.debug("begin()");
        
        try
		{
			connection.clearWarnings();
			originalAutoCommit = connection.getAutoCommit();
			connection.setAutoCommit(false);
			active = true;
	        
	        // notify after transaction is ready so that transaction can be used
			notifyBegin();
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
	    if (log.isDebugEnabled()) log.debug("commit()");
	    notifyCommit();
	    
		try
		{
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
	    if (log.isDebugEnabled()) log.debug("rollback()");
	    notifyRollback();
	    
		try
		{
			connection.rollback();
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
	
	
	/**
	 * Adds a listener that will be notified of {@link Transaction} events.
	 * 
	 * @param listener notify this class of transaction events
	 * @since 3.0
	 */
	public void addListener(TransactionListener listener)
	{
	    listenerList.add(listener);
	}
	
	
    /**
     * Removes a transaction listener.
     * 
     * @param listener listener to remove
     * @since 3.0
     */
    public void removeListener(TransactionListener listener)
    {
        listenerList.remove(listener);
    }
	
    
    /**
     * Gets list of listeners that were added with {@link #addListener(TransactionListener)}.
     * 
     * @return list of transaction listeners
     * @since 3.0
     */
    protected List<TransactionListener> getListeners()
    {
        return listenerList;
    }
    
	
	/**
	 * Sets {@link #isActive()} to false and restores auto commit to the original state.
	 */
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
	
	
	/**
	 * Invokes {@link TransactionListener#begin(Transaction)} for all listeners.
	 * @since 3.0
	 */
	protected void notifyBegin()
	{
        for (TransactionListener l : listenerList) l.begin(this);
	}
    
    
    /**
     * Invokes {@link TransactionListener#commit(Transaction)} for all listeners.
     * @since 3.0
     */
    protected void notifyCommit()
    {
        for (TransactionListener l : listenerList) l.commit(this);        
    }
    
    
    /**
     * Invokes {@link TransactionListener#rollback(Transaction)} for all listeners.
     * @since 3.0
     */
    protected void notifyRollback()
    {
        for (TransactionListener l : listenerList) l.rollback(this);        
    }
}
