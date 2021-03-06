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

import org.sormula.annotation.Where;
import org.sormula.annotation.WhereField;


/**
 * Translates row values to where condition parameters.
 * 
 * @since 1.0
 * @author Jeff Miller
 * @param <R> row class
 */
public class WhereTranslator<R> extends AbstractWhereTranslator<R>
{
    /**
     * Constructs and empty where condition. Add conditions with {@link #addColumnTranslator(ColumnTranslator)}
     * and {@link #addColumnTranslator(ColumnTranslator, String, String, String)}.
     * 
     * @param rowTranslator row translator for where condition
     * @param initialCapacity approximate number of columns that will be in translator
     * @throws TranslatorException if error
     * @since 3.1
     */
    public WhereTranslator(RowTranslator<R> rowTranslator, int initialCapacity) throws TranslatorException
    {
        super(rowTranslator);
        initColumnTranslatorList(initialCapacity);
    }

    
    /**
     * Constructs for where annotation. 
     * 
     * @param rowTranslator row translator for where condition
     * @param whereAnnotation annotation with where information
     * @throws TranslatorException if error
     */
    public WhereTranslator(RowTranslator<R> rowTranslator, Where whereAnnotation) throws TranslatorException
    {
        super(rowTranslator);
        init(rowTranslator, whereAnnotation);
    }
    
    
    /**
     * Initializes. Invoked by constructors.
     * 
     * @param rowTranslator row translator for where condition
     * @param whereAnnotation annotation with where information
     * @throws TranslatorException if error
     */
    protected void init(RowTranslator<R> rowTranslator, Where whereAnnotation) throws TranslatorException
    {
        // add translators for each column in where condition
        String [] fieldNames = whereAnnotation.fieldNames(); 
        
        if (fieldNames.length > 0)
        {
            // simple where condition, operator is "=" for each column
            initColumnTranslatorList(fieldNames.length);
            for (String fn: fieldNames)
            {
                ColumnTranslator<R> columnTranslator = rowTranslator.getColumnTranslator(fn);
                
                if (columnTranslator != null)
                {
                    addColumnTranslator(columnTranslator);
                }
                else
                {
                    throw new NoColumnTranslatorException(rowTranslator.getRowClass(), fn, 
                            "where condition named, " + whereAnnotation.name());
                }
            }
        }
        else
        {
            // complex where condition, custom operator for each column
            WhereField[] whereFields = whereAnnotation.whereFields();
            initColumnTranslatorList(whereFields.length);

            for (WhereField wf: whereFields)
            {
                ColumnTranslator<R> columnTranslator = rowTranslator.getColumnTranslator(wf.name());
                
                if (columnTranslator != null)
                {
                    addColumnTranslator(columnTranslator, wf.booleanOperator(), wf.comparisonOperator(), wf.operand());
                }
                else
                {
                    throw new NoColumnTranslatorException(rowTranslator.getRowClass(), wf.name(), 
                            "where condition named, " + whereAnnotation.name());
                }
            }
        }
    }
}
