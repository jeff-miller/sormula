package org.sormula.active;

import java.sql.Connection;
import java.sql.SQLException;

import org.sormula.SormulaException;
import org.sormula.active.operation.OperationDatabase;
import org.sormula.active.operation.OperationTransaction;
import org.sormula.log.ClassLogger;


/**
 * A class to group multiple active record operations within one JDBC transaction.
 * 
 * <blockquote><pre>
    ActiveTransaction transaction = new ActiveTransaction(new ActiveDatabase(dataSource));
    
    try
    {
        transaction.begin();
        
        // ...some active operations...
        
        transaction.commit();
    }
    catch (ActiveException e)
    {
        transaction.rollback();
    }
 * </pre></blockquote>
 * 
 * Only one instance of an ActiveTransaction may be in use for an instance of an ActiveDatabase at
 * one time. Create one instance of an ActiveDatabase for each possible transaction that may occur 
 * at that same time.
 * 
 * @author Jeff Miller
 * @since 1.7.1 and 2.1.1
 */
public class ActiveTransaction 
{
    private static final ClassLogger log = new ClassLogger();
    ActiveDatabase activeDatabase;
    OperationTransaction operationTransaction;
    
    
    /**
     * Constructs for the default active database. The default active database must be
     * set with {@link ActiveDatabase#setDefault(ActiveDatabase)} prior to using
     * this constructor.
     * <p>
     * Use this constructor only if no other thread may be using it at the same time. This is
     * the case since there is only one default {@link ActiveDatabase} per classloader and only one
     * transaction may be in use per {@link ActiveDatabase}.  
     * 
     * @throws ActiveException if error
     */
    public ActiveTransaction() throws ActiveException
    {
        this.activeDatabase = ActiveDatabase.getDefault();
        
        // fail here since activeDatabase must be known
        if (activeDatabase == null) throw new ActiveException("no default active database has been set; use ActiveDatabase.setDefault()");
        
        init();
    }
    
    
    /**
     * Constructs an {@link ActiveDatabase}. 
     * 
     * @param activeDatabase active database associated with transaction
     * @throws ActiveException if error
     */
    public ActiveTransaction(ActiveDatabase activeDatabase) throws ActiveException
    {
        this.activeDatabase = activeDatabase;
        init();
    }
    
    
    void init() throws ActiveException
    {
        try
        {
            operationTransaction = new OperationTransaction(new OperationDatabase(activeDatabase));
        }
        catch (Exception e)
        {
            throw new ActiveException("error creating transaction delegate", e);
        }
    }
    
    
    /**
     * Gets the active database associated with this transaction. Either the default if
     * {@link #ActiveTransaction()} was used or the one supplied in 
     * {@link #ActiveTransaction(ActiveDatabase)}.
     * 
     * @return active database
     */
    public ActiveDatabase getActiveDatabase()
    {
        return activeDatabase;
    }


    /**
     * Gets the operation transaction created by this transaction. The method is typically only
     * used by classes in org.sormula.active.operation package.
     * 
     * @return operation transaction
     */
    public OperationTransaction getOperationTransaction()
    {
        return operationTransaction;
    }


    /**
     * Starts the transaction. Use {@link #commit()} or {@link #rollback()} to close the transaction.
     * This method invokes {@link ActiveDatabase#setActiveTransaction(ActiveTransaction)} to indicate
     * that this transaction is in use for the active database.
     * 
     * @throws ActiveException if error
     */
    public void begin() throws ActiveException
    {
        try
        {
            if (log.isDebugEnabled()) log.debug("begin");
            operationTransaction.begin();
            activeDatabase.setActiveTransaction(this); // important: if begin() fails, this does not happen
        }
        catch (SormulaException e)
        {
            close();
            throw new ActiveException("error beginning transaction", e);
        }
    }
    
    
    /**
     * Completes the transaction. Invokes {@link Connection#commit()} on the underlying 
     * JDBC connection. This method invokes {@link ActiveDatabase#setActiveTransaction(ActiveTransaction)} 
     * with a null parameter to indicate that no transaction is in use for the active database.
     * 
     * @throws ActiveException if error
     */
    public void commit() throws ActiveException
    {
        try
        {
            if (log.isDebugEnabled()) log.debug("commit");
            operationTransaction.commit();
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
    
    
    /**
     * Rolls back the transaction. Invokes {@link Connection#rollback()} on the underlying 
     * JDBC connection. This method invokes {@link ActiveDatabase#setActiveTransaction(ActiveTransaction)} 
     * with a null parameter to indicate that no transaction is in use for the active database.
     * 
     * @throws ActiveException if error
     */
    public void rollback() throws ActiveException
    {
        try
        {
            if (log.isDebugEnabled()) log.debug("rollback");
            operationTransaction.rollback();
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

    
    /**
     * Cleans up resources that are used by this transaction. Specifically, the active transaction
     * is removed from active database and the JDBC connection is closed.
     * 
     * @throws ActiveException if error
     */
    protected void close() throws ActiveException
    {
        activeDatabase.setActiveTransaction(null);
        
        try
        {
            OperationDatabase od = operationTransaction.getOperationDatabase();
            od.logTimings();
            Connection connection = od.getConnection();
            if (connection != null) connection.close();
        }
        catch (SQLException e)
        {
            throw new ActiveException("error closing connection", e);
        }
    }
}
