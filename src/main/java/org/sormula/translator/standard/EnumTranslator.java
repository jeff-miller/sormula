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
 * ({@link Enum#name()}) and converts name to {@link Enum} with {@link Enum#valueOf(Class, String)}.
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
 * Create a subclass for a custom {@link TypeTranslator} for Enum fields. Override
 * {@link #read(ResultSet, int)} and {@link #write(PreparedStatement, int, Enum)} if column
 * is some type other than a String. Override {@link #enumToColumn(Enum)} and {@link #columnToEnum(Object)}
 * to store something other than Enum name in database.
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
     * Gets the class of Enum that is translated.
     * 
     * @return Class of Enum to be translated 
     */
    public Class<T> getEnumClass()
    {
        return enumClass;
    }


    /**
     * Sets the class of Enum to translate. This method is typically invoked by {@link RowTranslator} to 
     * initialize this translator when {@link Table} object is initialized that
     * contains an Enum field.
     * 
     * @param enumClass Class of Enum to be translated
     */
    public void setEnumClass(Class<T> enumClass)
    {
        this.enumClass = enumClass;
    }


    /**
     * Gets the name of the Enum to use in {@link #read(ResultSet, int)} when the value read from 
     * database cannot be found in {@link Class#getEnumConstants()} of class {@link #getEnumClass()}.
     * 
     * @return default name of Enum to use; empty string for no default (null Enum)
     */
    public String getDefaultEnumName()
    {
        return defaultEnumName;
    }


    /**
     * Sets the name of the Enum to use in {@link #read(ResultSet, int)} when the value read 
     * from the database is not a valid Enum. If no default has been set then
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
            // find default Enum to use in read()
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
     * @return default Enum; null if no default
     */
    public T getDefaultEnum()
    {
        return defaultEnum;
    }


    /**
     * Writes the return of {@link #enumToColumn(Enum)} for Enum parameter to database as a String. 
     * By default {@link #enumToColumn(Enum)} returns {@link Enum#name()}. Override this method if
     * a type other than String is to be written.
     * <p>
     * See {@link #enumToColumn(Enum)} for details about what will be written.
     * {@inheritDoc}
     */
    public void write(PreparedStatement preparedStatement, int parameterIndex, Enum<T> parameter) throws Exception
    {
        preparedStatement.setString(parameterIndex, (String)enumToColumn(parameter));
    }
    
    
    /**
     * Reads the column as a String and returns {@link #columnToEnum(Object)}. 
     * If column is null then null is returned. If no Enum can be found, then the
     * {@link #getDefaultEnum()} is returned.
     * <p>
     * See {@link #columnToEnum(Object)} for details about what will be returned.
     * {@inheritDoc}
     */
    public Enum<T> read(ResultSet resultSet, int columnIndex) throws Exception
    {
        return columnToEnum(resultSet.getString(columnIndex));
    }
    
    
    /**
     * Converts an Enum constant to a value that will be stored in the
     * table column. This method returns {@link Enum#name()}. Override this
     * method to use a custom value.
     * 
     * @param enumValue the Enum constant to convert
     * @return {@link Enum#name()}; null if enumValue is null
     * 
     * @since 4.1
     */
    protected Object enumToColumn(Enum<T> enumValue)
    {
        if (enumValue != null) return enumValue.name();
        return null;
    }
    
    
    /**
     * Converts a table column value to an Enum with {@link Enum#valueOf(Class, String)}.
     * Override this method if you returned a custom value for {@link #enumToColumn(Enum)}.
     * 
     * @param columnValue value read from table column that stores Enum
     * @return Enum where {@link #enumToColumn(Enum)} equals columnValue;
     * null if columnValue is null; {@link #getDefaultEnum()} if columnValue
     * cannot be found using {@link Enum#valueOf(Class, String)}.
     * 
     * @since 4.1
     */
    protected Enum<T> columnToEnum(Object columnValue)
    {
        Enum<T> result = null;
        
        if (columnValue != null)
        {
            try
            {
                result = T.valueOf(enumClass, columnValue.toString());
            }
            catch (IllegalArgumentException e)
            {
                // column was not found, use default
                result = getDefaultEnum();
            }
        }

        return result;
    }
}
