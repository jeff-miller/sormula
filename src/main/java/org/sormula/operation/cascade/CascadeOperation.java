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
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.sormula.Table;
import org.sormula.annotation.cascade.Cascade;
import org.sormula.annotation.cascade.OneToManyCascade;
import org.sormula.annotation.cascade.OneToOneCascade;
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
public abstract class CascadeOperation<S, T> implements AutoCloseable
{
    Table<S> sourceTable;
    SormulaField<S, ?> targetField;
    Table<T> targetTable;
    Class <?> cascadeOperationClass;
    boolean post;
    String[] foreignKeyValueFieldNames;
    String foreignKeyReferenceFieldName;
    S sourceRow;
    List<SormulaField<S, Object>> sourceKeyFieldList;
    List<SormulaField<T, Object>> targetForeignKeyFieldList;
    SormulaField<T, Object> targetForeignReferenceField;
    int keyFieldCount;
    String[] requiredCascades;
    
    
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
	 * Gets the field key value field names in target (child) rows.
	 * 
	 * @return target (child) row foreign key fields; null means don't update foreign key values
	 * @since 3.0
	 */
	public String[] getForeignKeyFieldNames()
    {
        return foreignKeyValueFieldNames;
    }


    /**
     * Sets the name of fields that contain the foreign key values in target (child) rows.
     * If length of array parameter is zero, then null is used so that null means that no
     * foreign key fields are defined.
     * 
     * @param foreignKeyFieldNames field names from cascade annotation foreignKeyValueFields
     * @since 3.0
     * @see Cascade#foreignKeyValueFields()
     * @see OneToManyCascade#foreignKeyValueFields()
     * @see OneToOneCascade#foreignKeyValueFields()
     */
    public void setForeignKeyFieldNames(String[] foreignKeyFieldNames)
    {
        if (foreignKeyFieldNames != null && foreignKeyFieldNames.length > 0) 
            this.foreignKeyValueFieldNames = foreignKeyFieldNames;
        else 
            this.foreignKeyReferenceFieldName = null;
    }


    /**
     * Gets the name of the foreign key reference field in the target (child) rows.
     * 
     * @return name; null means dont set reference
     * @since 3.0
     */
    public String getForeignKeyReferenceFieldName()
    {
        return foreignKeyReferenceFieldName;
    }


    /**
     * Sets the field name that contains a reference to the foreign key object in target (child) rows.
     * If length of parameter is zero, then null is used so that null means that no
     * foreign key reference field is defined.
     * 
     * @param foreignKeyReferenceFieldName field name of foreign key reference from cascade 
     * annotation foreignKeyReferenceField
     * 
     * @since 3.0
     * @see Cascade#foreignKeyReferenceField()
     * @see OneToManyCascade#foreignKeyReferenceField()
     * @see OneToOneCascade#foreignKeyReferenceField()
     */
    public void setForeignKeyReferenceFieldName(String foreignKeyReferenceFieldName)
    {
        if (foreignKeyReferenceFieldName != null && foreignKeyReferenceFieldName.length() > 0)
            this.foreignKeyReferenceFieldName = foreignKeyReferenceFieldName;
        else
            this.foreignKeyReferenceFieldName = null;
    }
    
    
    /**
     * Sets required cascade names to use. See {@link SqlOperation#setRequiredCascades(String...)} for 
     * details.
     * 
     * @param cascadeNames names of cascades that will be executed 
     * @since 3.0
     */
    public void setRequiredCascades(String... cascadeNames)
    {
        requiredCascades = cascadeNames;
    }
    
    
    /**
     * Gets required cascade names set with {@link #setRequiredCascades(String...)}.
     * 
     * @return names of cascades that will be executed
     * @since 3.0
     */
    public String[] getRequiredCascades()
    {
        return requiredCascades;
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
     * key mapping with {@link #prepareForeignKeyValueFields()}. Subclasses should override
     * to perform additional preparation.
     * 
     * @throws OperationException if error
     */
    public void prepare() throws OperationException
    {
        prepareForeignKeyValueFields();
        prepareForeignKeyReferenceField();
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
            operation.setRequiredCascades(requiredCascades);
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
     * {@link Cascade#foreignKeyValueFields()}.
     * 
     * @throws OperationException if error
     * @since 3.0
     */
    protected void prepareForeignKeyValueFields() throws OperationException
    {
        if (foreignKeyValueFieldNames != null)
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
                boolean sameTargetAndSourceNames = foreignKeyValueFieldNames[0].equals("#");
                for (ColumnTranslator<S> sct : sourceKeyColumnTranslators)
                {
                    String targetFieldName;
                    if (sameTargetAndSourceNames) targetFieldName = sct.getField().getName();
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
                                " as specified with Cascade.foreignKeyValueFields in " + sourceTable.getRowClass());
                    }
                }
            }
            catch (ReflectException e)
            {
                throw new OperationException("error creating method access for Cascade", e);
            }
            
            keyFieldCount = sourceKeyFieldList.size();
        }
    }

    
    /**
     * Prepares accessor that will set foreign key reference on cascaded target rows as defined by
     * {@link Cascade#foreignKeyReferenceField()}.
     * 
     * @throws OperationException if error
     * @since 3.0
     */
    protected void prepareForeignKeyReferenceField() throws OperationException
    { 
        if (foreignKeyReferenceFieldName != null)
        {
            String targetFieldName;
            if (foreignKeyReferenceFieldName.equals("class"))
            {
                // foreign key field is simple class name
                String classSimpleName = getSourceTable().getRowClass().getSimpleName();
                targetFieldName = classSimpleName.substring(0, 1).toLowerCase() +
                        classSimpleName.substring(1);
            }
            else
            {
                // foreign key field is specified
                targetFieldName = foreignKeyReferenceFieldName;
            }
            
            try
            {
                Field field = getTargetTable().getRowTranslator().getDeclaredField(targetFieldName);
                targetForeignReferenceField = new SormulaField<T, Object>(field);
            }
            catch (ReflectException e)
            {
                throw new OperationException("error creating method reference to field " + 
                        targetFieldName + " in " + getTargetTable().getRowClass(), e);
            }
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
            throw new OperationException("error setting foreign key value", e);
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
    
    
    /**
     * Sets the foreign key reference in target (child) row as source (parent) row.
     * 
     * @param row target (child) row to affect
     * @throws OperationException if error
     * @since 3.0
     */
    protected void setForeignKeyReference(T row) throws OperationException
    {
        if (targetForeignReferenceField != null)
        try
        {
            // set reference on target row 
            targetForeignReferenceField.invokeSetMethod(row, sourceRow);
        }
        catch (ReflectException e)
        {
            throw new OperationException("error setting foreign key reference", e);
        }
    }


    /**
     * Sets the foreign key reference in target (child) rows as source (parent) row.
     * 
     * @param rows target (child) rows to affect
     * @throws OperationException if error
     * @since 3.0
     */
    protected void setForeignKeyReference(Collection<T> rows) throws OperationException
    {
        if (targetForeignReferenceField != null)
        {
            // foreign key mapping is desired
            for (T t : rows) setForeignKeyReference(t);
        }
    }
}
