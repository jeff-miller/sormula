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
package org.sormula.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import org.sormula.log.ClassLogger;


/**
 * A field within a class with additional methods needed by sormula for reflection.
 * 
 * @author Jeff Miller
 *
 * @param <C> class containing the field
 * @param <T> class of field
 */
public class SormulaField<C, T>
{
    private static final ClassLogger log = new ClassLogger();
    
    Field field;
    Method getMethod;
    Method setMethod;
    boolean scalar;
    
    
    /**
     * Constructs for a field.
     * 
     * @param field java reflection Field that corresponds to class variable
     * @throws ReflectException if error
     */
    public SormulaField(Field field) throws ReflectException
    {
        this.field = field;
        
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
                    field.getDeclaringClass().getCanonicalName());
        }
        
        scalar = !(isClass(Collection.class) || isClass(Map.class));
    }
    
    
    /**
     * Gets field supplied in constructor.
     * 
     * @return field Java field
     */
    public Field getField()
    {
        return field;
    }


    /**
     * Reports boolean return type of field.
     * 
     * @return true if field type is primative boolean
     */
    public boolean isBooleanMethod()
    {
        return field.getType().getName().equals("boolean");
    }
    

    /**
     * Gets value from get method of an object using reflection.
     * 
     * @param object object to invoke get method upon
     * @return return value of get method invoked upon object
     * @throws ReflectException if error
     */
    public T invokeGetMethod(C object) throws ReflectException
    {
        try
        {
            @SuppressWarnings("unchecked") // invoke returns Object, type not known at compile time
            T value = (T)getMethod.invoke(object);
            if (log.isDebugEnabled()) log.debug("method="+getMethod.getName() + " value="+value);
            return value;
        }
        catch (Exception e)
        {
            throw new ReflectException("error getting value for " + getCanonicalGetMethodName(), e);
        }
    }
    
    
    /**
     * Sets value on object with set method using reflection.
     *  
     * @param object object to invoke set method upon
     * @param value value to set
     * @throws ReflectException if error
     */
    public void invokeSetMethod(C object, T value) throws ReflectException
    {
        try
        {
            if (log.isDebugEnabled()) log.debug("method="+setMethod.getName() + " value="+value);
            setMethod.invoke(object, value);
        }
        catch (Exception e)
        {
            throw new ReflectException("error setting value=" + value + " for " + getCanonicalSetMethodName(), e);
        }
    }
    
    
    /**
     * Reports if field is scalar.
     * 
     * @return true if field is not a {@link Collection} and not a {@link Map}
     * @see #isClass(Class)
     */
    public boolean isScalar()
    {
        return scalar;
    }


    /**
     * Tests if field is instance of class.
     * 
     * @param c class to test
     * @return true if field is instance of c or subclass of c
     * @see Class#isAssignableFrom(Class)
     */
    public boolean isClass(Class<?> c)
    {
        return c.isAssignableFrom(field.getType());
    }
    
    
    /**
     * Gets cannonical "get" method name.
     * 
     * @return package-name.class-name#get-method-name
     */
    public String getCanonicalGetMethodName()
    {
        return setMethod.getDeclaringClass().getCanonicalName() + "#" + getMethod.getName();
    }
    
    
    /**
     * Gets cannonical "set" method name.
     * 
     * @return package-name.class-name#set-method-name
     */
    public String getCanonicalSetMethodName()
    {
        return setMethod.getDeclaringClass().getCanonicalName() + "#" + setMethod.getName();
    }
}
