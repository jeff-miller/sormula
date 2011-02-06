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

import java.lang.reflect.Field;

import org.sormula.annotation.Column;


/**
 * Translates values from row object to jdbc parameters for a where condition as defined
 * by {@linkplain Column#primaryKey()} annotations.
 * 
 * @since 1.0
 * @author Jeff Miller
 * @param <R> row class
 */
public class PrimaryKeyWhereTranslator<R> extends AbstractWhereTranslator<R>
{
    /**
     * Constructs based upon a row translator.
     * 
     * @param rowTranslator obtain primary key information from this translator
     * @throws TranslatorException if error
     */
    public PrimaryKeyWhereTranslator(RowTranslator<R> rowTranslator) throws TranslatorException
    {
        super(rowTranslator); 
        initColumnTranslatorList(4);
        
        // for all fields
        for (Field f: rowTranslator.getRowClass().getDeclaredFields())
        {
            Column columnAnnotation = f.getAnnotation(Column.class);

            if (columnAnnotation != null && columnAnnotation.primaryKey())
            {
                ColumnTranslator<R> columnTranslator = rowTranslator.getColumnTranslator(f.getName());
                
                if (columnTranslator != null)
                {
                    addColumnTranslator(columnTranslator);
                }
                else
                {
                    throw new NoColumnTranslatorException(rowTranslator.getRowClass(), f.getName(), "primary key");
                }
            }
        }
    }
}
