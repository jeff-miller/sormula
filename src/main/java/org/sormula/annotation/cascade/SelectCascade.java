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
package org.sormula.annotation.cascade;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

import org.sormula.active.ActiveRecord;
import org.sormula.annotation.OrderBy;
import org.sormula.annotation.Where;
import org.sormula.operation.ArrayListSelectOperation;
import org.sormula.operation.HashMapSelectOperation;
import org.sormula.operation.MapSelectOperation;
import org.sormula.operation.ScalarSelectOperation;
import org.sormula.operation.cascade.lazy.AbstractLazySelector;
import org.sormula.operation.cascade.lazy.DurableLazySelector;
import org.sormula.operation.cascade.lazy.LazySelectable;
import org.sormula.operation.cascade.lazy.SimpleLazySelector;


/**
 * Used within a {@link Cascade} annotation to define a cascade select operation.
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
     * {@link ScalarSelectOperation}, {@link ArrayListSelectOperation}, {@link HashMapSelectOperation}
     * or some subclass of those.
     * <p>
     * The default value of {@link ArrayListSelectOperation} works correctly for source fields that 
     * are scalar or {@link List}. If the source field is not scalar or {@link List}, then some other
     * select operation must be used. 
     * 
     * @return operation to use for cascade
     */
    Class <? extends ScalarSelectOperation> operation() default ArrayListSelectOperation.class;
    

    /**
     * Specifies fields from source row to be used as parameters in cascade
     * operation for target row. Names must be in same order as defined in 
     * where condition on target row with {@link #targetWhereName()} or in same order 
     * as primary key fields of target row if primary key is used as where condition.
     * <p>
     * Use "#" to use field names defined by {@link #targetWhereName()}. Since "#" is default,
     * naming source and target fields the same allows sourceParameterFieldNames to be omitted.
     * 
     * @return field names of fields to be used as parameters; "#" to use field names from target where condition 
     */
    String[] sourceParameterFieldNames() default "#";

    
    /**
     * Specifies the where condition to use in cascade. Name is one of names
     * specified on target class by {@link Where#name()} or "primaryKey"
     * for primary key. An empty string indicates no where condition (select all).
     * Typically is the name of the where condition that uses foreign key(s) in
     * target row that correspond to primary key(s) in source row.
     * 
     * @return where condition name of target row; "primaryKey" to select by
     * primary key; empty string will select all rows
     */
    String targetWhereName() default "primaryKey"; 
    
    
    /**
     * Specifies the order condition to use in cascade. Name is one of names
     * specified on target class by {@link OrderBy#name()} or empty string for
     * unordered.
     * 
     * @return order condition name of target row; empty string for none
     */
    String targetOrderByName() default "";

    
    /**
     * Specifies when the cascade is to occur. This value is ignored if {@link #lazy()}
     * is true.
     * 
     * @return true to perform cascade after source row operation; false 
     * to perform cascade before source row operation
     */
    boolean post() default true;
    

    /**
     * Indicates that foreign key values should be set on target (child) rows during cascade.
     * Foreign key value fields are defined by {@link Cascade#foreignKeyValueFields()},
     * {@link OneToManyCascade#foreignKeyValueFields()}, or {@link OneToOneCascade#foreignKeyValueFields()}.
     * <p>
     * Foreign key values are set prior to cascade for insert, update, delete, and save cascades.
     * They are set after select cascades.
     * 
     * @return true to set foreign key values during cascade
     * @since 3.0
     */
    boolean setForeignKeyValues() default true;
    
    
    /**
     * Indicates that foreign key reference should be set on target (child) rows during cascade.
     * Foreign key reference field is defined by {@link Cascade#foreignKeyReferenceField()},
     * {@link OneToManyCascade#foreignKeyReferenceField()}, or {@link OneToOneCascade#foreignKeyReferenceField()}.
     * <p>
     * Foreign key reference is set prior to cascade for insert, update, delete, and save cascades.
     * It is set after select cascades.
     * 
     * @return true to set foreign key reference during cascade
     * @since 3.0
     */
    boolean setForeignKeyReference() default true;
    
    
    /**
     * Method name in target class that returns row key value for adding to map
     * results. Key method name is required when {@link #operation()} is instance of
     * {@link MapSelectOperation}; otherwise it is ignored.
     *  
     * @return name of method that gets key value from target row
     */
    String targetKeyMethodName() default "hashCode";
    
    
    /**
     * Marks a select cascade to be performed some later time after source row is selected.
     * The target row is selected when {@link LazySelectable#checkLazySelects(String)} is invoked 
     * or when {@link ActiveRecord#checkLazySelects(String)} is invoked.
     * 
     * @return true to skip select when source row is selected and add it to list of pending lazy select
     * fields; false to perform select cascade when source row is selected
     * @since 1.8 and 2.2
     * @see AbstractLazySelector
     * @see SimpleLazySelector
     * @see DurableLazySelector
     */
    boolean lazy() default false;
}
