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
package org.sormula.operation;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.sormula.Table;
import org.sormula.annotation.cascade.DeleteCascade;
import org.sormula.annotation.cascade.InsertCascade;
import org.sormula.annotation.cascade.SaveCascade;
import org.sormula.annotation.cascade.UpdateCascade;
import org.sormula.log.ClassLogger;
import org.sormula.operation.monitor.OperationTime;


/**
 * Base class for operations that modify database.
 * 
 * @since 1.0
 * @author Jeff Miller
 * @param <R> class type which contains members for columns of a row in a table
 */
public abstract class ModifyOperation<R> extends SqlOperation<R>
{
    private static final ClassLogger log = new ClassLogger();
    
    Collection<R> rows;
    int rowsAffected;
    boolean batch;
    
    
    /**
     * Constructs for a table.
     * 
     * @param table modify this table
     * @throws OperationException if error
     */
    public ModifyOperation(Table<R> table) throws OperationException
    {
        super(table);
    }
    
    
    /**
     * Sets parameters from a row object. For operations that affect one row.
     * Use this as alternative to {@link #setParameters(Object...)}.
     * 
     * @param row get parameter values from this row object
     */
    public void setRow(R row)
    {
        rows = new ArrayList<R>(1);
        rows.add(row);
    }
    
    
    /**
     * Sets parameters from rows in an array. For operations that use array of rows.
     *     
     * @param rows row objects to modify
     * @since 1.9 and 2.3
     */
    public void setRows(R[] rows)
    {
        setRows(Arrays.asList(rows));
    }
    
    
    /**
     * Sets parameters from rows in a collection. For operations that use {@link Collection} of rows.
     *     
     * @param rows row objects to modify
     */
    public void setRows(Collection<R> rows)
    {
        this.rows = rows;
        
        // rows and object parameters are mutually exclusive
        super.setParameters((Object)null); // note: must invoke super method
    }
    
    
    /**
     * Sets parameters from rows in a map. For operations that use {@link Map} of rows.
     *     
     * @param rows row objects to modify
     */
    public void setRows(Map<?, R> rows)
    {
        this.rows = rows.values();
        
        // rows and object parameters are mutually exclusive
        super.setParameters((Object)null); // note: must invoke super method
    }

    
    /**
     * Allows modification of table data using objects instead from row objects. This method is
     * not recommended for updates or inserts but may be useful for delete when no row object is
     * available. Use {@link #setRow(Object)}, {@link #setRows(Collection)}, or {@link #setRows(Map)}
     * instead. {@link #preExecute(Object)} and {@link #postExecute(Object)} will not be invoked
     * if this method is used.
     * 
     * @param parameters values to set in {@link PreparedStatement} for modify operation
     */
    @Override
    public void setParameters(Object... parameters)
    {
        super.setParameters(parameters);
        
        // rows and object parameters are mutually exclusive
        rows = null;
    }


    /**
     * Gets batch mode.
     * 
     * @return true if rows are to be inserted/updated/deleted in batch 
     * @since 1.9 and 2.3
     * @see PreparedStatement#executeBatch()
     */
    public boolean isBatch()
    {
        return batch;
    }


    /**
     * Sets batch mode. Set to true prior to {@link #execute()} to insert/update/delete rows using
     * JDBC batch mode. Batch mode does not support identity columns or cascades. Auto commit must 
     * be off for batch mode. {@link #preExecute(Object)} is invoked prior to batch add and 
     * {@link #postExecute(Object)} is invoked after batch execute.
     *  
     * @param batch true to use JDBC batching for {@link #execute()}
     * @since 1.9 and 2.3
     * @see PreparedStatement#executeBatch()
     */
    public void setBatch(boolean batch)
    {
        this.batch = batch;
        
        if (batch && isAutoGeneratedKeys())
        {
            log.warn("identity columns are not supported for batch mode");
            setAutoGeneratedKeys(false); // avoid errors 
        }
    }


