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
package org.sormula.translator.standard;

import java.util.HashMap;
import java.util.Map;

import org.sormula.annotation.EnumType;
import org.sormula.annotation.ExplicitType;
import org.sormula.annotation.ImplicitType;
import org.sormula.translator.TypeTranslator;


/**
 * {@link TypeTranslator} to use for Enum fields. Writes Enum ({@link Enum#toString()}) 
 * to column as a String. Override {@link Enum#toString()} to supply a value to store
 * in the database. By default {@link Enum#toString()} returns {@link Enum#name()}.
 * <p> 
 * Column should be a sql type that is compatible with java String (like varchar) and
 * wide enough for the longest value returned by {@link Enum#toString()}.
 * <p>
 * This class and subclasses may be used as a {@link TypeTranslator} for Enum fields
 * in one of two ways:
 * <ul>
 * <li>by specifying with {@link ImplicitType#translator()} or {@link ExplicitType#translator()}.</li> 
 * <li>by specifying in {@link EnumType#translator()}.</li>
 * </ul>  
 * 
 * @since 4.1
 * @author Jeff Miller
 */
public class EnumToStringTranslator<T extends Enum<T>> extends EnumTranslator<T>
{
    Map<Object, Enum<T>> columnEnumMap;    
    
    
    /**
     * {@inheritDoc}
     * <p>
     * This method also builds a map of Enums where keys are {@link #enumToColumn(Enum)}
     * for all elements of {@link Class#getEnumConstants()}. Map is used by {@link #columnToEnum(Object)}
     * to convert from column value to Enum.
     * 
     */
    @Override
    public void setEnumClass(Class<T> enumClass) 
    {
        super.setEnumClass(enumClass);
        
        // create map of column values to Enum for use in columnToEnum method
        T[] enums = enumClass.getEnumConstants();
        columnEnumMap = new HashMap<>(enums.length * 2);
        for (T e : enums) columnEnumMap.put(enumToColumn(e), e);
    }


    /**
     * Gets the map of Enum's that were built by {@link #setEnumClass(Class)} for use
     * in converting database values to Enum's in {@link #columnToEnum(Object)}.
     * 
     * @return map of Enum's where key is {@link #enumToColumn(Enum)}
     */
    public Map<Object, Enum<T>> getColumnEnumMap() 
    {
        return columnEnumMap;
    }


    /**
     * Converts an Enum constant to column value with {@link Enum#toString()}. Override
     * this method if something other than {@link Enum#toString()} should be used for
     * database value of Enum.
     * 
     * @param enumValue the Enum constant to convert
     * @return {@link Enum#toString()}; null if enumValue is null
     */
    @Override
    protected Object enumToColumn(Enum<T> enumValue)
    {
        if (enumValue != null) return enumValue.toString();
        return null;
    }
    

    /**
     * Converts a table column value to an Enum. This method looks up
     * Enum in map that was created by {@link #setEnumClass(Class)}. Key
     * to map is {@link #enumToColumn(Enum)}.
     * <p>
     * This method may be overridden if desired. It is not likely that
     * you will need to override if you've overridden {@link #enumToColumn(Enum)}.
     * 
     * @param columnValue value read from table column that stores Enum
     * @return Enum where {@link #enumToColumn(Enum)} equals columnValue;
     * null if columnValue is null; {@link #getDefaultEnum()} if columnValue
     * cannot be found in {@link #getColumnEnumMap()}
     */
    @Override
    protected Enum<T> columnToEnum(Object columnValue)
    {
        Enum<T> result = null;
        
        if (columnValue != null)
        {
            result = columnEnumMap.get(columnValue);
            
            if (result == null)
            {
                // column was not found, use default
                result = defaultEnum;
            }
        }

        return result;
    }
}
