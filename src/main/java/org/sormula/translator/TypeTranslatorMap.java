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

import org.sormula.annotation.ExplicitType;
import org.sormula.annotation.ImplicitType;



/**
 * Interface for classes that add and retrieve {@link TypeTranslator} using a
 * key of {@link Class}. Methods use wildcard type since custom translator types
 * are not known when obtained from annotations like {@link ExplicitType} or
 * {@link ImplicitType}. 
 * 
 * @author Jeff Miller
 * @since 1.6
 */
public interface TypeTranslatorMap
{
    /**
     * Defines the translator to use to convert a value to a prepared statement or to convert
     * a value from a result set.  
     * 
     * @param typeClass class that translator operates upon
     * @param typeTranslator translator to use for typeClass
     */
    public void putTypeTranslator(Class<?> typeClass, TypeTranslator<?> typeTranslator);
    
    
    /**
     * Same as {@link #putTypeTranslator(Class, TypeTranslator)} but uses class name. Usefull for adding
     * primative types like "int", "boolean", "float", etc.
     * 
     * @param typeClassName class name that translator operates upon
     * @param typeTranslator translator to use for typeClass
     * @since 1.9.2
     */
    public void putTypeTranslator(String typeClassName, TypeTranslator<?> typeTranslator);

    
    /**
     * Gets the translator to use to convert a value to a prepared statement and to convert
     * a value from a result set.
     * 
     * @param typeClass class that translator operates upon
     * @return translator to use for typeClass
     */
    public TypeTranslator<?> getTypeTranslator(Class<?> typeClass);
}
