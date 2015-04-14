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

import org.sormula.annotation.Column;
import org.sormula.annotation.Row;
import org.sormula.log.ClassLogger;


/**
 * Provides direct access to a field within a row class. This class is used for fields where
 * {@link Column#fieldAccess()} or {@link Row#fieldAccess()} is {@link FieldAccessType#Direct}.
 * 
 * @author Jeff Miller
 * @since 3.4
 * 
 * @param <C> class containing the field
 * @param <T> class of field
 */
public class DirectAccessField<C, T> extends RowField<C, T>
{
    private static final ClassLogger log = new ClassLogger();
    
    
    /**
     * Constructs for a field.
     * 
     * @param field java reflection Field that corresponds to class variable
     * @throws ReflectException if error
     */
    public DirectAccessField(Field field) throws ReflectException
    {
        super(field, false);
        field.setAccessible(true);
    }
    

    /**
     * Gets field value directly (non method access) using {@link Field#get(Object)}.
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
            @SuppressWarnings("unchecked") // get returns Object, type not known at compile time
            T value = (T)field.get(object);
            if (log.isDebugEnabled()) log.debug("get field="+field.getName() + " value="+value);
            return value;
        }
        catch (Exception e)
        {
            throw new ReflectException("error getting value for " + field, e);
        }
    }
    
    
    /**
     * Sets field value directly (non method access) using {@link Field#set(Object, Object)}.
     *  
     * @param object instance of field to set new value
     * @param value new value of field instance
     * @throws ReflectException if error
     */
    @Override
    public void set(C object, T value) throws ReflectException
    {
        try
        {
            if (log.isDebugEnabled()) log.debug("set field="+field.getName() + " value="+value);
            field.set(object, value);
        }
        catch (Exception e)
        {
            throw new ReflectException("error setting value=" + value + " for " + field, e);
        }
    }
}
