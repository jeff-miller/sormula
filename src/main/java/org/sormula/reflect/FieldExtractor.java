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
import org.sormula.translator.ColumnTranslator;


/**
 * Utility to get field values from a row class as defined by {@link ColumnTranslator}. This class
 * is used by cache package classes to get a list of primary key values for use as a {@link CacheKey}. 
 * <p>
 * The constructor performs all of the reflection instialization once by creating {@link SormulaField}
 * objects for each desired field. Then {@link #getFieldValues(Object)} can simply invoke reflection 
 * methods to obtain the values for each row as needed.  
 * 
 * @author Jeff Miller
 * @since 3.0
 * @param <R> row type
 */
public class FieldExtractor<R> 
{
	List<SormulaField<R, Object>> sormulaFieldList;
	
	
	/**
	 * Constructs from a list of {@link ColumnTranslator}.
	 * 
	 * @param columnTranslatorList list of fields that are read/written to database
	 * @throws ReflectException if error
	 */
    public FieldExtractor(List<ColumnTranslator<R>> columnTranslatorList) throws ReflectException
    {
    	sormulaFieldList = new ArrayList<SormulaField<R, Object>>(columnTranslatorList.size());
    	
    	for (ColumnTranslator<R> c: columnTranslatorList)
    	{
    		sormulaFieldList.add(new SormulaField<R, Object>(c.getField()));
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
    	Object[] result = new Object[sormulaFieldList.size()];
    	int i = 0;
    	
    	for (SormulaField<R, Object> s : sormulaFieldList)
    	{
    		//log.info("f=" + c.getField());
    		result[i++] = s.invokeGetMethod(row);
    	}
    	
    	return result;
    }
}
