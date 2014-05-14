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
 * Defines how to map Enum to/from a table column.
 * <p>
 * EnumType is optional since {@link EnumTranslator} is the default translator to use for 
 * an Enum field when no annotation is specified. 
 * <p>
 * Use {@link EnumType} to provide an alternate translator with
 * {@link #translator()} or to define a default Enum to use when the column name read is
 * not a valid Enum as determined by {@link Class#getEnumConstants()}.
 * <p>
 * Example:
 * <blockquote><pre>
 * public class SomeRowClass
 * {
 *     &#64;EnumType(defaultEnumName="Bad")
 *     SomeEnum e;
 *     ...
 * } 
 * 
 * public enum SomeEnum
 * {
 *     Good, Bad, Ugly;
 * }
 * </pre></blockquote>
 * 
 * @since 3.3
 * @author Jeff Miller
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface EnumType
{
    /**
     * Type translator to use to convert Enum types to/from column.
     * 
     * @return EnumTranslator to use 
     */
    Class<? extends EnumTranslator> translator() default EnumTranslator.class;
    
    
    /**
     * The name of the Enum ({@link Enum#name()}) for the default Enum. The name
     * supplied here determines the Enum to return from 
     * {@link EnumTranslator#read(java.sql.ResultSet, int)} when database column 
     * does not match any Enum names. Name is case sensitive.
     *  
     * @return non empty string means return Enum corresponding to the non empty string;
     * empty string means use null for default Enum
     */
     String defaultEnumName() default ""; 
}
