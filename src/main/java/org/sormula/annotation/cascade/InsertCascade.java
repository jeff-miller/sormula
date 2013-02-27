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
     * Indicates that foreign key values should be set on target (child) rows during cascade.
     * Foreign key value fields are defined by {@link Cascade#foreignKeyValueFields()},
     * {@link OneToManyCascade#foreignKeyValueFields()}, or {@link OneToOneCascade#foreignKeyValueFields()}.
     * <p>
     * Foreign key values are set prior to cascade for insert, update, delete, and save cascades.
     * They are set after select cascades.
     * <p>
     * This value is particularly useful when parent key is created based upon an identity column
     * {@link Column#identity()} is true. Child row's parent foreign key will be set when parent is
     * inserted.
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
}
