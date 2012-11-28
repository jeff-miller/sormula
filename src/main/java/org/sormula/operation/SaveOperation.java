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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.sormula.Table;
import org.sormula.annotation.cascade.Cascade;
import org.sormula.annotation.cascade.OneToManyCascade;
import org.sormula.annotation.cascade.OneToOneCascade;
import org.sormula.annotation.cascade.SaveCascade;
import org.sormula.annotation.cascade.SaveCascadeAnnotationReader;
import org.sormula.log.ClassLogger;
import org.sormula.operation.cascade.CascadeOperation;
import org.sormula.operation.cascade.SaveCascadeOperation;
import org.sormula.reflect.SormulaField;


/**
 * SQL update or insert operation for row of type R. Rows are updated if they exist
 * in the database or inserted if they are new rows. Cascades are performed based upon
 * {@link Cascade#saves()}, {@link OneToManyCascade#saves()}, or {@link OneToOneCascade#saves()}.
 * <p>
 * Inserts and updates are performed by class members of type {@link InsertOperation} 
 * and {@link UpdateOperation}. Since most of the work is delegated to {@link InsertOperation} 
 * and {@link UpdateOperation}, not all methods of base class {@link ModifyOperation} are
 * used. All of the methods in SaveOperation may be safely overridden but some subclass methods
 * may not be used. 
 *  
 * @param <R> class type which contains members for columns of a row in a table
 * 
 * @since 1.1
 * @author Jeff Miller
 */
public class SaveOperation<R> extends ModifyOperation<R>
{
	private static ClassLogger log = new ClassLogger();
    InsertOperation<R> insertOperation;
    UpdateOperation<R> updateOperation;
    boolean invokeSuper;
    
    
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
        
        insertOperation = new InsertOperation<R>(table)
		{
			@Override
			protected List<CascadeOperation<R, ?>> prepareCascades(Field field) throws OperationException 
			{
				// delegate to SaveOperation so that cascades are save operations instead of insert operations
				return SaveOperation.this.prepareCascades(field);
			}
			
			@Override
			protected void preExecute(R row) throws OperationException 
			{
				// delegate to SaveOperation so that it may be overriden by subclass of SaveOperation
				invokeSuper = false;
				SaveOperation.this.preExecute(row); // if subclass overrides then invokeSuper remains false
				if (invokeSuper) super.preExecute(row);
			}

			@Override
			protected void postExecute(R row) throws OperationException 
			{
				// delegate to SaveOperation so that it may be overriden by subclass of SaveOperation
				invokeSuper = false;
				SaveOperation.this.postExecute(row); // if subclass overrides then invokeSuper remains false
				if (invokeSuper) super.postExecute(row);
			}

			@Override
			protected void preExecuteCascade(R row) throws OperationException 
			{
				// delegate to SaveOperation so that it may be overriden by subclass of SaveOperation
				invokeSuper = false;
				SaveOperation.this.preExecuteCascade(row); // if subclass overrides then invokeSuper remains false
				if (invokeSuper) super.preExecuteCascade(row);			
			}

			@Override
			protected void postExecuteCascade(R row) throws OperationException 
			{
				// delegate to SaveOperation so that it may be overriden by subclass of SaveOperation
				invokeSuper = false;
				SaveOperation.this.postExecuteCascade(row); // if subclass overrides then invokeSuper remains false
				if (invokeSuper) super.postExecuteCascade(row);
			}
		};
        
