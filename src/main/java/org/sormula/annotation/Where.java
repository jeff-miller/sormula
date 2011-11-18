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
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.sormula.operation.SqlOperation;


/**
 * Defines columns to be used in a where condition for a row class. Use this to annotate
 * a row class or within {@linkplain Wheres} annotation.
 * 
 * @since 1.0
 * @author Jeff Miller
 */
@Inherited
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Where
{
    /**
     * Name of where condition. Must be unique amoung all where conditions for a row class.
     * 
     * @return name of where condition
     * @see SqlOperation#setWhere(String)  
     */
    String name();
    
    
    /**
     * Define condition where all fields in the condition use "=" comparison operator and
     * are joined logically by "AND" boolean operator. This is a typical type of where condition
     * and allows for simpler annotation syntax. Use this instead of {@link #whereFields()}.
     * 
     * @return array of field names to be used in where condition
     */
    String[] fieldNames() default {};
    
    
    /**
     * Define condition where one or more fields use comparison operator or boolean operator other
     * than the defaults. Use this instead of {@link #fieldNames()}.
     * 
     * @return array of column conditions in where condition
     */
    WhereField[] whereFields() default {};
}
