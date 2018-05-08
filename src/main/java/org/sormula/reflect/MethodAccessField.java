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
package org.sormula.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.sormula.annotation.Column;
import org.sormula.annotation.Row;
import org.sormula.log.SormulaLogger;
import org.sormula.log.SormulaLoggerFactory;


/**
 * Provides method access to a field within a row class. Getter/setter methods are used to access field. 
 * This class is used for fields where {@link Column#fieldAccess()} or {@link Row#fieldAccess()} is 
 * {@link FieldAccessType#Method}.
 * 
 * @author Jeff Miller
 * 
 * @param <C> class containing the field
 * @param <T> class of field
 * @since 3.4
 */
public class MethodAccessField<C, T> extends RowField<C, T>
{
    private static final SormulaLogger log = SormulaLoggerFactory.getClassLogger();
    Method getMethod;
    Method setMethod;

    
    /**
     * Constructs for a field.
     * 
     * @param field java reflection Field that corresponds to class variable
     * @throws ReflectException if error
     */
    public MethodAccessField(Field field) throws ReflectException
    {
        super(field);
        initGettersAndSetters();
    }
    
    
    void initGettersAndSetters() throws ReflectException
    {
        String getterPrefix;
        if (isBooleanMethod())
        {
            getterPrefix = "is";
        }
        else
        {
            getterPrefix = "get";
        }
        
        String methodBaseName = field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
        String methodName = null;
        
        try
        {
            methodName = getterPrefix + methodBaseName;
            getMethod = field.getDeclaringClass().getMethod(methodName);
            
            methodName = "set" + methodBaseName;
            setMethod = field.getDeclaringClass().getMethod(methodName, field.getType());
        }
        catch (NoSuchMethodException e)
        {
            throw new ReflectException("missing method " + methodName + " for " + 
                    field.getDeclaringClass().getCanonicalName(), e);
        }
    }

    
    /**
     * Gets field value with row getter method using {@link Method#invoke(Object, Object...)}.
     * 
     * @param object instance of field to get 
     * @return return value of field instance
     * @throws ReflectException if error
     */
    @Override
    public T get(C object) throws ReflectException 
    {
        try
        {
            @SuppressWarnings("unchecked") // invoke returns Object, type not known at compile time
            T value = (T)getMethod.invoke(object);
            if (log.isDebugEnabled()) log.debug("get method="+getMethod.getName() + " value="+value);
            return value;
        }
        catch (Exception e)
        {
            throw new ReflectException("error getting value for " + getMethod, e);
        }
    }


    /**
     * Sets field value with row setter method using {@link Method#invoke(Object, Object...)}.
     * 
     * @param object instance of field to set 
     * @throws ReflectException if error
     */
    public void set(C object, T value) throws ReflectException 
    {
        try
        {
            if (log.isDebugEnabled()) log.debug("set method="+setMethod.getName() + " value="+value);
            setMethod.invoke(object, value);
        }
        catch (Exception e)
        {
            throw new ReflectException("error setting value=" + value + " for " + setMethod, e);
        }
    }


    /**
     * @return method class corresponding to get method
     * @since 4.0
     */
	public Method getGetMethod() 
	{
		return getMethod;
	}


    /**
     * @return method class corresponding to set method
     * @since 4.0
     */
	public Method getSetMethod() 
	{
		return setMethod;
	}
}