		updateOperation = new UpdateOperation<R>(table)
		{
			@Override
			protected List<CascadeOperation<R, ?>> prepareCascades(Field field) throws OperationException 
			{
				// delegate to SaveOperation so that cascades are save operations instead of update operations
				return SaveOperation.this.prepareCascades(field);
			}

			@Override
			protected void preExecute(R row) throws OperationException 
			{
				// delegate to SaveOperation so that it may be overriden by subclass of SaveOperation
				invokeSuper = false;
				SaveOperation.this.preExecute(row); // if subclass overrides then invokeSuper remains false
				if (invokeSuper) super.preExecute(row);
			}

			@Override
			protected void postExecute(R row) throws OperationException 
			{
				// delegate to SaveOperation so that it may be overriden by subclass of SaveOperation
				invokeSuper = false;
				SaveOperation.this.postExecute(row); // if subclass overrides then invokeSuper remains false
				if (invokeSuper) super.postExecute(row);
			}

			@Override
			protected void preExecuteCascade(R row) throws OperationException 
			{
				// delegate to SaveOperation so that it may be overriden by subclass of SaveOperation
				invokeSuper = false;
				SaveOperation.this.preExecuteCascade(row); // if subclass overrides then invokeSuper remains false
				if (invokeSuper) super.preExecuteCascade(row);
			}

			@Override
			protected void postExecuteCascade(R row) throws OperationException 
			{
				// delegate to SaveOperation so that it may be overriden by subclass of SaveOperation
				invokeSuper = false;
				SaveOperation.this.postExecuteCascade(row); // if subclass overrides then invokeSuper remains false
				if (invokeSuper) super.postExecuteCascade(row);
			}
		};
        updateOperation.setWhere(whereConditionName);
    }


    @Override
    public void close() throws OperationException
    {
        insertOperation.close();
        updateOperation.close();
    }


    /**
     * Saves a row. Set parameters, executes, closes. 
     * Alias for {@link #modify(Object)}.
     * 
     * @param row row to use for parameters
     * @return {@link #getRowsAffected()}
     * @throws OperationException if error
     * @since 1.4
     */
    public int save(R row) throws OperationException
    {
        return super.modify(row);
    }


    /**
     * Saves all rows in collection. Set parameters, executes, closes. 
     * Alias for {@link #modifyAll(Collection)}.
     * 
     * @param rows collection of rows to use as parameters 
     * @return {@link #getRowsAffected()}
     * @throws OperationException if error
     * @since 1.4
     */
    public int saveAll(Collection<R> rows) throws OperationException
    {
        return super.modifyAll(rows);
    }


    /**
     * Saves rows based upon parameters. Set parameters, executes, closes. 
     * Alias for {@link #modify(Object...)}.
     * 
     * @param parameters operation parameters as objects (see {@link #setParameters(Object...)})
     * @return count of rows affected
     * @throws OperationException if error
     * @since 1.4
     */
    public int save(Object... parameters) throws OperationException
    {
        return super.modify(parameters);
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


    /**
     * Creates save cascade operations based upon the save annotations for field.
     *
     * @param field annotation is for this field of row class R
     * @return list of save cascade operations; empty list for none
     * @throws OperationException if error
     * @since 1.9.3 and 2.3.3
     */
    @Override
    protected List<CascadeOperation<R, ?>> prepareCascades(Field field) throws OperationException
    {
        List<CascadeOperation<R, ?>> co = null;
        SaveCascadeAnnotationReader scar = new SaveCascadeAnnotationReader(field);
        SaveCascade[] saveCascades = scar.getSaveCascades();
        
        if (saveCascades.length > 0)
        {
            // at least one save cascade
            if (log.isDebugEnabled()) log.debug("prepareCascades() for " + field.getName());
            Table<?> targetTable = getTargetTable(scar.getTargetClass(), field);
            SormulaField<R, ?> targetField = createTargetField(field);
            co = new ArrayList<CascadeOperation<R, ?>>(saveCascades.length);
            
            // for each cascade operation
            for (SaveCascade c: saveCascades)
            {
                if (log.isDebugEnabled()) log.debug("prepare cascade " + c.operation());
                @SuppressWarnings("unchecked") // target field type is not known at compile time
                CascadeOperation<R, ?> operation = new SaveCascadeOperation(targetField, targetTable, c);
                operation.prepare();
                co.add(operation);
            }
        }
        else
        {
            // no cascades
            co = Collections.emptyList();
        }
        
        return co;
    }
	
    
    /**
     * {@inheritDoc}
     */
	@Override
	protected void preExecute(R row) throws OperationException 
	{
		// indicate to invoke InsertOperation/UpdateOperation equivalent method
		invokeSuper = true;
	}

	
    /**
     * {@inheritDoc}
     */
	@Override
	protected void postExecute(R row) throws OperationException 
	{
		// indicate to invoke InsertOperation/UpdateOperation equivalent method
		invokeSuper = true;
	}

	
    /**
     * {@inheritDoc}
     */
	@Override
	protected void preExecuteCascade(R row) throws OperationException 
	{
		// indicate to invoke InsertOperation/UpdateOperation equivalent method
		invokeSuper = true;
	}


    /**
     * {@inheritDoc}
     */
	@Override
	protected void postExecuteCascade(R row) throws OperationException 
	{
		// indicate to invoke InsertOperation/UpdateOperation equivalent method
		invokeSuper = true;
	}
}
