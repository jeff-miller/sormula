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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.sormula.Database;
import org.sormula.Table;
import org.sormula.annotation.cascade.DeleteCascade;
import org.sormula.annotation.cascade.InsertCascade;
import org.sormula.annotation.cascade.SaveCascade;
import org.sormula.annotation.cascade.UpdateCascade;
import org.sormula.cache.Cache;
import org.sormula.cache.CacheException;
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
    int[] modifyCounts;
    
    
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
        rows = new ArrayList<>(1);
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
     * Gets the rows that will be modified. All of the setter methods for setting rows convert
     * parameters to {@link Collection} so this method will get the row(s) regardless of setter
     * method that was used.
     * 
     * @return rows to be modified or null if {@link #setParameters(Object...)} was used
     * @since 3.0
     */
    public Collection<R> getRows()
    {
        return rows;
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
     * @return true if rows are to be inserted/updated/deleted in batch mode
     * @since 1.9 and 2.3
     * @see PreparedStatement#executeBatch()
     */
    public boolean isBatch()
    {
        return batch;
    }


    /**
     * Sets batch mode. Set to true prior to {@link #execute()} to insert/update/delete rows using
     * JDBC batch mode. Batch mode does not support identity columns. Auto commit must 
     * be off for batch mode. {@link #preExecute(Object)} is invoked prior to batch add and 
     * {@link #postExecute(Object)} is invoked after batch execute.
     * <p>
     * Since version 4.1, cascades are performed for batch mode.
     * <p>
     * Batch modifications are not cached. So when batch is true and table is cached, then 
     * table cache is flushed prior to executing batch modifications to avoid inconsistencies in
     * cache. {@link Database#flush()} may be required if batched rows affect foreign key
     * relationships.
     * 
     * @param batch true to use JDBC batching for {@link #execute()}
     * @since 1.9 and 2.3
     * @see PreparedStatement#executeBatch()
     */
    public void setBatch(boolean batch)
    {
        this.batch = batch;
    }


    /**
     * Executes operation for all row parameters using current prepared statement.
     * {@link #getRowsAffected()} will return the sum of all rows affected.
     * 
     * @throws OperationException if error
     * @throws BatchException for batch operations if EXECUTE_FAILED is returned
     * @throws BatchException for batch operations if SUCCESS_NO_INFO is returned and cascading is needed
     * @throws ReadOnlyException if row or table is read only
     */
    @Override
    public void execute() throws OperationException
    {
        if (readOnly) throw new ReadOnlyException("Attempt to modify when table or operation is read-only");
            
        if (isCached())
        {
            try
            {
                // notify cache of start of execution
                getTable().getCache().execute(this);
            }
            catch (CacheException e)
            {
                throw new OperationException("execute error", e);
            }
        }

        initOperationTime();
        prepareCheck();
        int allRowsAffected = 0; 
        OperationTime operationTime = getOperationTime();
        
        try
        {
            PreparedStatement ps = getPreparedStatement();
            
            if (batch)
            {
                if (log.isDebugEnabled()) log.debug("begin batch");
                
                // batch modifications are not cached, flush to avoid inconsistencies
                getTable().flush();
                
                // execute all rows as a batch
                if (rows != null && rows.size() > 0) 
                {
                    // some drivers require at least one addBatch with executeBatch so perform only if there are rows
                    modifyCounts = new int[rows.size()];
                    
                    // prepare
                    for (R row: rows)
                    {
                        if (log.isDebugEnabled()) log.debug("write batch parameters from row=" + row);
                        setNextParameter(1);
                        if (isCascading()) preExecuteCascade(row);
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
                    int rowIndex = 0;
                    for (int r: rowsAffected)
                    {
                        modifyCounts[rowIndex++] = r;
                        if (r > 0) allRowsAffected += r;
                        else if (r == Statement.SUCCESS_NO_INFO) ++allRowsAffected; // assume 1 row
                        else if (r == Statement.EXECUTE_FAILED) throw new BatchException("EXECUTE_FAILED returned for executeBatch()");
                    }
                    operationTime.stop();

                    if (log.isDebugEnabled())
                    {
                        log.debug("batch rows affected:");
                        for (int r: rowsAffected) log.debug(Integer.toString(r));
                    }
                    
                    // post execute
                    int affectedIndex = 0;
                    for (R row: rows)
                    {
                        int r = rowsAffected[affectedIndex];
                        if (r > 0 || r == Statement.SUCCESS_NO_INFO)
                        {
                            // perform only if row was affected
                            postExecute(row);
                            if (isCascading())
                            {
                                if (r == Statement.SUCCESS_NO_INFO)
                                {
                                    // cannot know if cascade should occur since no update count
                                    throw new BatchException("SUCCESS_NO_INFO returned for executeBatch(); cascade cannot be determined");
                                }
                                
                                postExecuteCascade(row);
                            }
                        }
                        
                        ++affectedIndex;
                    }
                    
                    ps.clearBatch();
                }
                else
                {
                    // no rows 
                    modifyCounts = new int[0];
                }
            }
            else if (rows != null)
            {
                // operation parameters from rows
                modifyCounts = new int[rows.size()];
                int rowIndex = 0;
                for (R row: rows)
                {
                    boolean cacheAuthority = false;
                    
                    if (isCached())
                    {
                        if (log.isDebugEnabled()) log.debug("modify cache " + table.getRowClass());
                        if (notifyCacheModify(row))
                        {
                            // cache will modify database, assume 1 row will be modified
                            ++allRowsAffected;
                            modifyCounts[rowIndex++] = 1;
                            cacheAuthority = true;
                            
                            if (isCascading())
                            {
                                // target tables must be cascaded now for consistency
                                preExecuteCascade(row);
                                postExecuteCascade(row);
                            }
                        }
                    }
                    
                    if (!cacheAuthority)
                    {
                        // cache will not modify database for row
                        if (log.isDebugEnabled()) log.debug("write parameters from row=" + row);
                        setNextParameter(1);
                        if (isCascading()) preExecuteCascade(row);
                        preExecute(row);
                        operationTime.startWriteTime();
                        writeColumns(row);
                        writeWhere(row);
                        operationTime.stop();
                        
                        if (log.isDebugEnabled()) log.debug("execute update row=" + row);
                        operationTime.startExecuteTime();
                        int updateCount = ps.executeUpdate();
                        allRowsAffected += updateCount;
                        modifyCounts[rowIndex++] = updateCount;
                        if (log.isDebugEnabled()) log.debug("execute update =" + updateCount + " rows affected=" + allRowsAffected);
                        operationTime.stop();
                        
                        if (updateCount > 0)
                        {
                            // perform the following only when database indicates that a modification occurred
                            postExecute(row);
                            
                            if (isCascading()) postExecuteCascade(row);
                        }
                        
                        if (isCached() && updateCount > 0)
                        {
                            // notify cache that database was modified with row
                            notifyCacheModified(row);
                        }
                    }
                }
            }
            else if (getParameters() != null)
            {
                // operation parameters from objects
                modifyCounts = new int[1];
                if (log.isDebugEnabled()) log.debug("write parameters from objects");
                setNextParameter(1);
                operationTime.startWriteTime();
                writeParameters();
                operationTime.stop();
                
                if (log.isDebugEnabled()) log.debug("execute update");
                operationTime.startExecuteTime();
                allRowsAffected = modifyCounts[0] = ps.executeUpdate();
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
     * Gets the number of rows that were modified. The size of the array will be the
     * size of {@link #getRows()} if not null or 1 if {@link #getParameters()} is not null. The
     * value of each array element is determined by one of the following:
     * <ul> 
     * <li>return of {@link PreparedStatement#executeUpdate()} for each row in {@link #getRows()}</li>
     * <li>return of {@link PreparedStatement#executeBatch()} if {@link #isBatch()} is true</li>
     * <li>return of {@link PreparedStatement#executeUpdate()} if {@link #getParameters()} is not null</li>
     * </ul>
     * 
     * @return array of counts of rows that were modified; for batch operations some values
     * may be {@link Statement#SUCCESS_NO_INFO} for some databases  
     * 
     * @since 4.1
     */
    public int[] getModifyCounts()
    {
        return modifyCounts;
    }


    /**
     * Modifies one row. Set parameters, executes, closes.
     * <p>
     * Since this class implements {@link AutoCloseable}, you may see resource leak
     * warning when you use this method. You can ignore it, add a suppress annotation, 
     * explicitly close, or close with a try-with-resources statement. Closing an operation 
     * more than once will not cause problems since the close methods are idempotent. 
     * 
     * @param row row to use for parameters
     * @return {@link #getRowsAffected()}
     * @throws OperationException if error
     * @since 1.4
     */
    public int modify(R row) throws OperationException
    {
        setRow(row);
        setRowsAffected(0);
        try 
        {
        	execute();
        }
        finally
        {
        	close();
        }
        return getRowsAffected();
    }
    
    
    /**
     * Modifies a collection of rows. Set parameters, executes, closes.
     * <p>
     * Since this class implements {@link AutoCloseable}, you may see resource leak
     * warning when you use this method. You can ignore it, add a suppress annotation, 
     * explicitly close, or close with a try-with-resources statement. Closing an operation 
     * more than once will not cause problems since the close methods are idempotent. 
     * 
     * @param rows collection of rows to use as parameters 
     * @return {@link #getRowsAffected()}
     * @throws OperationException if error
     * @since 1.4
     */
    public int modifyAll(Collection<R> rows) throws OperationException
    {
        setRows(rows);
        setRowsAffected(0);
        try 
        {
        	execute();
        }
        finally
        {
        	close();
        }
        return getRowsAffected();
    }


    /**
     * Modifies row(s) with sql parameters as Objects. Executes and then closes.
     * <p>
     * Since this class implements {@link AutoCloseable}, you may see resource leak
     * warning when you use this method. You can ignore it, add a suppress annotation, 
     * explicitly close, or close with a try-with-resources statement. Closing an operation 
     * more than once will not cause problems since the close methods are idempotent. 
     * 
     * @param parameters operation parameters as objects (see {@link #setParameters(Object...)})
     * @return count of rows affected
     * @throws OperationException if error
     * @since 1.4
     */
    public int modify(Object... parameters) throws OperationException
    {
        setParameters(parameters);
        setRowsAffected(0);
        try 
        {
        	execute();
        }
        finally
        {
        	close();
        }
        return getRowsAffected();
    }

    
    /**
     * Invoked prior to JDBC execute. Override to modify the row prior to JDBC execute.
     * Default implementation does nothing. This method is not invoked when 
     * {@link ModifyOperation#setParameters(Object...)} is used since no row is available.
     * 
     * @param row row for JDBC execute
     * @throws OperationException if error
     */
    protected void preExecute(R row) throws OperationException
    {
    }
    
    
    /**
     * Invoked after JDBC execute. Override to modify the row after JDBC execute has occurred.
     * Default implementation does nothing. This method is not invoked when 
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
     * @throws OperationException if error 
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
     * @throws OperationException if error
     */
    protected void postExecuteCascade(R row) throws OperationException
    {
        cascade(row, true);
    }
    

    protected void setRowsAffected(int rowsAffected)
    {
        this.rowsAffected = rowsAffected;
    }
    
    
    /**
     * Delegates to {@link Cache#insert(Object)}, {@link Cache#update(Object)},
     * {@link Cache#delete(Object)} based upon the subclass operation.

     * @param row row to be modified
     * @return true if cache is authority for row (cache will modify row in database);
     * false if cache does not modify the database 
     * @since 3.0
     * @throws OperationException if error
     */
    protected abstract boolean notifyCacheModify(R row) throws OperationException;
    
    
    /**
     * Delegates to {@link Cache#inserted(Object)}, {@link Cache#updated(Object)},
     * {@link Cache#deleted(Object)} based upon the subclass operation.

     * @param row that was modified in database
     * @since 3.0
     * @throws OperationException if error
     */
    protected abstract void notifyCacheModified(R row) throws OperationException;
}
