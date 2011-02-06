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
import java.util.Collection;

import org.sormula.Database;


/**
 * Defines cascade annotations for a class variable that is either scalar or a collection. 
 * 
 * @since 1.0
 * @author Jeff Miller
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Cascade
{
    /**
     * Class type of target field to affect. Used as parameter to {@linkplain Database#getTable(Class)} to
     * get table for cascade operation. If Object.class is used and target field is scalar, then target class 
     * is obtained from target field at runtime. For parameterized target field types, like {@link Collection}
     * types, targetClass() must be specified.
     * 
     * @return class of field that is involved in cascade; Object.class to indicate that class is
     * to be obtained from scalar field at runtime by relfection
     */
    Class<?> targetClass() default Object.class;
    
    
    /**
     * Select cascade operation for target fields of any type.
     * 
     * @return select annotations for cascade
     */
    SelectCascade[] selects() default {};
    
    
    /**
     * @return insert annotations for cascade
     */
    InsertCascade[] inserts() default {};
    
    
    /**
     * @return update annotations for cascade
     */
    UpdateCascade[] updates() default {};
    
    
    /**
     * @return delete annotations for cascade
     */
    DeleteCascade[] deletes() default {};
}
