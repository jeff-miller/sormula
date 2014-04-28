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

import org.sormula.annotation.EnumType;
import org.sormula.annotation.ExplicitType;
import org.sormula.annotation.ImplicitType;
import org.sormula.translator.TypeTranslator;


/**
 * TODO
 * This class and subclasses may be used as a {@link TypeTranslator} for Enum fields
 * by specifying in {@link ImplicitType#translator()} or {@link ExplicitType#translator()}. 
 * <p> 
 * This class and subclasses may be specified in {@link EnumType#translator()}.
 * <p>  
 * Subclass this class to create a custom {@link TypeTranslator} for Enum fields.
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
     * TODO
     * @return
     */
    public Class<T> getEnumClass()
    {
        return enumClass;
    }


    /**
     * TODO
     * @param enumClass
     */
    public void setEnumClass(Class<T> enumClass)
    {
        this.enumClass = enumClass;
    }


    /**
     * TODO
     * @return
     */
    public String getDefaultEnumName()
    {
        return defaultEnumName;
    }


    /**
     * TODO
     * @param defaultEnumName
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
     * {@inheritDoc}
     */
    public void write(PreparedStatement preparedStatement, int parameterIndex, Enum<T> parameter) throws Exception
    {
        if (parameter != null) preparedStatement.setString(parameterIndex, parameter.name());
        else                   preparedStatement.setString(parameterIndex, null);
    }
    
    
    /**
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
