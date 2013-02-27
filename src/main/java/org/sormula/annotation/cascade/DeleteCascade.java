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

import org.sormula.operation.DeleteOperation;


/**
 * Used within a {@link Cascade} annotation to define a cascade delete operation.
 * 
 * @since 1.0
 * @author Jeff Miller
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
public @interface DeleteCascade
{
    /**
     * Specifies operation to perform as a delete cascade. Typically it is
     * {@link DeleteOperation}. Use subclass of {@link DeleteOperation} to
     * customize the delete cascade.
     * 
     * @return operation to use for cascade
     */
    Class <? extends DeleteOperation> operation() default DeleteOperation.class;
    
    
    /**
     * Specifies when the cascade is to occur. Note that default is false which
     * is the inverse of other cascade annotations since child row deletes
     * typically occur prior to parent row to maintain foreign key constraints.
     * 
     * @return true to perform cascade after source row operation; false 
     * to perform cascade before source row operation
     */
    boolean post() default false;
    

    /**
     * Indicates that foreign key values should be set on target (child) rows during cascade.
     * Foreign key value fields are defined by {@link Cascade#foreignKeyValueFields()},
     * {@link OneToManyCascade#foreignKeyValueFields()}, or {@link OneToOneCascade#foreignKeyValueFields()}.
     * <p>
     * Foreign key values are set prior to cascade for insert, update, delete, and save cascades.
     * They are set after select cascades.
     * <p>
     * Default is false for delete cascade since typically foreign keys are not needed when deleting.
     * 
     * @return true to set foreign key values during cascade
     * @since 3.0
     */
    boolean setForeignKeyValues() default false; 
    
    
    /**
     * Indicates that foreign key reference should be set on target (child) rows during cascade.
     * Foreign key reference field is defined by {@link Cascade#foreignKeyReferenceField()},
     * {@link OneToManyCascade#foreignKeyReferenceField()}, or {@link OneToOneCascade#foreignKeyReferenceField()}.
     * <p>
     * Foreign key reference is set prior to cascade for insert, update, delete, and save cascades.
     * It is set after select cascades.
     * <p>
     * Default is false for delete cascade since typically foreign keys are not needed when deleting.
     * 
     * @return true to set foreign key reference during cascade
     * @since 3.0
     */
    boolean setForeignKeyReference() default false;
}
