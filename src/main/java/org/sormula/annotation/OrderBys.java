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
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.sormula.Table;
import org.sormula.operation.SqlOperation;


/**
 * Defines two or more order by phrases for row class. Use this annotation if
 * more than one {@link OrderBy} is needed. Annotates a row class,
 * {@link Table} subclass, {@link SqlOperation} or within {@link OrderBy}.
 *
 * @since 1.0
 * @author Jeff Miller
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface OrderBys
{
    /**
     * Use {@link #value()}.
     */
    @Deprecated
    OrderBy[] orderByConditions() default {};
    
    
    /**
     * Order by conditions. value() is the default and does not need to be specified. 
     * <p> 
     * "default {}" allows either method to be used. It will be removed when 
     * {@link #orderByConditions()} is removed.
     * 
     * @return array of {@link OrderBy} annotations
     */
    OrderBy[] value()  default {};
}
