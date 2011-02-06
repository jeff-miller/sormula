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
package org.sormula.annotation.cascade;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.sormula.annotation.OrderBy;
import org.sormula.annotation.Where;
import org.sormula.operation.ArrayListSelectOperation;
import org.sormula.operation.HashMapSelectOperation;
import org.sormula.operation.MapSelectOperation;
import org.sormula.operation.ScalarSelectOperation;


/**
 * Used within a {@linkplain Cascade} annotation to define a select operation on target
 * field in response to select operation that occurs on source row.  
 * 
 * @since 1.0
 * @author Jeff Miller
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
public @interface SelectCascade
{
    /**
     * Specifics operation to perform as a select cascade. Typical operations would be
     * {@link ScalarSelectOperation}, {@link ArrayListSelectOperation}, {@link HashMapSelectOperation}.
     * 
     * @return operation to use for cascade
     */
    Class <? extends ScalarSelectOperation> operation();
    

    /**
     * Specifies fields from source row to be used as parameters in cascade
     * operation for target row. Names must be in same order as defined in 
     * where condition on target row with {@link #targetWhereName()} or in same order 
     * as primary key fields of target row if primary key is used as where condition.
     * 
     * @return field names of fields to be used as parameters 
     */
    String[] sourceParameterFieldNames() default {};

    
    /**
     * Specifies the where condition to use in cascade. Name is one of names
     * specified on target class by {@linkplain Where#name()} or "primaryKey"
     * for primary key. An empty string indicates no where condition (select all).
     * 
     * @return where condition name of target row; "primaryKey" to select by
     * primary key
     */
    String targetWhereName() default "primaryKey"; 
    
    
    /**
     * Specifies the order condition to use in cascade. Name is one of names
     * specified on target class by {@linkplain OrderBy#name()} or empty string for
     * unordered.
     * 
     * @return order condition name of target row; empty string for none
     */
    String targetOrderByName() default "";

    
    /**
     * @return true to perform cascade after source row operation; false 
     * to perform cascade before source row operation
     */
    boolean post() default true;
    
    
    /**
     * Method name in target class that returns row key value for adding to map
     * results. Key method name is required when {@link #operation()} is instance of
     * {@link MapSelectOperation}; otherwise it is ignored.
     *  
     * @return name of method that gets key value from target row
     */
    String targetKeyMethodName() default "hashCode";
}
