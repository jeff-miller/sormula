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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.sormula.annotation.Column;
import org.sormula.annotation.Row;
import org.sormula.annotation.Transient;


/**
 * Translates values from row object to JDBC parameters for a where condition as defined
 * by {@link Column#primaryKey()} or {@link Column#identity()} annotations or
 * by {@link Row#primaryKeyFields()}.
 * 
 * @since 1.0
 * @author Jeff Miller
 * @param <R> row class
 */
public class PrimaryKeyWhereTranslator<R> extends AbstractWhereTranslator<R>
{
    /**
     * Constructs based upon a row translator. If {@link Row#primaryKeyFields()} is not empty, then
     * fields from {@link Row#primaryKeyFields()} are used. Otherwise {@link Column} for class
     * fields are used. If {@link Row#primaryKeyFields()} is empty and no {@link Column} annotation 
     * exists for row, then the first declared field will be used as the primary key.
     * 
     * @param rowTranslator obtain primary key information from this translator
     * @throws TranslatorException if error
     */
    public PrimaryKeyWhereTranslator(RowTranslator<R> rowTranslator) throws TranslatorException
    {
        super(rowTranslator); 
        initColumnTranslatorList(4);
        
        Row rowAnnotation = rowTranslator.getRowClass().getAnnotation(Row.class);
        
        if (rowAnnotation != null && rowAnnotation.primaryKeyFields().length > 0)
        {
            // primary keys defined by Row annotation
            for (String fieldName : rowAnnotation.primaryKeyFields())
            {
                Field field = rowTranslator.getDeclaredField(fieldName);
                if (field != null)
                {
                    addColumnTranslator(field, "primary key");
                }
                else
                {
                    throw new TranslatorException("no field=" + fieldName + ", in class=" + 
                            rowClass.getCanonicalName() + ", for Row#primaryKeyFields");
                }
            }
        }
        else
        {
            // search for primary key(s) from Column annotations
            Field firstField = null;
            
            // for all fields
            for (Field f: rowTranslator.getDeclaredFields())
            {
                if (Modifier.isStatic(f.getModifiers())) continue; // static are never primary keys
                if (f.isAnnotationPresent(Transient.class)) continue; // transient are never primary keys
                
                if (firstField == null) firstField = f;
                
                Column columnAnnotation = f.getAnnotation(Column.class);
                if (columnAnnotation != null && (columnAnnotation.primaryKey() || columnAnnotation.identity()))
                {
                    addColumnTranslator(f, "primary key");
                }
            }
            
            if (getColumnTranslatorList().size() == 0 && firstField != null)
            {
                // no primary key specificed, assume first non-static, non-transient field
                addColumnTranslator(firstField, "default");
            }
        }
    }
    

    /**
     * Adds a {@link ColumnTranslator} for a field. Invoked by constructor.
     * 
     * @param f add translator for this field
     * @param annotationName annotation name to display in exception message
     * @throws TranslatorException if no column translator exists for field
     */
    protected void addColumnTranslator(Field f, String annotationName) throws TranslatorException
    {
        ColumnTranslator<R> columnTranslator = rowTranslator.getColumnTranslator(f.getName());
        
        if (columnTranslator != null)
        {
            addColumnTranslator(columnTranslator);
        }
        else
        {
            throw new NoColumnTranslatorException(rowTranslator.getRowClass(), f.getName(), annotationName);
        }
    }
}
