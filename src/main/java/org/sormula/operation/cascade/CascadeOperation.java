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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.sormula.Table;
import org.sormula.annotation.cascade.InsertCascade;
import org.sormula.operation.OperationException;
import org.sormula.operation.SqlOperation;
import org.sormula.reflect.ReflectException;
import org.sormula.reflect.SormulaField;
import org.sormula.translator.ColumnTranslator;
import org.sormula.translator.RowTranslator;


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
    String[] foreignKeyValueFieldNames;
    String foreignKeyReferenceFieldName; // TODO
    S sourceRow;
    List<SormulaField<S, Object>> sourceKeyFieldList;
    List<SormulaField<T, Object>> targetForeignKeyFieldList;
    SormulaField<T, Object> targetForeignReferenceField; // TODO
    int keyFieldCount;
    
    
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
     * is to be performed before source row operation
     * @since 3.0
     */
    public CascadeOperation(Table<S> sourceTable, SormulaField<S, ?> targetField, Table<T> targetTable, Class <?> cascadeOperationClass)
    {
        this.sourceTable = sourceTable;
        this.targetField = targetField;
        this.targetTable = targetTable;
        this.cascadeOperationClass = cascadeOperationClass;
    }

	
    /**
     * Sets when to perform the cascade.
     * 
     * @param post true if cascade is to be performed after select/execute; 
     *             false if cascade is to be performed before select/execute
     * @since 3.0
     */
	public void setPost(boolean post)
    {
        this.post = post;
    }


    /**
	 * @return true if cascade is to be performed after select/execute; 
	 *         false if cascade is to be performed before select/execute
	 */
	public boolean isPost()
	{
	    return post;
	}
	

	/**
	 * TODO
	 * @return
	 * @since 3.0
	 */
	public String[] getForeignKeyFieldNames()
    {
        return foreignKeyValueFieldNames;
    }


	/**
	 * TODO
	 * @param foreignKeyFieldNames
	 * @since 3.0
	 */
    public void setForeignKeyFieldNames(String[] foreignKeyFieldNames)
    {
        this.foreignKeyValueFieldNames = foreignKeyFieldNames;
    }


    /**
     * TODO
     * @return
     * @since 3.0
     */
    public String getForeignKeyReferenceFieldName()
    {
        return foreignKeyReferenceFieldName;
    }


    /**
     * TODO
     * @param foreignKeyReferenceFieldName
     * @since 3.0
     */
    public void setForeignKeyReferenceFieldName(String foreignKeyReferenceFieldName)
    {
        this.foreignKeyReferenceFieldName = foreignKeyReferenceFieldName;
    }


    /**
	 * Gets number of key fields. The number of key fields is the number of primary key
	 * fields in the source row which is also the same as the number of foreign key fields
	 * in target row.
	 *  
	 * @return count of key fields used in foreign key mapping
	 * @since 3.0
	 */
    public int getKeyFieldCount()
    {
        return keyFieldCount;
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
     * Prepares operation by initializing JDBC statements. This method prepares foreign
     * key mapping with {@link #prepareForeignKeyMapping()}. Subclasses should override
     * to perform additional preparation.
     * 
     * @throws OperationException if error
     */
    public void prepare() throws OperationException
    {
        prepareForeignKeyMapping();
    }
    
    
    
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

    
    /**
     * Prepares accessors that will set foreign key(s) on cascaded target rows as defined by
     * {@link InsertCascade#foreignKeyValueFields()}.
     * 
     * @throws OperationException if error
     * @since 3.0
     */
    protected void prepareForeignKeyMapping() throws OperationException
    {
        if (foreignKeyValueFieldNames.length > 0)
        {
            // at least one foreign key in target
            List<ColumnTranslator<S>> sourceKeyColumnTranslators = getSourceTable().
                    getRowTranslator().getPrimaryKeyWhereTranslator().getColumnTranslatorList();
            RowTranslator<T> targetRowTranslator = getTargetTable().getRowTranslator();
            
            // parallel lists mapping source primary key(s) to target foreign key(s)
            sourceKeyFieldList = new ArrayList<SormulaField<S,Object>>(sourceKeyColumnTranslators.size());
            targetForeignKeyFieldList = new ArrayList<SormulaField<T, Object>>(sourceKeyColumnTranslators.size());
            
            try
            {
                // for all source primary key(s)
                int foreignKeyNameIndex = 0;
                boolean sametargetAndSourceNames = foreignKeyValueFieldNames[0].equals("*");
                for (ColumnTranslator<S> sct : sourceKeyColumnTranslators)
                {
                    String targetFieldName;
                    if (sametargetAndSourceNames) targetFieldName = sct.getField().getName();
                    else                          targetFieldName = foreignKeyValueFieldNames[foreignKeyNameIndex++];
                    
                    ColumnTranslator<T> tct = targetRowTranslator.getColumnTranslator(targetFieldName);
                    if (tct != null)
                    {
                        // target foreign key field exists for corresponding source primary key field
                        // add each to parallel lists
                        sourceKeyFieldList.add(new SormulaField<S, Object>(sct.getField()));
                        targetForeignKeyFieldList.add(new SormulaField<T, Object>(tct.getField()));
                    }
                    else
                    {
                        throw new OperationException(targetFieldName + " does not exist in " + getTargetTable().getRowClass() + 
                                " as specified with InsertCascade in " + sourceTable.getRowClass());
                    }
                }
            }
            catch (ReflectException e)
            {
                throw new OperationException("error creating method access for InsertCascade", e);
            }
            
            keyFieldCount = sourceKeyFieldList.size();
        }
    }
    
    
    /**
     * Sets the foreign key(s) in target (child) row from primary key(s) in source (parent) row.
     * 
     * @param row target (child) row to affect
     * @throws OperationException if error
     * @since 3.0
     */
    protected void setForeignKeyValues(T row) throws OperationException
    {
        try
        {
            // for each key within a row
            for (int i = 0; i < keyFieldCount; ++i)
            {
                // set source key on target row foreign key field
                Object sourceKey = sourceKeyFieldList.get(i).invokeGetMethod(sourceRow);
                targetForeignKeyFieldList.get(i).invokeSetMethod(row, sourceKey);
            }
        }
        catch (ReflectException e)
        {
            throw new OperationException("TODO", e);
        }
    }


    /**
     * Sets the foreign key(s) in target (child) rows from primary key(s) in source (parent) row.
     * 
     * @param rows target (child) rows to affect
     * @throws OperationException if error
     * @since 3.0
     */
    protected void setForeignKeyValues(Collection<T> rows) throws OperationException
    {
        if (keyFieldCount > 0)
        {
            // foreign key mapping is desired
            for (T t : rows) setForeignKeyValues(t);
        }
    }
}
