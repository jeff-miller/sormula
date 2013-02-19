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

import org.sormula.annotation.Column;
import org.sormula.operation.InsertOperation;


/**
 * Used within a {@link Cascade} annotation to define a cascade insert operation.  
 * 
 * @since 1.0
 * @author Jeff Miller
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface InsertCascade
{
    /**
     * Specifies operation to perform as a insert cascade. Typically it is
     * {@link InsertOperation}. Use subclass of {@link InsertOperation} to
     * customize the insert cascade.
     * 
     * @return operation to use for cascade
     */
    Class <? extends InsertOperation> operation() default InsertOperation.class;
    
    
    /**
     * Specifies when the cascade is to occur.
     * 
     * @return true to perform cascade after source row operation; false 
     * to perform cascade before source row operation
     */
    boolean post() default true;
    
    
    /**
     * Indicates that insert cascade should set foreign key(s) on target (child) rows before
     * they are inserted. When target (parent) row is cascaded, then each target (child) row
     * foreign key setters are invoked with values from source (parent) primary key. 
     * <p>
     * Source row key(s) are primary keys in source row where {@link Column#primaryKey()} is true.
     * <p>
     * When asterisk (*) is used, then cascade assumes that source key field names are the
     * same as target (child) key field names. For example: Parent.parentId --> Child.parentId.
     * <p>
     * If explicit fields are named, then they must be in same order as source row key fields.
     * 
     * @return names of foreign key fields in child (target) row; asterisk "*" means use
     * same foreign key names as source (parent) field names; empty array means don't set foreign 
     * key(s) on target rows
     * @since 3.0
     */
    String[] targetForeignKeyFields() default {};
}
