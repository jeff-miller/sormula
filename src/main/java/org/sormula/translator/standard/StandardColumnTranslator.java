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

import java.lang.reflect.Field;

import org.sormula.reflect.RowField;
import org.sormula.translator.AbstractColumnTranslator;


/**
 * Delegates to translator based upon field type. This translator should work for all
 * standard Java data types. 
 * 
 * @since 1.0
 * @author Jeff Miller
 * @param <R> row class for table
 * @param <T> Java field type
 */
public class StandardColumnTranslator<R, T> extends AbstractColumnTranslator<R, T>
{
    @Deprecated
    public StandardColumnTranslator(Field field, String columnName) throws Exception
    {
        super(field, columnName);
    }
    
    
    /**
     * Constructs for a {@link RowField} and column name.
     * 
     * @param rowField field within row class that contains the value of sql column
     * @param columnName sql column name associated with rowField
     * @throws Exception if error
     * @since 3.4
     */
    public StandardColumnTranslator(RowField<R, T> rowField, String columnName) throws Exception
    {
        super(rowField, columnName);
    }
}
