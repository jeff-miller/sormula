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
package org.sormula.translator;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import org.sormula.annotation.Column;
import org.sormula.log.ClassLogger;
import org.sormula.reflect.ReflectException;
import org.sormula.reflect.SormulaField;


/**
 * Common functionality for most translators.
 *
 * @since 1.0
 * @author Jeff Miller
 * @param <R> row class
 * @param <T> java field type
 */
public abstract class AbstractColumnTranslator<R, T> implements ColumnTranslator<R>
{
    private static final ClassLogger log = new ClassLogger();
    
    String columnName;
    SormulaField<R, T> sormulaField;
    boolean identity;
    
    
    /**
     * Factory method for creating a new instance of a column translator for a field.
     * Gets columnTranslatorClass constructor with parameters (Field, String) and invokes
     * {@link Class#newInstance()}.
     * 
     * @param columnTranslatorClass translator
     * @param field field within row class
     * @param columnName name of column associated with row field
     * @return column translator
     * @throws TranslatorException if error
     */
	public static ColumnTranslator<?> newInstance(Class<? extends ColumnTranslator> columnTranslatorClass, 
	        Field field, String columnName) throws TranslatorException
    {
        try
        {
            if (log.isDebugEnabled()) log.debug(field.getDeclaringClass().getCanonicalName() + "#" + field.getName() + " column="+columnName + 
                    " translator is "+columnTranslatorClass.getCanonicalName());
            Constructor<? extends ColumnTranslator> constructor = columnTranslatorClass.getConstructor(Field.class, String.class);
            return constructor.newInstance(field, columnName);
        }
        catch (Exception e)
        {
            throw new TranslatorException("error creating translator for " + field.getDeclaringClass().getCanonicalName() + "#" + field.getName(), e);
        }
    }
    
    
    /**
     * Constructs for a column.
     * 
     * @param field java reflection Field that corresponds to column
     * @param columnName name of table column
     * @throws TranslatorException if error
     */
    public AbstractColumnTranslator(Field field, String columnName) throws TranslatorException
    {
        try
        {
            sormulaField = new SormulaField<R, T>(field);
            Column columnAnnotation = field.getAnnotation(Column.class);
            setIdentity(columnAnnotation != null && columnAnnotation.identity());
        }
        catch (ReflectException e)
        {
            throw new TranslatorException("error creating access to field " + field.getName(), e);
        }
        
        this.columnName = columnName;
    }
    
    
    /**
     * {@inheritDoc}
     */
    public Field getField()
    {
        return sormulaField.getField();
    }


    /**
     * {@inheritDoc}
     */
    public String getColumnName()
    {
        return columnName;
    }
    
    
    /**
     * {@inheritDoc}
     */
    public boolean isIdentity()
    {
        return identity;
    }
    public void setIdentity(boolean identity)
    {
        this.identity = identity;
    }


    /**
     * Gets the field as a {@link SormulaField}.
     * 
     * @return {@link SormulaField} for field supplied in constructor
     */
    public SormulaField<R, T> getSormulaField()
    {
        return sormulaField;
    }
}
