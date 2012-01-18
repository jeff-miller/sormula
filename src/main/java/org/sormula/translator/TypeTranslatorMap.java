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



/**
 * TODO
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
     * @param <T> compile-time type of typeClass
     * @param typeClass class that translator operates upon
     * @param typeTranslator translator to use for typeClass
     */
    public <T> void putTypeTranslator(Class<T> typeClass, TypeTranslator<T> typeTranslator);

    
    /**
     * Gets the translator to use to convert a value to a prepared statement and to convert
     * a value from a result set.
     * 
     * @param <T> compile-time type of typeClass
     * @param typeClass class that translator operates upon
     * @return translator to use for typeClass
     */
    public <T> TypeTranslator<T> getTypeTranslator(Class<T> typeClass);
}
