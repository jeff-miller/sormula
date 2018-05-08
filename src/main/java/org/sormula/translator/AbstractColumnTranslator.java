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
import org.sormula.log.SormulaLogger;
import org.sormula.log.SormulaLoggerFactory;
import org.sormula.reflect.RowField;
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
    private static final SormulaLogger log = SormulaLoggerFactory.getClassLogger();
    
    String columnName;
    RowField<R, T> rowField;
    TypeTranslator<T> typeTranslator;
    boolean identity;
    boolean readOnly;
    
    
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
        catch (Exception e)
        {
            throw new TranslatorException("error creating translator for " + rowField.getField(), e);
        }
    }
    
    
    /**
     * Constructs for a column. Translates with {@link ObjectTranslator} for default.
     * 
     * @param rowfield field within row that corresponds to column 
     * @param columnName name of table column associated with rowField
     * @throws TranslatorException if error
     * @since 3.4
     */
    public AbstractColumnTranslator(RowField<R, T> rowfield, String columnName) throws TranslatorException
    {
        this.columnName = columnName;
        this.rowField = rowfield;
    
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
        return rowField.getField();
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
     * Gets the {@link RowField} for field in this translator.
     * 
     * @return {@link RowField} for field supplied in constructor
     * @since 3.4
     */
    public RowField<R, T> getRowField()
    {
        return (RowField<R, T>)rowField;
    }

    
    /**
     * {@inheritDoc}
     */
    public void write(PreparedStatement preparedStatement, int parameterIndex, R row) throws Exception
    {
        if (typeTranslator != null)
        {
            typeTranslator.write(preparedStatement, parameterIndex, rowField.get(row));
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
            rowField.set(row, typeTranslator.read(resultSet, columnIndex));
        }
        else
        {
            throw new NoTypeTranslatorException(getField());
        }
    }
}
