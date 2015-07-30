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
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.sormula.Table;
import org.sormula.operation.SqlOperation;


/**
 * Defines columns to be used in a order phrase. Use this to annotate
 * a row class, {@link Table} subclass, {@link SqlOperation} or  
 * within {@link OrderBys} annotation.
 * <p>
 * Use only one of these methods: {@link #ascending()}, {@link #descending()}, or 
 * {@link #orderByFields()}.
 * 
 * @since 1.0
 * @author Jeff Miller
 */
@Inherited
@Repeatable(OrderBys.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface OrderBy
{
    /**
     * Name of the order by phrase. Must be unique among all order by names for a row class.
     *  
     * @return name of order phrase  
     */
    String name();
    
    
    /**
     * Define fields where all are ascending order. This is typical situation and allows 
     * for simpler annotation syntax. Use this instead of {@link #orderByFields()}.
     * 
     * @return array of field names for ascending order
     */
    String[] ascending() default {};
    
    
    /**
     * Define fields where all are descending order. This is typical situation and allows 
     * for simpler annotation syntax. Use this instead of {@link #orderByFields()}.
     * 
     * @return array of field names for descending order
     */
    String[] descending() default {};
    
    
    /**
     * Define fields that are mix of ascending and descending order. Use this instead of 
     * {@link #ascending()} or {@link #descending()}.
     * 
     * @return array of column conditions in order phrase
     */
    OrderByField[] orderByFields() default {};
}
