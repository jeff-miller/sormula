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
package org.sormula.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.sormula.translator.NameTranslator;
import org.sormula.translator.NoNameTranslator;


/**
 * Annotates a row class. Defines class attributes for a row class. This annotation
 * is only required if table name is different from class name or if a name
 * translator is needed.
 * 
 * @since 1.0
 * @author Jeff Miller
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Row
{
    /**
     * @return name of table associated with row; if not specified then tableName is 
     * {@link Class#getSimpleName()} or by {@link NameTranslator}
     */
    String tableName() default "";
    
    
    /**
     * @return translator for mapping java names to sql names
     */
    Class<? extends NameTranslator> nameTranslator() default NoNameTranslator.class;
}
