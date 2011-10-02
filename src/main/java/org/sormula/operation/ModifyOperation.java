/* sormula - Simple object relational mapping
 * Copyright (C) 2011 Jeff Miller
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
import java.util.Collection;
import java.util.Map;

import org.sormula.Table;
import org.sormula.annotation.cascade.DeleteCascade;
import org.sormula.annotation.cascade.InsertCascade;
import org.sormula.annotation.cascade.UpdateCascade;
import org.sormula.log.ClassLogger;


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
     * Executes operation for all row parameters using current prepared statement.
     * {@link #getRowsAffected()} will return the sum of all rows affected.
     * 
     * @throws OperationException if error
     */
    @Override
    public void execute() throws OperationException
    {
        prepareCheck();
        int allRowsAffected = 0; 
        
        try
        {
            if (rows != null)
            {
                // operation parameters from rows
                for (R row: rows)
                {
                    if (log.isDebugEnabled()) log.debug("prepare parameters from row=" + row);
                    setNextParameter(1);
                    preExecuteCascade(row);
                    preExecute(row);
                    prepareColumns(row);
                    prepareWhere(row);
                    if (log.isDebugEnabled()) log.debug("execute update row=" + row);
                    allRowsAffected += getPreparedStatement().executeUpdate();
                    postExecute(row);
                    postExecuteCascade(row);
                }
            }
            else if (getParameters() != null)
            {
                // operation parameters from objects
                if (log.isDebugEnabled()) log.debug("prepare parameters from objects");
                setNextParameter(1);
                prepareParameters();
                if (log.isDebugEnabled()) log.debug("execute update");
                allRowsAffected = getPreparedStatement().executeUpdate();
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
     * Invoked prior to JDBC execute. Override to modify the row prior to JDBC execute.
     * Default implementaion does nothing. This method is not invoked when 
     * {@linkplain ModifyOperation#setParameters(Object...)} is used since no row is available.
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
     * {@linkplain ModifyOperation#setParameters(Object...)} is used since no row is available. 
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
