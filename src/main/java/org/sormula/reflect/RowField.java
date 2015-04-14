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


/**
 * An abstract reference to a field within a row class with utility methods needed by sormula for reflection.
 * 
 * @author Jeff Miller
 * @since 3.4
 * @param <C> class containing the field
 * @param <T> class of field
 */
public abstract class RowField<C, T> extends SormulaField<C, T> 
// NOTE: SormulaField is base class so that RowField to help with backward compatibility with SormulaField
{
    /**
     * Factory method to create concrete {@link RowField} subclass instance for a field.
     * 
     * @param fieldAccessType direct or method access
     * @param field field to access
     * @return {@link MethodAccessField} or {@link DirectAccessField}
     * @throws ReflectException if error
     */
    public static <C, T> RowField<C, T> newInstance(FieldAccessType fieldAccessType, Field field) throws ReflectException
    {
        if (fieldAccessType == FieldAccessType.Direct)
        {
            return new DirectAccessField<C, T>(field);
        }
        else if (fieldAccessType == FieldAccessType.Method)
        {
            return new MethodAccessField<C, T>(field);
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
        super(field);
    }
    
    
    /**
     * Constructs and optionally initializes getter/setter method references.
     * 
     * @param field java reflection Field that corresponds to class variable
     * @param initGettersAndSetters true to get references to getter and setter methods
     * @throws ReflectException if error
     */
    protected RowField(Field field, boolean initGettersAndSetters) throws ReflectException
    {
        super(field, initGettersAndSetters);
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
