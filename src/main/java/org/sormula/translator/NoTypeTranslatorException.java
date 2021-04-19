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

import org.sormula.Database;
import org.sormula.Table;
import org.sormula.annotation.ImplicitType;
import org.sormula.translator.standard.StandardColumnTranslator;


/**
 * No type translator defined. Thrown by {@link StandardColumnTranslator}
 * when a nonstandard field type is used but no {@link TypeTranslator} has
 * been defined for nonstandard field type. A nonstandard type translator
 * may be defined by {@link ImplicitType}, {@link Database#putTypeTranslator(Class, TypeTranslator)}, or
 * {@link Table#putTypeTranslator(Class, TypeTranslator)}.
 * 
 * @since 1.0
 * @author Jeff Miller
 */
public class NoTypeTranslatorException extends TranslatorException
{
    private static final long serialVersionUID = 1L;
    

    /**
     * Constructs for a {@link Field} that does not have a {@link TypeTranslator}.
     * 
     * @param field field with no {@link TypeTranslator}
     */
    public NoTypeTranslatorException(Field field)
    {
        super("no type translator for field=" + field.getName() + 
                ", in class=" + field.getDeclaringClass().getCanonicalName()); 
    }
}
