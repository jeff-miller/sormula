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

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.sormula.Table;
import org.sormula.annotation.EnumType;
import org.sormula.annotation.ExplicitType;
import org.sormula.annotation.ImplicitType;
import org.sormula.translator.RowTranslator;
import org.sormula.translator.TypeTranslator;


/**
 * {@link TypeTranslator} to use for Enum fields. Writes Enum name ({@link Enum#name()}) 
 * to column as a String. Reads column as a String Enum name
 * ({@link Enum#name()}) and converts name to {@link Enum} by searching {@link Class#getEnumConstants()}.
 * <p> 
 * Column should be a sql type that is compatible with java String (like varchar) and
 * wide enough for the longest Enum name.
 * <p>
 * This class and subclasses may be used as a {@link TypeTranslator} for Enum fields
 * in one of two ways:
 * <ul>
 * <li>by specifying with {@link ImplicitType#translator()} or {@link ExplicitType#translator()}.</li> 
 * <li>by specifying in {@link EnumType#translator()}.</li>
 * </ul>  
 * This class is the default translator to use for Enum types when no annotation is specified for
 * an Enum field within a row class.
 * <p>
 * Subclass to create a custom {@link TypeTranslator} for Enum fields. Override
 * {@link #read(ResultSet, int)} and {@link #write(PreparedStatement, int, Enum)}.
 * 
 * @since 3.3
 * @author Jeff Miller
 */
public class EnumTranslator<T extends Enum<T>> implements TypeTranslator<Enum<T>>
{
    Class<T> enumClass;
    String defaultEnumName;
    T defaultEnum;
    
    
    /**
     * Gets the class to use to look up Enum by name using {@link Class#getEnumConstants()}.
     * 
     * @return Class of Enum to be translated 
     */
    public Class<T> getEnumClass()
    {
        return enumClass;
    }


    /**
     * Sets the class to use to look up Enum by name using {@link Class#getEnumConstants()}. 
     * This method is typically invoked by {@link RowTranslator} to initialize this translator
     * when {@link Table} object is initialized that contains an Enum field
     * 
     * @param enumClass Class of Enum to be translated
     */
    public void setEnumClass(Class<T> enumClass)
    {
        this.enumClass = enumClass;
    }


    /**
     * Gets the Enum name to use in {@link #read(ResultSet, int)} when the name read from 
     * database cannot be found in {@link Class#getEnumConstants()} of class {@link #getEnumClass()}.
     * 
     * @return default name of Enum to use; empty string for no default (null Enum)
     */
    public String getDefaultEnumName()
    {
        return defaultEnumName;
    }


    /**
     * Sets the Enum name to use in {@link #read(ResultSet, int)} when the Enum name read
     * from the database is not a valid Enum name. If no default has been set then
     * {@link #read(ResultSet, int)} will return null.
     * <p>
     * {@link #setEnumClass(Class)} must be invoked prior to invoking this method. This method is 
     * invoked by {@link RowTranslator} when {@link Table} object is initialized.
     * 
     * @param defaultEnumName default name of Enum to use or empty string for no default
     */
    public void setDefaultEnumName(String defaultEnumName)
    {
        this.defaultEnumName = defaultEnumName;
        
        if (defaultEnumName.length() > 0)
        {
            // find default enum to use in read()
            for (T e : enumClass.getEnumConstants())
            {
                if (defaultEnumName.equals(e.name()))
                {
                    // found
                    defaultEnum = e;
                    break;
                }
            }
        }
    }


    /**
     * Gets the default Enum associated with {@link #getDefaultEnumName()}.
     * 
     * @return default Enum or null if none
     */
    public T getDefaultEnum()
    {
        return defaultEnum;
    }


    /**
     * Writes the name of the Enum parameter to database as a String. If parameter is null, then
     * null is written to database.
     * 
     * {@inheritDoc}
     */
    public void write(PreparedStatement preparedStatement, int parameterIndex, Enum<T> parameter) throws Exception
    {
        if (parameter != null) preparedStatement.setString(parameterIndex, parameter.name());
        else                   preparedStatement.setString(parameterIndex, null);
    }
    
    
    /**
     * Reads the name of the Enum from the database and finds the corresponding Enum from
     * {@link Class#getEnumConstants()}. If column is null then null is returned. If no Enum
     * can be found, then Enum with name of {@link #getDefaultEnumName()} is returned.
     * 
     * {@inheritDoc}
     */
    public Enum<T> read(ResultSet resultSet, int columnIndex) throws Exception
    {
        Enum<T> result = null;
        String name = resultSet.getString(columnIndex);
        
        if (name != null)
        {
            // linear search should be fast enough since most enums are few in number
            for (T e : enumClass.getEnumConstants())
            {
                if (name.equals(e.name()))
                {
                    // found
                    result = e;
                    break;
                }
            }
            
            if (result == null)
            {
                // name was not found for enum
                result = defaultEnum;
            }
        }
        
        return result; 
    }
}
