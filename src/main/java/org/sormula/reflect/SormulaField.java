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
 * @deprecated Will be removed in version 4.0
 */
@Deprecated
public class SormulaField<C, T>
{
    private static final ClassLogger log = new ClassLogger();
    
    // note: move all members and corresponding methods to RowField when SormulaField is removed
    Field field;
    Method getMethod;
    Method setMethod;
    boolean scalar;
    boolean array;
    boolean collection;
    boolean map;
    
    
    /**
     * Constructs for a field.
     * 
     * @param field java reflection Field that corresponds to class variable
     * @throws ReflectException if error
     */
    public SormulaField(Field field) throws ReflectException
    {
        this(field, true);
    }
    
    
    /** 
     * Constructs for a field and optionally initializes references to getter and setter methods.
     * 
     * @param field java reflection Field that corresponds to class variable
     * @param initGettersAndSetters true to get references to getter and setter methods
     * @throws ReflectException if error
     * @since 3.4
     */
    protected SormulaField(Field field, boolean initGettersAndSetters) throws ReflectException
    {
        this.field = field;
        array = field.getType().isArray();
        collection = isClass(Collection.class);
        map = isClass(Map.class);
        scalar = !(array || collection || map);
        
        if (initGettersAndSetters) initGettersAndSetters();
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
     * Gets field supplied in constructor.
     * 
     * @return field Java field
     */
    public Field getField()
    {
        return field;
    }

    
    /**
     * Gets field array type.
     * 
     * @return if field is an array
     * @since 1.9 and 2.3
     */
    public boolean isArray()
    {
        return array;
    }


    /**
     * Gets field {@link Collection} inheritance.
     * 
     * @return true if field is a {@link #collection}
     * @since 1.9 and 2.3
     */
    public boolean isCollection()
    {
        return collection;
    }


    /**
     * Gets field {@link Map} inheritance.
     * 
     * @return true if field is a {@link Map}
     * @since 1.9 and 2.3
     */
    public boolean isMap()
    {
        return map;
    }


    /**
     * Reports boolean return type of field.
     * 
     * @return true if field type is primitive boolean
     */
    public boolean isBooleanMethod()
    {
        return field.getType().getName().equals("boolean");
    }
    
    
    /**
     * Invokes {@link #invokeGetMethod(Object)}. Overridden by
     * {@link DirectAccessField} and {@link MethodAccessField}.
     * 
     * @param object instance of field to get 
     * @return return value of field instance
     * @throws ReflectException if error
     * @since 3.4
     */
    public T get(C object) throws ReflectException
    {
        return invokeGetMethod(object);
    }
    
    
    /**
     * Invokes {@link #invokeSetMethod(Object, Object)}. Overridden by
     * {@link DirectAccessField} and {@link MethodAccessField}.
     *  
     * @param object instance of field to set new value
     * @param value new value of field instance
     * @throws ReflectException if error
     * @since 3.4
     */
    public void set(C object, T value) throws ReflectException
    {
        invokeSetMethod(object, value);
    }
    

    /**
     * Gets value from get method of an object using reflection.
     * 
     * @param object object to invoke get method upon
     * @return return value of get method invoked upon object
     * @throws ReflectException if error
     */
    @Deprecated
    public T invokeGetMethod(C object) throws ReflectException
    {
        try
        {
            @SuppressWarnings("unchecked") // invoke returns Object, type not known at compile time
            T value = (T)getMethod.invoke(object);
            if (log.isDebugEnabled()) log.debug("invokeGetMethod() method="+getMethod.getName() + " value="+value);
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
    @Deprecated
    public void invokeSetMethod(C object, T value) throws ReflectException
    {
        try
        {
            if (log.isDebugEnabled()) log.debug("invokeSetMethod() method="+setMethod.getName() + " value="+value);
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
    // move this method to RowField when SormulaField is removed
    public boolean isClass(Class<?> c)
    {
        return c.isAssignableFrom(field.getType());
    }
    
    
    /**
     * Gets canonical "get" method name.
     * 
     * @return package-name.class-name#get-method-name
     */
    @Deprecated
    public String getCanonicalGetMethodName()
    {
        return getMethod.getDeclaringClass().getCanonicalName() + "#" + getMethod.getName();
    }
    
    
    /**
     * Gets canonical "set" method name.
     * 
     * @return package-name.class-name#set-method-name
     */
    @Deprecated
    public String getCanonicalSetMethodName()
    {
        return setMethod.getDeclaringClass().getCanonicalName() + "#" + setMethod.getName();
    }
}
