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

import java.lang.reflect.Field;
import java.util.List;

import org.sormula.Table;
import org.sormula.operation.cascade.CascadeOperation;


/**
 * SQL update or insert operation for row of type R. Rows are updated if they exist
 * in the database or inserted if they are new rows.
 *
 * @param <R> class type which contains members for columns of a row in a table
 * 
 * @since 1.1
 * @author Jeff Miller
 */
public class SaveOperation<R> extends ModifyOperation<R>
{
    InsertOperation<R> insertOperation;
    UpdateOperation<R> updateOperation;
    
    
    /**
     * Constructs to update by primary key or insert if update fails.
     * 
     * @param table update/insert to this table
     * @throws OperationException if error
     */
    public SaveOperation(Table<R> table) throws OperationException
    {
        this(table, "primaryKey");
    }
    
    
    /**
     * Constructs to update by where condition or insert if update fails.
     * 
     * @param table update/insert to this table
     * @param whereConditionName name of where condition to use for update ("primaryKey" to update
     * by primary key; empty string to update all rows in table)
     * @throws OperationException if error
     */
    public SaveOperation(Table<R> table, String whereConditionName) throws OperationException
    {
        super(table);
        insertOperation = new InsertOperation<R>(table);
        updateOperation = new UpdateOperation<R>(table);
        updateOperation.setWhere(whereConditionName);
    }


    @Override
    public void close() throws OperationException
    {
        insertOperation.close();
        updateOperation.close();
    }


    @Override
    public void execute() throws OperationException
    {
        int allRowsAffected = 0; 
        
        try
        {
            if (rows != null)
            {
                // operation parameters from rows
                for (R row: rows)
                {
                    // try update first
                    updateOperation.setRow(row);
                    updateOperation.execute();

                    if (updateOperation.getRowsAffected() == 1)
                    {
                        // update succeeded
                        ++allRowsAffected;
                    }
                    else
                    {
                        // update did not suceed, assume record does not exist
                        insertOperation.setRow(row);
                        insertOperation.execute();
                        allRowsAffected += insertOperation.getRowsAffected();
                    }
                }
            }
            else if (getParameters() != null)
            {
                // operation parameters from objects (not typical)
                
                // try update first
                updateOperation.setParameters(getParameters());
                updateOperation.execute();

                if (updateOperation.getRowsAffected() == 1)
                {
                    // update succeeded
                    ++allRowsAffected;
                }
                else
                {
                    // update did not suceed, assume record does not exist
                    insertOperation.setParameters(getParameters());
                    insertOperation.execute();
                    allRowsAffected += insertOperation.getRowsAffected();
                }
            }
        }
        catch (Exception e)
        {
            throw new OperationException("execute() error", e);
        }
        
        setRowsAffected(allRowsAffected);
    }


    @Override
    protected List<CascadeOperation<R, ?>> prepareCascades(Field field) throws OperationException
    {
        // cascades are handled by insert and update operation
        return null;
    }
}
