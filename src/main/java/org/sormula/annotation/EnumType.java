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
package org.sormula.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.sormula.translator.standard.EnumTranslator;


/**
 * TODO
 * 
 * @since 3.3
 * @author Jeff Miller
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface EnumType
{
    /**
     * @return type translator to use on class
     */
    Class<? extends EnumTranslator> translator() default EnumTranslator.class;
    
    
    /**
     * TODO
     * note case sensitive
     * 
     * Enum to return from {@link EnumTranslator#read(java.sql.ResultSet, int)} when
     * database column does not match any enum names. 
     * "" means return null 
     * non empty string means return enum corresponding to the non empty string 
     * 
     * @return
     */
     String defaultEnumName() default ""; 
}
