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


/**
 * Failed column translator lookup exception. Thrown by translators when a column translator
 * is expected for a row member (field) but none are defined.
 * 
 * @since 1.0
 * @author Jeff Miller
 */
public class NoColumnTranslatorException extends TranslatorException
{
    private static final long serialVersionUID = 1L;
    

    public NoColumnTranslatorException(Class<?> rowClass, String fieldName, String annotationName)
    {
        super("no column translator for field=" + fieldName + ", in class=" + rowClass.getCanonicalName() +
                ", for annotation=" + annotationName); 
    }
}
