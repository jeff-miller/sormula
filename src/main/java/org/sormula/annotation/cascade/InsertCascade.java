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
    

    // TODO performs update if true and foreign info defined in Cascade, OneToOneCascade, OneToManyCascade
    boolean setForeignKeyValues() default true;
    boolean setForeignKeyReference() default true;
}