    /**
     * Executes operation for all row parameters using current prepared statement.
     * {@link #getRowsAffected()} will return the sum of all rows affected.
     * 
     * @throws OperationException if error
     */
    @Override
    public void execute() throws OperationException
    {
        if (readOnly) throw new OperationException("Attempt to modify with read-only operation");
            
        initOperationTime();
        prepareCheck();
        int allRowsAffected = 0; 
        OperationTime operationTime = getOperationTime();
        
        try
        {
            PreparedStatement ps = getPreparedStatement();
            
            if (isBatch())
            {
                // execute all rows as a batch
                if (rows != null && rows.size() > 0) 
                {
                    // some drivers require at least one addBatch with executeBatch so perform only if there are rows
                    
                    // prepare
                    for (R row: rows)
                    {
                        if (log.isDebugEnabled()) log.debug("write batch parameters from row=" + row);
                        setNextParameter(1);
                        preExecute(row);
                        operationTime.startWriteTime();
                        writeColumns(row);
                        writeWhere(row);
                        ps.addBatch();
                        operationTime.stop();
                    }
                    
                    // execute
                    if (log.isDebugEnabled()) log.debug("execute batch");
                    operationTime.startExecuteTime();
                    int[] rowsAffected = ps.executeBatch(); 
                    for (int r: rowsAffected)
                    {
                        if (r > 0) allRowsAffected += r;
                    }
                    operationTime.stop();
                    
                    // post execute
                    for (R row: rows)
                    {
                        postExecute(row);
                    }
                    
                    ps.clearBatch();
                }
            }
            else if (rows != null)
            {
                // operation parameters from rows
                for (R row: rows)
                {
                    if (log.isDebugEnabled()) log.debug("write parameters from row=" + row);
                    
                    setNextParameter(1);
                    preExecuteCascade(row);
                    preExecute(row);
                    operationTime.startWriteTime();
                    writeColumns(row);
                    writeWhere(row);
                    operationTime.stop();
                    
                    if (log.isDebugEnabled()) log.debug("execute update row=" + row);
                    operationTime.startExecuteTime();
                    int updateCount = ps.executeUpdate();
                    allRowsAffected += updateCount;
                    operationTime.stop();
                    
                    if (updateCount > 0)
                    {
                        postExecute(row);
                        postExecuteCascade(row);
                    }
                }
            }
            else if (getParameters() != null)
            {
                // operation parameters from objects
                if (log.isDebugEnabled()) log.debug("write parameters from objects");
                setNextParameter(1);
                operationTime.startWriteTime();
                writeParameters();
                operationTime.stop();
                
                if (log.isDebugEnabled()) log.debug("execute update");
                operationTime.startExecuteTime();
                allRowsAffected = ps.executeUpdate();
                operationTime.stop();
            }
        }
        catch (Exception e)
        {
            throw new OperationException("execute() error", e);
        }
        
        setRowsAffected(allRowsAffected);
    }
    

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws OperationException
    {
        super.close();
        rows = null;
    }


    /**
     * Gets the number of rows affected from most recent invocation of {@link #execute()}.
     * 
     * @return count of rows affect by most recent {@link #execute()}
     */
    public int getRowsAffected()
    {
        return rowsAffected;
    }


    /**
     * Modifies one row. Set parameters, executes, closes.
     * 
     * @param row row to use for parameters
     * @return {@link #getRowsAffected()}
     * @throws OperationException if error
     * @since 1.4
     */
    public int modify(R row) throws OperationException
    {
        setRow(row);
        execute();
        close();
        return getRowsAffected();
    }
    
    
    /**
     * Modifies a collection of rows. Set parameters, executes, closes.
     * 
     * @param rows collection of rows to use as parameters 
     * @return {@link #getRowsAffected()}
     * @throws OperationException if error
     * @since 1.4
     */
    public int modifyAll(Collection<R> rows) throws OperationException
    {
        setRows(rows);
        execute();
        close();
        return getRowsAffected();
    }


    /**
     * Modifies row(s) with sql parametes as Objects
     * 
     * @param parameters operation parameters as objects (see {@link #setParameters(Object...)})
     * @return count of rows affected
     * @throws OperationException if error
     * @since 1.4
     */
    public int modify(Object... parameters) throws OperationException
    {
        setParameters(parameters);
        execute();
        close();
        return getRowsAffected();
    }

    
    /**
     * Invoked prior to JDBC execute. Override to modify the row prior to JDBC execute.
     * Default implementaion does nothing. This method is not invoked when 
     * {@link ModifyOperation#setParameters(Object...)} is used since no row is available.
     * 
     * @param row row for JDBC execute
     * @throws OperationException if error
     */
    protected void preExecute(R row) throws OperationException
    {
    }
    
    
    /**
     * Invoked after JDBC execute. Override to modify the row after JDBC execute has occured.
     * Default implementaion does nothing. This method is not invoked when 
     * {@link ModifyOperation#setParameters(Object...)} is used since no row is available. 
     * 
     * @param row row for JDBC execute
     * @throws OperationException if error
     */
    protected void postExecute(R row) throws OperationException
    {
    }

    
    /**
     * Invoked prior to JDBC execute. Performs all modify cascade operations 
     * where post flag is false. 
     * @see InsertCascade#post()
     * @see UpdateCascade#post()
     * @see SaveCascade#post()
     * @see DeleteCascade#post()
     * 
     * @param row row for JDBC execute 
     */
    protected void preExecuteCascade(R row) throws OperationException
    {
        cascade(row, false);
    }


    /**
     * Invoked after JDBC execute. Performs all modify cascade operations 
     * where post flag is true. 
     * @see InsertCascade#post()
     * @see UpdateCascade#post()
     * @see SaveCascade#post()
     * @see DeleteCascade#post()
     * 
     * @param row row for JDBC execute 
     */
    protected void postExecuteCascade(R row) throws OperationException
    {
        cascade(row, true);
    }
    

    protected void setRowsAffected(int rowsAffected)
    {
        this.rowsAffected = rowsAffected;
    }
}
