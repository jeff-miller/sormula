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
package org.sormula.active.operation;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.Transaction;
import org.sormula.active.ActiveDatabase;
import org.sormula.active.ActiveException;
import org.sormula.active.ActiveRecord;
import org.sormula.active.ActiveTable;
import org.sormula.log.ClassLogger;


/**
 * Template for executing {@link #operate()} on an active table. Used by {@link ActiveTable}
 * for thread-safe and transaction based operation for a table (and any cascaded tables).
 * A database transaction will only be used if {@link Connection#getAutoCommit()} returns false.
 * 
 * @author Jeff Miller
 * @since 1.7
 *
 * @param <R> record type
 * @param <T> execute return type
 */
public abstract class ActiveOperation<R extends ActiveRecord, T>
{
    private static final ClassLogger log = new ClassLogger();
    ActiveTable<R> activeTable;
    String exceptionMessage;
    ActiveDatabase activeDatabase;
    OperationDatabase operationDatabase;
    Transaction transaction;
    Table<R> table;
    
    
    /**
     * Constructs for a table and fail message.
     * 
     * @param activeTable table to operate upon
     * @param exceptionMessage exception message for {@link ActiveException} if operation fails 
     */
    public ActiveOperation(ActiveTable<R> activeTable, String exceptionMessage)
    {
        this.activeTable = activeTable;
        this.exceptionMessage = exceptionMessage;
        activeDatabase = activeTable.getActiveDatabase();
    }

    
    /**
     * Executes {@link #operate()} within a try/catch and transaction. If {@link #operate()}
     * throws an exception, then transaction is rolled back.
     * 
     * @return value returned by {@link #operate()}
     * @throws ActiveException if error
     */
    public T execute() throws ActiveException
    {
        T result;
        
        try
        {
            begin();
            result = operate();
            commit();
        }
        catch (Exception e)
        {
            rollback();
            throw new ActiveException(exceptionMessage, e);
        }
        
        return result;
    }


    /**
     * Invoked by {@link #execute()} to perform the operation. Subclasses must implement.
     * 
     * @return result of operation (Integer, List<R>, etc.)
     * @throws Exception
     */
    protected abstract T operate() throws Exception;

    
    protected void attach(R record)
    {
        record.attach(activeDatabase);
    }

    
    protected void attach(Collection<R> records)
    {
        for (R r: records) r.attach(activeDatabase);
    }

    
    protected void begin() throws ActiveException
    {
        if (log.isDebugEnabled()) log.debug("ActiveOperation.begin()");
        try
        {
            operationDatabase = new OperationDatabase(activeDatabase);
            table = operationDatabase.getTable(activeTable.getRecordClass());
            
            if (!operationDatabase.getConnection().getAutoCommit())
            {
                // use transaction for connections that don't have autocommit
                if (log.isDebugEnabled()) log.debug("ActiveOperation start transaction");
                transaction = operationDatabase.getTransaction();
                transaction.begin();
            }
        }
        catch (SQLException e)
        {
            throw new ActiveException("error creating operation database", e);
        }
        catch (SormulaException e)
        {
            close(); // avoid connection leak
            throw new ActiveException("error starting active record transaction", e);
        }
    }
    
    
    protected void commit() throws ActiveException
    {
        if (log.isDebugEnabled()) log.debug("ActiveOperation.commit()");
        
        try
        {
            if (transaction != null)
            {
                if (log.isDebugEnabled()) log.debug("ActiveOperation commit transaction");
                transaction.commit();
                transaction = null;
            }
        }
        catch (SormulaException e)
        {
            throw new ActiveException("error committing transaction", e);
        }
        finally
        {
            close();
        }
    }

    
    protected void rollback() throws ActiveException
    {
        if (log.isDebugEnabled()) log.debug("ActiveOperation.rollback()");
        
        try
        {
            if (transaction != null)
            {
                if (log.isDebugEnabled()) log.debug("ActiveOperation rollback transaction");
                transaction.rollback();
                transaction = null;
            }
        }
        catch (SormulaException e)
        {
            throw new ActiveException("error rolling back transaction", e);
        }
        finally
        {
            close();
        }
    }
    
    
    protected void close() throws ActiveException
    {
        table = null;
        
        if (operationDatabase != null)
        {
            try
            {
                operationDatabase.getConnection().close();
            }
            catch (SQLException e)
            {
                throw new ActiveException("error closing connection", e);
            }
            
            operationDatabase = null;
        }
    }
}
