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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.sormula.Table;
import org.sormula.annotation.cascade.SelectCascade;
import org.sormula.operation.MapSelectOperation;
import org.sormula.operation.OperationException;
import org.sormula.operation.ScalarSelectOperation;
import org.sormula.operation.SelectOperation;
import org.sormula.reflect.ReflectException;
import org.sormula.reflect.SormulaField;


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
    public SelectCascadeOperation(SormulaField<S, ?> targetField, Table<T> targetTable, SelectCascade selectCascadeAnnotation)
    {
        super(targetField, targetTable, selectCascadeAnnotation.operation(), selectCascadeAnnotation.post());
        this.selectCascadeAnnotation = selectCascadeAnnotation;
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public void cascade(S sourceRow) throws OperationException
    {
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
                targetField.invokeSetMethod(sourceRow, selectOperation.readNext());
            }
            else
            {
                // collection or map, set target field to all rows in results
                @SuppressWarnings("unchecked") // collection fields must use SelectOperation
                SelectOperation<T, Object> o = (SelectOperation<T, Object>)selectOperation;
                targetField.invokeSetMethod(sourceRow, o.readAll());
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
        selectOperation = (ScalarSelectOperation<T>)createOperation();
        
        // parameter fields to read
        prepareParameterFields();
        
        // where
        selectOperation.setWhere(selectCascadeAnnotation.targetWhereName());
        
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
        if (targetField.isScalar() && !targetField.isClass(getTargetTable().getRowTranslator().getRowClass()))
        {
        	throw new OperationException(targetField.getClass().getName() + " is not assignable from " +
        			getTargetTable().getRowTranslator().getRowClass().getName());
        }
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
        Class sourceRowClass = getTargetField().getField().getDeclaringClass();
        
        try
        {
            String[] parameterFieldNames = selectCascadeAnnotation.sourceParameterFieldNames();
            parameterFields = new ArrayList<SormulaField<S,?>>(parameterFieldNames.length);
            
            for (int i = 0; i < parameterFieldNames.length; ++i)
            {
                parameterFields.add(new SormulaField<S, Object>(
                        sourceRowClass.getDeclaredField(parameterFieldNames[i])));
            }
        }
        catch (ReflectException e)
        {
            throw new OperationException("error creating SormulaField", e);
        }
        catch (NoSuchFieldException e)
        {
            throw new OperationException("error getting parameter field", e);
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
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws OperationException
    {
        selectOperation.close();
    }
}
