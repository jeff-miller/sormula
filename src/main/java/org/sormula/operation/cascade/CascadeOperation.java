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
package org.sormula.operation.cascade;

import java.lang.reflect.Constructor;

import org.sormula.Table;
import org.sormula.operation.OperationException;
import org.sormula.operation.SqlOperation;
import org.sormula.reflect.SormulaField;


/**
 * Base class for all cascade operations.
 * 
 * @author Jeff Miller
 *
 * @param <S> row class of table that is source of cascade
 * @param <T> row class of table that is target of cascade 
 */
public abstract class CascadeOperation<S, T>
{
    Table<S> sourceTable;
    SormulaField<S, ?> targetField;
    Table<T> targetTable;
    Class <?> cascadeOperationClass;
    boolean post;
    S sourceRow;
    
    
    /**
     * Constructs for field and table to be affected by cascade.
     * 
     * @param targetField in source row to be affected by cascade operation
     * @param targetTable sorm table that will be cascaded
     * @param cascadeOperationClass class of cascade operation (used to create new instance)
     * @param post true if operation is to be performed after source row operation; false if operation
     * is to be performed before source row operation
     */
    @Deprecated // use constructor with source table
	public CascadeOperation(SormulaField<S, ?> targetField, Table<T> targetTable, Class <?> cascadeOperationClass, boolean post)
    {
        this.targetField = targetField;
        this.targetTable = targetTable;
        this.cascadeOperationClass = cascadeOperationClass;
        this.post = post;
    }
    
    
    /**
     * Constructs for field and table to be affected by cascade.
     * 
     * @param sourceTable cascade orgininates on row from this table
     * @param targetField in source row to be affected by cascade operation
     * @param targetTable sorm table that will be cascaded
     * @param cascadeOperationClass class of cascade operation (used to create new instance)
     * @param post true if operation is to be performed after source row operation; false if operation
     * is to be performed before source row operation
     * @since 3.0
     */
    public CascadeOperation(Table<S> sourceTable, SormulaField<S, ?> targetField, Table<T> targetTable, Class <?> cascadeOperationClass, boolean post)
    {
        this.sourceTable = sourceTable;
        this.targetField = targetField;
        this.targetTable = targetTable;
        this.cascadeOperationClass = cascadeOperationClass;
        this.post = post;
    }

	
	/**
	 * @return true if cascade is to be performed after select/execute; 
	 *        false if cascade is to be performed before select/execute
	 */
	public boolean isPost()
	{
	    return post;
	}
	

    /**
     * Performs cascade operation. Retains value of source row parameter. Subclasses should
     * invoke super.cascade() and then implement their specific behavior.
     * 
	 * @param sourceRow row in parent table that was source the cascade
	 * @throws OperationException if error
	 */
    public void cascade(S sourceRow) throws OperationException
    {
        this.sourceRow = sourceRow;
    }

    
    /**
     * Prepares operation by initializing JDBC statements.
     * 
     * @throws OperationException if error
     */
    public abstract void prepare() throws OperationException;
    
    
    /**
     * Cleans up by closing any JDBC resources.
     * 
     * @throws OperationException if error
     */
    public abstract void close() throws OperationException;
    

    /**
     * Gets {@link Table} that originates (is source of) cascade.
     * 
     * @return parent (source) table of cascade
     * @since 3.0
     */
    public Table<S> getSourceTable()
    {
        return sourceTable;
    }


    /**
     * Gets target field as {@link SormulaField}.
     * 
     * @return field in source row to be affected by cascade operation
     */
    public SormulaField<S, ?> getTargetField()
    {
        return targetField;
    }


    /**
     * @return table to be used in cascade
     */
    public Table<T> getTargetTable()
    {
        return targetTable;
    }
    
    
    /**
     * Source row of cascade set by {@link #cascade(Object)}.
     * 
     * @return row where cascade originates
     * @since 3.0
     */
    public S getSourceRow()
    {
        return sourceRow;
    }


    /**
     * Creates new instance of sql operation from {@link #cascadeOperationClass} supplied in the 
     * constructor.
     */
    protected SqlOperation<?> createOperation() throws OperationException
    {
        SqlOperation<?> operation = null;
        
        try
        {
            Constructor<?> constructor = cascadeOperationClass.getConstructor(Table.class);
            operation = (SqlOperation<?>)constructor.newInstance(getTargetTable());
        }
        catch (NoSuchMethodException e)
        {
            throw new OperationException("no constructor for " + cascadeOperationClass.getCanonicalName() +
                    " for field " + getTargetField().getField().getName());
        }
        catch (Exception e)
        {
            throw new OperationException("error constructing " + cascadeOperationClass.getCanonicalName() +
                    " for field " + getTargetField().getField().getName());
        }
        
        return operation;
    }
}
