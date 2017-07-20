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
import java.util.Collection;
import java.util.Map;


/**
 * An abstract reference to a field within a row class with utility methods needed by sormula for reflection.
 * 
 * @author Jeff Miller
 * @since 3.4
 * @param <C> class containing the field
 * @param <T> class of field
 */
public abstract class RowField<C, T> 
{
    Field field;
    boolean scalar;
    boolean array;
    boolean collection;
    boolean map;

    
    /**
     * Factory method to create concrete {@link RowField} subclass instance for a field.
     * 
     * @param fieldAccessType direct or method access
     * @param field field to access
     * @return {@link MethodAccessField} or {@link DirectAccessField}
     * @throws ReflectException if error
	 * @param <C> class containing the field
	 * @param <T> class of field
     */
    public static <C, T> RowField<C, T> newInstance(FieldAccessType fieldAccessType, Field field) throws ReflectException
    {
        if (fieldAccessType == FieldAccessType.Direct)
        {
            return new DirectAccessField<>(field);
        }
        else if (fieldAccessType == FieldAccessType.Method)
        {
            return new MethodAccessField<>(field);
        }
        else
        {
            // not likely
            throw new ReflectException("unknown accessor type " + fieldAccessType);
        }
    }
    
    
    /**
     * Constructs for a field.
     * 
     * @param field java reflection Field that corresponds to class variable
     * @throws ReflectException if error
     */
    public RowField(Field field) throws ReflectException
    {
        this.field = field;
        array = field.getType().isArray();
        collection = isClass(Collection.class);
        map = isClass(Map.class);
        scalar = !(array || collection || map);
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
     * Gets value from get method of an object using reflection.
     * 
     * @param object instance of field to get 
     * @return return value of field instance
     * @throws ReflectException if error
     */
    public abstract T get(C object) throws ReflectException;
    
    
    /**
     * Sets value on object with set method using reflection.
     *  
     * @param object instance of field to set new value
     * @param value new value of field instance
     * @throws ReflectException if error
     */
    public abstract void set(C object, T value) throws ReflectException;
}
