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
package org.sormula.reflect;

import java.util.ArrayList;
import java.util.List;

import org.sormula.cache.CacheKey;
import org.sormula.translator.AbstractWhereTranslator;
import org.sormula.translator.ColumnTranslator;
import org.sormula.translator.RowTranslator;
import org.sormula.translator.TranslatorException;


/**
 * Utility to get field values from a row class as defined by {@link ColumnTranslator}. This class
 * is used by cache package classes to get a list of primary key values for use as a {@link CacheKey}. 
 * <p>
 * The constructor performs all of the reflection initialization once by creating {@link RowField}
 * objects for each desired field. Then {@link #getFieldValues(Object)} can simply invoke reflection 
 * methods to obtain the values for each row as needed.  
 * 
 * @author Jeff Miller
 * @since 3.0
 * @param <R> row type
 */
public class FieldExtractor<R> 
{
	List<RowField<R, ?>> rowFieldList;
    
    
    /**
     * Constructs for where translator columns.
     * 
     * @param whereTranslator use fields from this translator
     * @since 3.4
     */
    public FieldExtractor(AbstractWhereTranslator<R> whereTranslator) throws ReflectException
    {
        init(whereTranslator.getRowTranslator(), whereTranslator.getColumnTranslatorList());
    }
    
    
    /**
     * Constructs for all fields in row.
     * 
     * @param rowTranslator use fields from this translator
     * @since 3.4
     */
    public FieldExtractor(RowTranslator<R> rowTranslator) throws ReflectException
    {
        init(rowTranslator, rowTranslator.getColumnTranslatorList());
    }
    
    
    private void init(RowTranslator<R> rowTranslator, List<ColumnTranslator<R>> columnTranslatorList) throws ReflectException
    {
        rowFieldList = new ArrayList<RowField<R, ?>>(columnTranslatorList.size());
        
        try
        {
            for (ColumnTranslator<R> c: columnTranslatorList)
            {
                // create with row translator to get the correct type of field access
                rowFieldList.add(rowTranslator.createRowField(c.getField()));
            }
        }
        catch (TranslatorException e)
        {
            throw new ReflectException("initialization error", e);
        }
    }

    
    /**
     * Gets the values of fields for a row.
     * 
     * @param row obtain values from this row
     * @return field values as Object array
     */
    public Object[] getFieldValues(R row) throws ReflectException
    {
    	Object[] result = new Object[rowFieldList.size()];
    	int i = 0;
    	
    	for (RowField<R, ?> s : rowFieldList)
    	{
    		//log.info("f=" + c.getField());
    		result[i++] = s.get(row);
    	}
    	
    	return result;
    }
}
