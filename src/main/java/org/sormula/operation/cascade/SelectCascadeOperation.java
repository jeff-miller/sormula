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

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.sormula.Table;
import org.sormula.annotation.cascade.SelectCascade;
import org.sormula.log.ClassLogger;
import org.sormula.operation.MapSelectOperation;
import org.sormula.operation.OperationException;
import org.sormula.operation.ScalarSelectOperation;
import org.sormula.operation.SelectOperation;
import org.sormula.reflect.ReflectException;
import org.sormula.reflect.SormulaField;
import org.sormula.translator.ColumnTranslator;
import org.sormula.translator.RowTranslator;
import org.sormula.translator.TranslatorException;
import org.sormula.translator.WhereTranslator;


/**
 * Cascade that selects rows from target table into target field when source 
 * operation initiates a cascade. 
 * 
 * @author Jeff Miller
 *
 * @param <S> row class of table that is source of cascade
 * @param <T> row class of table that is target of cascade
 */
public class SelectCascadeOperation<S, T> extends CascadeOperation<S, T>
{
    private static final ClassLogger log = new ClassLogger();
	SelectCascade selectCascadeAnnotation;
	ScalarSelectOperation<T> selectOperation;
	List<SormulaField<S, ?>> parameterFields;
	
	
    /**
     * Constructor used by {@link SelectOperation}.
     *  
     * @param targetField cascade select operation modifies this field
     * @param targetTable cascade select operation is performed on this table 
     * @param selectCascadeAnnotation cascade operation
     */
	@Deprecated // use constructor with source table
    public SelectCascadeOperation(SormulaField<S, ?> targetField, Table<T> targetTable, SelectCascade selectCascadeAnnotation)
    {
        super(targetField, targetTable, selectCascadeAnnotation.operation(), selectCascadeAnnotation.post());
        this.selectCascadeAnnotation = selectCascadeAnnotation;
    }
    
    
    /**
     * Constructor used by {@link SelectOperation}.
     *  
     * @param sourceTable cascade orgininates on row from this table 
     * @param targetField cascade select operation modifies this field
     * @param targetTable cascade select operation is performed on this table 
     * @param selectCascadeAnnotation cascade operation
     * @since 3.0
     */
    public SelectCascadeOperation(Table<S> sourceTable, SormulaField<S, ?> targetField, Table<T> targetTable, SelectCascade selectCascadeAnnotation)
    {
        super(sourceTable, targetField, targetTable, selectCascadeAnnotation.operation());
        this.selectCascadeAnnotation = selectCascadeAnnotation;
        setPost(selectCascadeAnnotation.post());
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public void cascade(S sourceRow) throws OperationException
    {
        super.cascade(sourceRow);
        setParameters(sourceRow);
        selectOperation.execute();
        
        // set results in target field
        @SuppressWarnings("unchecked") // target field type is not known at compile time
        SormulaField<S, Object> targetField = (SormulaField<S, Object>)getTargetField();
        
        try
        {
            if (targetField.isScalar())
            {
                // non collection type, set target row as next row
                T targetRow = selectOperation.readNext();
                setForeignKeyValues(targetRow);
                setForeignKeyReference(targetRow);
                targetField.invokeSetMethod(sourceRow, targetRow);
            }
            else
            {
                // collection or map, set target field to all rows in results
                @SuppressWarnings("unchecked") // collection fields must use SelectOperation
                SelectOperation<T, Object> o = (SelectOperation<T, Object>)selectOperation;
                Object rows = o.readAll();
                
                if (rows instanceof Collection)
                {
                    // select operation returned a Collection
                    @SuppressWarnings("unchecked") // target field type is not known at compile time
                    Collection<T> c = (Collection<T>)rows;
                    setForeignKeyValues(c);
                    setForeignKeyReference(c);
                    
                    if (targetField.isArray())
                    {
                        // set as array
                        targetField.invokeSetMethod(sourceRow, toTargetArray(c));
                    }
                    else
                    {
                        // set as collection 
                        targetField.invokeSetMethod(sourceRow, c);
                    }
                }
                else if (rows instanceof Map)
                {
                    // select operation returned a Map
                    @SuppressWarnings("unchecked") // target field type is not known at compile time
                    Map<?, T> m = (Map<?, T>)rows;

                    Collection<T> mapValues = m.values();
                    setForeignKeyValues(mapValues);
                    setForeignKeyReference(mapValues);
                    
                    if (targetField.isArray())
                    {
                        // set as array
                        targetField.invokeSetMethod(sourceRow, toTargetArray(mapValues));
                    }
                    else
                    {
                        // set as map
                        targetField.invokeSetMethod(sourceRow, m);
                    }
                }
                else
                {
                    throw new OperationException("can't convert result " + o.getClass() +
                        " for " + targetField.getCanonicalSetMethodName());
                }
            }
        }
        catch (ReflectException e)
        {
            throw new OperationException("error setting cascade results to " + 
                    targetField.getCanonicalSetMethodName(), e);
        }
    }
    

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked") // type controlled by annotations 
    public void prepare() throws OperationException
    {
        super.prepare();
        selectOperation = (ScalarSelectOperation<T>)createOperation();
        
        if (!isSourceTargetFieldNames())
        {
            // selectCascadeAnnotation.sourceParameterFieldNames() are NOT defined by selectCascadeAnnotation.targetWhereName()
            // prepareWhere() will need parameter fields if selectCascadeAnnotation.targetWhereName()="#sourceFieldNames"
            // so do in this order
            prepareParameterFields();
            prepareWhere();
        }
        else if (!isWhereSourceFieldNames()) 
        {
            // target names may be needed in prepareParameterFields() so do in this order

            // selectCascadeAnnotation.targetWhereName() are NOT defined by selectCascadeAnnotation.sourceParameterFieldNames()
            // prepareParameterFields() will be need target fields if selectCascadeAnnotation.sourceParameterFieldNames()="#targetFieldNames"
            // so do in this order
            prepareWhere();
            prepareParameterFields();
        }
        else
        {
            // source and target are circularly defined
            throw new OperationException(selectCascadeAnnotation.sourceParameterFieldNames()[0] + " and " +
                    selectCascadeAnnotation.targetWhereName() + " are circular configurations for " + 
                    getTargetField().getField());
        }
        
        // order by
        selectOperation.setOrderBy(selectCascadeAnnotation.targetOrderByName());
        
        if (selectOperation instanceof MapSelectOperation)
        {
        	// map select operations need method that gets key from row
        	try
        	{
	        	Method keyMethod = getTargetTable().getRowTranslator().getRowClass().getMethod(
	        			selectCascadeAnnotation.targetKeyMethodName());
	        	((MapSelectOperation)selectOperation).setGetKeyMethod(keyMethod);
        	}
        	catch (NoSuchMethodException e)
        	{
        		throw new OperationException("error getting key method in SelectCascade.keyMethodName()=" +
        		        selectCascadeAnnotation.targetKeyMethodName(), e);
        	}
        }
        
        // verify scalar field is compatible with target table row type (this test may not be necessary?)
        SormulaField<S, ?> tf = getTargetField();
        if (tf.isScalar() && !tf.isClass(getTargetTable().getRowTranslator().getRowClass()))
        {
        	throw new OperationException(tf.getClass().getName() + " is not assignable from " +
        			getTargetTable().getRowTranslator().getRowClass().getName());
        }
    }
    
    
    /**
     * @return true if {@link SelectCascade#sourceParameterFieldNames()} is "#primaryKeyFields"
     * @since 3.1
     */
    public boolean isSourcePrimaryKeyFields()
    {
        String[] parameterFieldNames = selectCascadeAnnotation.sourceParameterFieldNames();
        return parameterFieldNames.length > 0 && parameterFieldNames[0].equals("#primaryKeyFields");
    }
    
    
    /**
     * @return true if {@link SelectCascade#sourceParameterFieldNames()} is "#targetFieldNames"
     * @since 3.1
     */
    public boolean isSourceTargetFieldNames()
    {
        String[] parameterFieldNames = selectCascadeAnnotation.sourceParameterFieldNames();
        return parameterFieldNames.length > 0 && parameterFieldNames[0].equals("#targetFieldNames");
    }
    
    
    /**
     * @return true if {@link SelectCascade#targetWhereName()} is "#sourceFieldNames"
     * @since 3.1
     */
    public boolean isWhereSourceFieldNames()
    {
        return selectCascadeAnnotation.targetWhereName().equals("#sourceFieldNames");
    }
    
    
    /**
     * @return true if {@link SelectCascade#targetWhereName()} is "#foreignKeyValueFields"
     * @since 3.1
     */
    public boolean isWhereForeignKeyValueFields()
    {
        return selectCascadeAnnotation.targetWhereName().equals("#foreignKeyValueFields");
    }


    /**
     * Uses reflection to get fields for each of the source class
     * variables that will be read from source and set as parameters on
     * cascade operation. Parameter values are set when {@link #setParameters} is
     * invoked by {@link #cascade}.
     * 
     * @throws OperationException if error
     */
    protected void prepareParameterFields() throws OperationException
    {
        // parameters are from source class (target field is in source class)
        RowTranslator<S> sourceRowTranslator = getSourceTable().getRowTranslator();
        
        try
        {
            String[] parameterFieldNames;
            
            if (isSourcePrimaryKeyFields())
            {
                // use fields from primary keys
                if (log.isDebugEnabled())
                {
                    log.debug("source parameter field names are source primary keys");
                }
                
                List<ColumnTranslator<S>> primaryKeyColumnTranslators = sourceRowTranslator.getPrimaryKeyWhereTranslator().getColumnTranslatorList();
                parameterFieldNames = new String[primaryKeyColumnTranslators.size()];
                int i = 0;
                for (ColumnTranslator<S> ct : primaryKeyColumnTranslators)
                {
                    parameterFieldNames[i++] = ct.getColumnName();
                }
            }
            else if (isSourceTargetFieldNames())
            {
                // use fields from target where
                if (log.isDebugEnabled())
                {
                    log.debug("source parameter field names from target where condition " + selectOperation.getWhereConditionName());
                }
                
                List<ColumnTranslator<T>> whereColumnTranslators = selectOperation.getWhereTranslator2().getColumnTranslatorList();
                parameterFieldNames = new String[whereColumnTranslators.size()];
                int i = 0;
                for (ColumnTranslator<T> ct : whereColumnTranslators)
                {
                    parameterFieldNames[i++] = ct.getColumnName();
                }
            }
            else
            {
                // use fields from annotation
                parameterFieldNames = selectCascadeAnnotation.sourceParameterFieldNames();
                if (log.isDebugEnabled())
                {
                    log.debug("source parameter field names from cascade sourceParameterFieldNames=" + 
                            Arrays.asList(parameterFieldNames));
                }
            }
            
            parameterFields = new ArrayList<SormulaField<S, ?>>(parameterFieldNames.length);
            for (int i = 0; i < parameterFieldNames.length; ++i)
            {
                parameterFields.add(new SormulaField<S, Object>(
                        sourceRowTranslator.getDeclaredField(parameterFieldNames[i])));
            }
        }
        catch (ReflectException e)
        {
            throw new OperationException("error creating SormulaField", e);
        }
    }
    
    
    /**
     * {@link #cascade} invokes this method to set parameters in cascade operation based 
     * upon parameter fields that were created by {@link #prepareParameterFields()}. 
     * Override for custom parameter initialization.
     * 
     * @param sourceRow cascade source row
     */
    protected void setParameters(S sourceRow) throws OperationException
    {
        if (parameterFields.size() > 0)
        {
            // at least one parameter (zero parameters are allowed)
        
            // need Object[] for setParameters(Object...)
            Object[] parameters = new Object[parameterFields.size()];
            int i = 0;
            
            try
            {
                for (SormulaField<S, ?> f: parameterFields)
                {
                    parameters[i++] = f.invokeGetMethod(sourceRow);
                }
            }
            catch (ReflectException e)
            {
                throw new OperationException("error getting parameter value", e);
            }
            
            selectOperation.setParameters(parameters);
        }
    }

    
    /**
     * TODO
     * @throws OperationException
     * @since 3.1
     */
    protected void prepareWhere() throws OperationException    
    {
        try
        {
            if (isWhereSourceFieldNames())
            {
                if (log.isDebugEnabled()) log.debug("#sourceFieldNames where condition for " + getTargetTable().getRowClass());
                RowTranslator<T> targetRowTranslator = getTargetTable().getRowTranslator();
                WhereTranslator<T> whereTranslator = new WhereTranslator<T>(targetRowTranslator, parameterFields.size());
                
                // for all source parameter fields
                for (SormulaField<S, ?> spf : parameterFields)
                {
                    // look up target column translator of same name
                    if (log.isDebugEnabled()) log.debug("add field " + spf.getField().getName());
                    ColumnTranslator<T> ctTarget = targetRowTranslator.getColumnTranslator(spf.getField().getName());
                    if (ctTarget != null)
                    {
                        whereTranslator.addColumnTranslator(ctTarget);
                    }
                    else
                    {
                        // not likely since super.prepare() would have detected this condition
                        throw new OperationException("source field named, " + spf.getField().getName() + 
                                ", does not exist in target, " + getTargetTable().getRowClass());
                    }
                }
                
                selectOperation.setWhereTranslator2(whereTranslator);
            }
            else if (isWhereForeignKeyValueFields())
            {
                if (log.isDebugEnabled()) log.debug("#foreignKeyValueFields where condition for " + getTargetTable().getRowClass());
                RowTranslator<T> targetRowTranslator = getTargetTable().getRowTranslator();
                WhereTranslator<T> whereTranslator = new WhereTranslator<T>(targetRowTranslator, getTargetForeignKeyValueFieldList().size());
                
                // for all foreign key fields in target
                for (SormulaField<T, Object> tfk : getTargetForeignKeyValueFieldList())
                {
                    // look up target column translator of same name
                    if (log.isDebugEnabled()) log.debug("add field " + tfk.getField());
                    ColumnTranslator<T> ctTarget = targetRowTranslator.getColumnTranslator(tfk.getField().getName());
                    if (ctTarget != null)
                    {
                        whereTranslator.addColumnTranslator(ctTarget);
                    }
                    else
                    {
                        // not likely since super.prepare() would have detected this condition
                        throw new OperationException("target foreign key field named, " + tfk.getField().getName() + 
                                ", does not exist in target, " + getTargetTable().getRowClass());
                    }
                }
                
                selectOperation.setWhereTranslator2(whereTranslator);
            }
            else
            {
                // use where defined by annotation
                selectOperation.setWhere(selectCascadeAnnotation.targetWhereName());
            }
        }
        catch (TranslatorException e)
        {
            throw new OperationException("error creating where translator for " + selectCascadeAnnotation.targetWhereName());
        }
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws OperationException
    {
        selectOperation.close();
    }
    
    
    protected T[] toTargetArray(Collection<T> c) throws OperationException
    {
        try
        {
            // create array to hold all elements of collection
            @SuppressWarnings("unchecked") // target not known at compile time
            T[] array = (T[])Array.newInstance(getTargetField().getField().getType().getComponentType(), c.size());
            
            // copy elements into array
            c.toArray(array);
            
            // return array
            return array;  
        }
        catch (Exception e)
        {
            throw new OperationException("error creating new array for target field " + getTargetField().getField(), e);
        }
    }
}
