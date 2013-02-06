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

import org.sormula.Table;
import org.sormula.active.ActiveDatabase;
import org.sormula.active.ActiveException;
import org.sormula.active.ActiveRecord;
import org.sormula.active.ActiveTable;
import org.sormula.active.ActiveTransaction;
import org.sormula.active.NoDefaultActiveDatabaseException;
import org.sormula.log.ClassLogger;


/**
 * Template for executing {@link #operate()} on an active table. Used by {@link ActiveTable}
 * for thread-safe and transaction based operation for a table (and any cascaded tables).
 * A database transaction will only be used if {@link Connection#getAutoCommit()} returns false.
 * 
 * @author Jeff Miller
 * @since 1.7 and 2.1
 *
 * @param <R> record type
 * @param <T> execute return type
 */
public abstract class ActiveOperation<R extends ActiveRecord<R>, T>
{
    private static final ClassLogger log = new ClassLogger();
    ActiveTable<R> activeTable;
    String exceptionMessage;
    ActiveDatabase activeDatabase;
    ActiveTransaction activeTransaction;
    OperationDatabase operationDatabase;
    Table<R> table;
    boolean localTransaction;
    
    
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
        
        if (activeDatabase == null)
        {
            activeDatabase = ActiveDatabase.getDefault();
            if (activeDatabase == null) throw new NoDefaultActiveDatabaseException();
        }
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
        activeTransaction = activeDatabase.getActiveTransaction();
        localTransaction = false;
        
        try
        {
            if (activeTransaction == null)
            {
                // no active transaction, create one (creates operation transaction and operation db)
                activeTransaction = new ActiveTransaction(activeDatabase);
                localTransaction = true;
            }
             
            // init based upon activeTransaction
            operationDatabase = activeTransaction.getOperationTransaction().getOperationDatabase();
            table = operationDatabase.getTable(activeTable.getRecordClass());
                
            if (localTransaction)
            {
                // transaction exists only for this method
                if (operationDatabase.getConnection().getAutoCommit())
                {
                    // don't use transaction since autocommit is off
                    if (log.isDebugEnabled()) log.debug("execute() with no transaction");
                    result = operate();
                }
                else
                {
                    // use transaction since autocommit is on
                    if (log.isDebugEnabled()) log.debug("execute() with local ActiveTransaction");
                    try
                    {
                        activeTransaction.begin();
                        result = operate();
                        activeTransaction.commit();
                    }
                    catch (Exception e)
                    {
                        activeTransaction.rollback();
                        throw e;
                    }
                }
            }
            else
            {
                // a transaction is already active
                if (log.isDebugEnabled()) log.debug("execute() using existing transaction");
                result = operate();
            }
        }
        catch (Exception e)
        {
            throw new ActiveException(exceptionMessage, e);
        }
        finally
        {
            close();
        }
    
        return result;
    }


    /**
     * Reports type of transaction used. If true, then the transaction is valid only 
     * while {@link #execute()} is in progress.
     * 
     * @return true if transaction is for use only by {@link #execute()}
     */
    public boolean isLocalTransaction()
    {
        return localTransaction;
    }


    /**
     * Invoked by {@link #execute()} to perform the operation. Subclasses must implement.
     * 
     * @return result of operation (Integer, List<R>, etc.)
     * @throws Exception
     */
    protected abstract T operate() throws Exception;

    
    /**
     * Gets the database used in {@link #execute()}. Database is closed when
     * the transaction is closed.
     * 
     * @return database used to by this operation
     */
    protected OperationDatabase getOperationDatabase()
    {
        return operationDatabase;
    }
    

    /**
     * Gets the table used in {@link #execute()}. Table is obtained with
     * {@link OperationDatabase#getTable(Class)}.
     * 
     * @return table used to by this operation
     */
    protected Table<R> getTable()
    {
        return table;
    }


    /**
     * Attaches an active record to the active database used by this operation with
     * {@link ActiveRecord#attach(ActiveDatabase)}.
     * 
     * @param record active record to be attached
     */
    protected void attach(R record)
    {
        record.attach(activeDatabase);
    }

    
    /**
     * Attaches a collection of active record to the active database used by this operation with
     * {@link ActiveRecord#attach(ActiveDatabase)}.
     * 
     * @param records active records to be attached
     */
    protected void attach(Collection<R> records)
    {
        for (R r: records) r.attach(activeDatabase);
    }


    /**
     * Cleans up any used resources. The most important clean up is closing the JDBC 
     * connection if {@link #isLocalTransaction()} is true. Otherwise JDBC connection will
     * be closed when transaction completes.
     * 
     * @throws ActiveException if error
     */
    protected void close() throws ActiveException
    {
        if (localTransaction && operationDatabase != null)
        {
            // avoid connection leak
            try
            {
                Connection connection = operationDatabase.getConnection();
                if (connection != null) connection.close();
            }
            catch (SQLException e)
            {
                throw new ActiveException("error closing connection", e);
            }
        }
        
        // repeated close has no affect
        operationDatabase = null;
        table = null;
    }
}
