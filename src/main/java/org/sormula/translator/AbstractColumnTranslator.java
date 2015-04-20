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
package org.sormula.translator;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.sormula.annotation.Column;
import org.sormula.log.ClassLogger;
import org.sormula.reflect.ReflectException;
import org.sormula.reflect.RowField;
import org.sormula.reflect.SormulaField;
import org.sormula.translator.standard.ObjectTranslator;


/**
 * Common functionality for most translators.
 *
 * @since 1.0
 * @author Jeff Miller
 * @param <R> row class
 * @param <T> Java field type
 */
public abstract class AbstractColumnTranslator<R, T> implements ColumnTranslator<R>
{
    private static final ClassLogger log = new ClassLogger();
    
    String columnName;
    SormulaField<R, T> sormulaField;
    TypeTranslator<T> typeTranslator;
    boolean identity;
    boolean readOnly;
    
    
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
    @Deprecated
	public static ColumnTranslator<?> newInstance(Class<? extends ColumnTranslator> columnTranslatorClass, 
	        Field field, String columnName) throws TranslatorException
    {
        try
        {
            if (log.isDebugEnabled()) log.debug(field + " column="+columnName + 
                    " translator is "+columnTranslatorClass.getCanonicalName());
            Constructor<? extends ColumnTranslator> constructor = columnTranslatorClass.getConstructor(Field.class, String.class);
            return constructor.newInstance(field, columnName);
        }
        catch (Exception e)
        {
            throw new TranslatorException("error creating translator for " + field, e);
        }
    }
    
    
    /**
     * Factory method for creating a new instance of a column translator for a row field.
     * Gets columnTranslatorClass constructor with parameters (RowField, String) and invokes
     * {@link Class#newInstance()}. {@link RowField} is needed instead of {@link Field} so
     * that appropriate field access is used.
     * 
     * @param columnTranslatorClass translator
     * @param rowField row field of row class to use in translator
     * @param columnName name of column associated with rowfield parameter
     * @return column translator
     * @throws TranslatorException if error
     * @since 3.4
     */
    public static ColumnTranslator<?> newInstance(Class<? extends ColumnTranslator> columnTranslatorClass, 
            RowField<?, ?> rowField, String columnName) throws TranslatorException
    {
        try
        {
            if (log.isDebugEnabled()) log.debug(rowField.getField() + " column="+columnName + 
                    " translator is "+columnTranslatorClass.getCanonicalName());
            Constructor<? extends ColumnTranslator> constructor = columnTranslatorClass.getConstructor(RowField.class, String.class);
            return constructor.newInstance(rowField, columnName);
        }
        catch (NoSuchMethodException e)
        {
            // this is a temporary work-around to allow pre 3.4 ColumnTranslators to work
            // remove this block when pre 3.4 constructor is deprecated
            // assume pre 3.4 translator, use deprecated constructor
            return newInstance(columnTranslatorClass, rowField.getField(), columnName);
        }
        catch (Exception e)
        {
            throw new TranslatorException("error creating translator for " + rowField.getField(), e);
        }
    }
    
    
    /**
     * Constructs for a column. Translates with {@link ObjectTranslator}.
     * 
     * @param field java reflection Field that corresponds to column
     * @param columnName name of table column
     * @throws TranslatorException if error
     */
	@Deprecated
    public AbstractColumnTranslator(Field field, String columnName) throws TranslatorException
    {
        this.columnName = columnName;
        
        try
        {
            sormulaField = new SormulaField<>(field);
            Column columnAnnotation = field.getAnnotation(Column.class);
            if (columnAnnotation != null)
            {
                setIdentity(columnAnnotation.identity());
                setReadOnly(columnAnnotation.readOnly());
            }
        }
        catch (ReflectException e)
        {
            throw new TranslatorException("error creating access to field " + field, e);
        }
        
        @SuppressWarnings("unchecked")
        TypeTranslator<T> objectTranslator = (TypeTranslator<T>)new ObjectTranslator();
        setTypeTranslator(objectTranslator);
    }
    
    
    /**
     * Constructs for a column. Translates with {@link ObjectTranslator} for default.
     * 
     * @param field java reflection Field that corresponds to column
     * @param columnName name of table column
     * @throws TranslatorException if error
     * @since 3.4
     */
    public AbstractColumnTranslator(RowField<R, T> rowfield, String columnName) throws TranslatorException
    {
        this.columnName = columnName;
        this.sormulaField = rowfield;
    
        Column columnAnnotation = rowfield.getField().getAnnotation(Column.class);
        if (columnAnnotation != null)
        {
            setIdentity(columnAnnotation.identity());
            setReadOnly(columnAnnotation.readOnly());
        }
        
        @SuppressWarnings("unchecked")
        TypeTranslator<T> objectTranslator = (TypeTranslator<T>)new ObjectTranslator();
        setTypeTranslator(objectTranslator);
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
     * @return translator to read result sets and write to prepared statements
     * @since 1.6 and 2.0
     */
    public TypeTranslator<T> getTypeTranslator()
    {
        return typeTranslator;
    }


    /**
     * Sets the translator to read result sets and write to prepared statements.
     * 
     * @param typeTranslator type translator to use for this column
     * @since 1.6 and 2.0
     */
    public void setTypeTranslator(TypeTranslator<T> typeTranslator)
    {
        this.typeTranslator = typeTranslator;
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
     * {@inheritDoc}
     */
    public boolean isReadOnly()
    {
        return readOnly;
    }
    public void setReadOnly(boolean readOnly)
    {
        this.readOnly = readOnly;
    }
    

    /**
     * Gets the field as a {@link SormulaField}. Use {@link #getRowField()} instead of
     * this method.
     * 
     * @return {@link SormulaField} for field supplied in constructor
     */
    @Deprecated
    public SormulaField<R, T> getSormulaField()
    {
        return sormulaField;
    }

    
    /**
     * Gets the {@link RowField} for field in this translator.
     * 
     * @return {@link RowField} for field supplied in constructor
     * @since 3.4
     */
    public RowField<R, T> getRowField()
    {
        // note: cast will fail if deprecated constructor was used
        return (RowField<R, T>)sormulaField;
    }

    
    /**
     * {@inheritDoc}
     */
    public void write(PreparedStatement preparedStatement, int parameterIndex, R row) throws Exception
    {
        if (typeTranslator != null)
        {
            typeTranslator.write(preparedStatement, parameterIndex, sormulaField.get(row));
        }
        else
        {
            throw new NoTypeTranslatorException(getField());
        }
    }
    
    
    /**
     * {@inheritDoc}
     */
    public void read(ResultSet resultSet, int columnIndex, R row) throws Exception
    {
        if (typeTranslator != null)
        {
            sormulaField.set(row, typeTranslator.read(resultSet, columnIndex));
        }
        else
        {
            throw new NoTypeTranslatorException(getField());
        }
    }
}
