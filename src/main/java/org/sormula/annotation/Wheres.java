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


/**
 * Defines two or more {@link Where} annotations for a row class. Annotates a row class.
 *
 * @since 1.0
 * @author Jeff Miller
 */
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Wheres
{
    /**
     * Use {@link #value()}.
     */
    @Deprecated
    Where[] whereConditions() default {};
    
    
    /**
     * Where conditions. value() is the default and does not need to be specified.
     * <p> 
     * "default {}" allows either method to be used. It will be removed when 
     * {@link #whereConditions()} is removed.
     * 
     * @return array of {@link Where} annotations for a row class
     */
    Where[] value() default {};
}
