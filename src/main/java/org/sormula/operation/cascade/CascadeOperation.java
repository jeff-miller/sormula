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
import java.util.Map;

import org.sormula.Table;
import org.sormula.annotation.cascade.Cascade;
import org.sormula.annotation.cascade.OneToManyCascade;
import org.sormula.annotation.cascade.OneToOneCascade;
import org.sormula.log.ClassLogger;
import org.sormula.operation.MissingFieldException;
import org.sormula.operation.OperationException;
import org.sormula.operation.SqlOperation;
import org.sormula.reflect.ReflectException;
import org.sormula.reflect.RowField;
import org.sormula.translator.ColumnTranslator;
import org.sormula.translator.RowTranslator;
import org.sormula.translator.TranslatorException;


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
    private static final ClassLogger log = new ClassLogger();
    SqlOperation<?> sqlOperation;
    SqlOperation<S> sourceOperation;
    @Deprecated Table<S> sourceTable; // remove when deprecated constructor CascadeOperation(Table<S>...) is removed
    RowField<S, ?> targetField;
    Table<T> targetTable;
    Class <?> cascadeSqlOperationClass;
    boolean post;
    String[] foreignKeyValueFieldNames;
    String foreignKeyReferenceFieldName;
    S sourceRow;
    List<RowField<S, Object>> sourceKeyFieldList;
    List<RowField<T, Object>> targetForeignKeyValueFieldList;
    RowField<T, Object> targetForeignReferenceField;
    int keyFieldCount;
    @Deprecated String[] requiredCascades; // remove when deprecated constructor CascadeOperation(Table<S>...) is removed
    @Deprecated Map<String, Object> namedParameterMap; // remove when deprecated constructor CascadeOperation(Table<S>...) is removed
    @Deprecated int depth; // remove when deprecated constructor CascadeOperation(Table<S>...) is removed
    
    
    /**
     * Constructs from source operation and targets of the cascade.
     * 
     * @param sourceOperation operation where cascade originates 
     * @param targetField in source row to be affected by cascade operation
     * @param targetTable sormula table that will be cascaded
     * @param cascadeSqlOperationClass class of cascade operation (used to create new instance)
     * is to be performed before source row operation
     * @since 4.1
     */
    public CascadeOperation(SqlOperation<S> sourceOperation, RowField<S, ?> targetField, Table<T> targetTable, Class <?> cascadeSqlOperationClass)
    {
        this.sourceOperation = sourceOperation;
        this.targetField = targetField;
        this.targetTable = targetTable;
        this.cascadeSqlOperationClass = cascadeSqlOperationClass;
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
     * Gets the depth of the cascade graph relative to the root cascade operation. Root
     * cascade operation is depth 0.
     * 
     * @return depth of this cascade 
     * @since 4.1
     */
    public int getDepth() 
    {
        return sourceOperation.getCascadeDepth() + 1;
    }


    /**
     * Sets the cascade depth. Typically set by {@link SqlOperation} when preparing cascades.
     * 
     * @param depth 0..n
     * @since 4.1
     * @deprecated no need to set depth since it is always source operation level + 1
     */
    @Deprecated
    public void setDepth(int depth) 
    {
        this.depth = depth;
    }


    /**
     * Gets the field key value field names in target (child) rows.
     * 
     * @return target (child) row foreign key fields; null means don't update foreign key values
     * @since 3.1
     */
    public String[] getForeignKeyValueFieldNames()
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
            this.foreignKeyValueFieldNames = null;
    }


    /**
     * Gets the name of the foreign key reference field in the target (child) rows.
     * 
     * @return name; null means don't set reference
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
     * @deprecated no longer needed since determined by {@link SqlOperation#getRequiredCascades()} of source operation
     */
    @Deprecated
    public void setRequiredCascades(String... cascadeNames)
    {
        requiredCascades = cascadeNames;
    }
    
    
    /**
     * If constructed with {@link #CascadeOperation(Table, RowField, Table, Class)} then
     * gets required cascade names set with {@link #setRequiredCascades(String...)}.
     * <p>
     * If constructed with {@link #CascadeOperation(SqlOperation, RowField, Table, Class)} then
     * returns source operation {@link SqlOperation#getRequiredCascades()}.
     * 
     * @return names of cascades that will be executed
     * @since 3.0
     */
    public String[] getRequiredCascades()
    {
        return sourceOperation.getRequiredCascades();
    }


    /**
	 * Gets number of key fields. The number of key fields is the number of primary key
	 * fields in the source row which is also the same as the number of foreign key fields
	 * in target row.
	 *  
	 * @return count of key fields used in foreign key mapping; zero if no foreign key mapping
	 * @since 3.0
	 */
    public int getKeyFieldCount()
    {
        return keyFieldCount;
    }
    

    /**
     * Gets the map of named parameters.
     * 
     * @return map of name to value or null if no named parameters
     * @since 3.1
     * @see SqlOperation#getParameter(String)
     * @see SqlOperation#setParameter(String, Object)
     * @see SqlOperation#getNamedParameterMap()
     */
    public Map<String, Object> getNamedParameterMap()
    {
        return sourceOperation.getNamedParameterMap();
    }


    /**
     * Sets the map of named parameters. 
     * 
     * @param namedParameterMap map of name to value or null if no named parameters
     * @since 3.1
     * @see SqlOperation#getParameter(String)
     * @see SqlOperation#setParameter(String, Object)
     * @see SqlOperation#getNamedParameterMap()
     * @deprecated no longer needed since determined by {@link SqlOperation#getNamedParameterMap()} of source operation
     */
    @Deprecated
    public void setNamedParameterMap(Map<String, Object> namedParameterMap)
    {
        this.namedParameterMap = namedParameterMap;
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
     * Gets operation that triggered this cascade.
     * 
     * @return source operation or null if deprecated constructor was used to create this 
     * @since 4.1
     */
    public SqlOperation<S> getSourceOperation() 
    {
        return sourceOperation;
    }


    /**
     * Gets {@link Table} that originates (is source of) cascade.
     * 
     * @return parent (source) table of cascade
     * @since 3.0
     */
    public Table<S> getSourceTable()
    {
        return sourceOperation.getTable();
    }


    /**
     * Gets target field as {@link RowField}.
     * 
     * @return field in source row to be affected by cascade operation
     */
    public RowField<S, ?> getTargetField()
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
     * Creates new instance of sql operation from {@link #cascadeSqlOperationClass} supplied in the 
     * constructor.
     * @return new instance of {@link SqlOperation} that will be used for cascade
     * @throws OperationException if error
     */
    protected SqlOperation<?> createOperation() throws OperationException
    {
        sqlOperation = null;
        
        try
        {
            Constructor<?> constructor = cascadeSqlOperationClass.getConstructor(Table.class);
            sqlOperation = (SqlOperation<?>)constructor.newInstance(getTargetTable());
        }
        catch (NoSuchMethodException e)
        {
            throw new OperationException("no constructor for " + cascadeSqlOperationClass.getCanonicalName() +
                    " for field " + getTargetField().getField().getName());
        }
        catch (Exception e)
        {
            throw new OperationException("error constructing " + cascadeSqlOperationClass.getCanonicalName() +
                    " for field " + getTargetField().getField().getName());
        }
        
        return sqlOperation;
    }

    
    /**
     * Sets the sql operation attributes that are the same as source attributes for 
     * all levels. Cascade depth is also set as 1 + source depth.
     * 
     * @since 4.1
     */
    protected void deriveSqlOperationAttributes()
    {
        sqlOperation.setCascadeDepth(getDepth());
        sqlOperation.setRequiredCascades(getRequiredCascades());
        sqlOperation.setNamedParameterMap(getNamedParameterMap());
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
            RowTranslator<S> sourceRowTranslator = getSourceTable().getRowTranslator();
            RowTranslator<T> targetRowTranslator = getTargetTable().getRowTranslator();
            List<ColumnTranslator<S>> sourceKeyColumnTranslators = sourceRowTranslator.
                    getPrimaryKeyWhereTranslator().getColumnTranslatorList();
            
            // parallel lists mapping source primary key(s) to target foreign key(s)
            sourceKeyFieldList = new ArrayList<RowField<S,Object>>(sourceKeyColumnTranslators.size());
            targetForeignKeyValueFieldList = new ArrayList<RowField<T, Object>>(sourceKeyColumnTranslators.size());
            
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
                        if (log.isDebugEnabled())
                        {
                            log.debug(sct.getField() + " maps to foreign key " + tct.getField());
                        }
                        
                        @SuppressWarnings("unchecked")
                        RowField<S, Object> sourceKeyRowField = (RowField<S, Object>)sourceRowTranslator.createRowField(sct.getField());
                        sourceKeyFieldList.add(sourceKeyRowField);
                        
                        @SuppressWarnings("unchecked")
                        RowField<T, Object> targetForiegnKeyValueRowField = (RowField<T, Object>)targetRowTranslator.createRowField(tct.getField());
                        targetForeignKeyValueFieldList.add(targetForiegnKeyValueRowField);
                    }
                    else
                    {
                        throw new OperationException(targetFieldName + " does not exist in " + getTargetTable().getRowClass() + 
                                " as specified with Cascade.foreignKeyValueFields in " + getSourceTable().getRowClass());
                    }
                }
            }
            catch (TranslatorException e)
            {
                // TOOD indicate type of access in message?
                throw new OperationException("error creating access for Cascade", e);
            }
            
            // provides quick test if any mapping is needed
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
    @SuppressWarnings("unchecked") // move to local assignment?
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
                RowTranslator<T> rowTranslator = getTargetTable().getRowTranslator();
                Field field = rowTranslator.getDeclaredField(targetFieldName);
                if (field != null) targetForeignReferenceField = (RowField<T, Object>)rowTranslator.createRowField(field); 
                else throw new MissingFieldException(targetFieldName, getTargetTable().getRowClass());
                
                if (log.isDebugEnabled())
                {
                    log.debug(getSourceTable().getRowClass() + " maps to foreign key reference " + field);
                }
            }
            catch (TranslatorException e)
            {
                throw new OperationException("error creating access to field " + 
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
                Object sourceKey = sourceKeyFieldList.get(i).get(sourceRow);
                targetForeignKeyValueFieldList.get(i).set(row, sourceKey);
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
            targetForeignReferenceField.set(row, sourceRow);
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


    /**
     * @return list of foreign key fields or null if none
     * @since 3.1
     */
    protected List<RowField<T, Object>> getTargetForeignKeyValueFieldList()
    {
        return targetForeignKeyValueFieldList;
    }
}
