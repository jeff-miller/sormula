package org.sormula.active.operation;

import org.sormula.Transaction;


/**
 * A {@link Transaction} for use by classes in org.sormula.active.operation package.
 * 
 * @author Jeff Miller
 * @since 1.7.1
 */
public class OperationTransaction extends Transaction
{
    OperationDatabase operationDatabase;
    
    
    /**
     * Constructs for an operation database.
     * 
     * @param operationDatabase operation database use for all operations within transaction
     */
    public OperationTransaction(OperationDatabase operationDatabase)
    {
        super(operationDatabase.getConnection());
        this.operationDatabase = operationDatabase;
    }


    /**
     * Gets operation database for this transaction.
     * 
     * @return operation database supplied in constructor
     */
    public OperationDatabase getOperationDatabase()
    {
        return operationDatabase;
    }
}
