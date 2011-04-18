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
package org.sormula.translator.standard;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import org.sormula.translator.AbstractColumnTranslator;
import org.sormula.translator.ColumnTranslator;
import org.sormula.translator.ObjectColumnTranslator;
import org.sormula.translator.TranslatorException;


/**
 * Delegates to translator based upon field type. This translator should work for all
 * standard Java data types. 
 * 
 * @since 1.0
 * @author Jeff Miller
 * @param <R> row class for table
 */
public class StandardColumnTranslator<R> implements ColumnTranslator<R>
{
    static Map<String, Class<? extends ColumnTranslator>> columnTranslatorMap = 
        new HashMap<String, Class<? extends ColumnTranslator>>(50);
    static
    {
        columnTranslatorMap.put(BigDecimal.class.getCanonicalName(), BigDecimalColumnTranslator.class);
        columnTranslatorMap.put("boolean", BooleanColumnTranslator.class);
        columnTranslatorMap.put(Boolean.class.getCanonicalName(), BooleanColumnTranslator.class);
        columnTranslatorMap.put("byte", ByteColumnTranslator.class);
        columnTranslatorMap.put(Byte.class.getCanonicalName(), ByteColumnTranslator.class);
        columnTranslatorMap.put("double", DoubleColumnTranslator.class);
        columnTranslatorMap.put(Double.class.getCanonicalName(), DoubleColumnTranslator.class);
        columnTranslatorMap.put("float", FloatColumnTranslator.class);
        columnTranslatorMap.put(Float.class.getCanonicalName(), FloatColumnTranslator.class);
        columnTranslatorMap.put("int", IntegerColumnTranslator.class);
        columnTranslatorMap.put(Integer.class.getCanonicalName(), IntegerColumnTranslator.class);
        columnTranslatorMap.put("long", LongColumnTranslator.class);
        columnTranslatorMap.put(Long.class.getCanonicalName(), LongColumnTranslator.class);
        columnTranslatorMap.put("short", ShortColumnTranslator.class);
        columnTranslatorMap.put(Short.class.getCanonicalName(), ShortColumnTranslator.class);
        columnTranslatorMap.put(Object.class.getCanonicalName(), ObjectColumnTranslator.class);
        columnTranslatorMap.put(String.class.getCanonicalName(), StringColumnTranslator.class);
        columnTranslatorMap.put(java.util.Date.class.getCanonicalName(), DateColumnTranslator.class);
        columnTranslatorMap.put(java.sql.Date.class.getCanonicalName(), SqlDateColumnTranslator.class);
        columnTranslatorMap.put(java.sql.Time.class.getCanonicalName(), SqlTimeColumnTranslator.class);
        columnTranslatorMap.put(java.sql.Timestamp.class.getCanonicalName(), SqlTimestampColumnTranslator.class);
    }
    
    ColumnTranslator<R> columnTranslator;
    
    
    /**
     * Constructor for default annotation value.
     */
    public StandardColumnTranslator()
    {
    }
    

	/**
	 * See super class constructor for description.
	 */
    @SuppressWarnings("unchecked") // annotations cannot be parameterized
    public StandardColumnTranslator(Field field, String columnName) throws Exception
    {
        // choose translator based upon field type
        Class<? extends ColumnTranslator> translatorClass = columnTranslatorMap.get(field.getType().getCanonicalName());
        
        if (translatorClass != null)
        {
            Class<? extends ColumnTranslator<?>> tc = (Class<? extends ColumnTranslator<?>>)translatorClass;
            columnTranslator = (ColumnTranslator<R>)AbstractColumnTranslator.newInstance(tc, field, columnName); 
        }
        else
        {
            throw new TranslatorException("no translator for " + field.getDeclaringClass().getCanonicalName() + "#" + field.getName());
        }
    }

    
    /**
     * {@inheritDoc}
     */
    public Field getField()
    {
        return columnTranslator.getField();
    }
    

    /**
     * {@inheritDoc}
     */
    public String getColumnName()
    {
        return columnTranslator.getColumnName();
    }
    
    
    /**
     * {@inheritDoc}
     */
    public boolean isIdentity()
    {
        return columnTranslator.isIdentity();
    }

    
    /**
     * {@inheritDoc}
     */
    public void write(PreparedStatement preparedStatement, int parameterIndex, R row) throws Exception
    {
        columnTranslator.write(preparedStatement, parameterIndex, row);
    }

    
    /**
     * {@inheritDoc}
     */
    public void read(ResultSet resultSet, int parameterIndex, R row) throws Exception
    {
        columnTranslator.read(resultSet, parameterIndex, row);
    }
}
