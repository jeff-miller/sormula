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
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

import org.sormula.Database;


/**
 * A general purpose cascade annotation. It may be used on any class variable that is either scalar 
 * or a collection. Use this as an alternative to {@link OneToManyCascade} or {@link OneToOneCascade}. 
 * <p>
 * More than one operation is allowed per field even though it is not likely that you would
 * need more than one. {@link #selects()}, {@link #updates()}, {@link #inserts()},
 * and {@link #deletes()} accepts arrays which allow an empty array to mean "do nothing". 
 * 
 * @since 1.0
 * @author Jeff Miller
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Cascade
{
    /**
     * Class type of target field to affect. Used as parameter to {@link Database#getTable(Class)} to
     * get table for cascade operation. {@link #targetClass()} is optional for scalar fields since
     * target class can be obtained from target field at runtime.
     * <p>
     * For nonscalar target field types, like arrays, {@link Collection} types, and
     * {@link Map} types, targetClass can be determined from the field. Array types can be determined
     * through {@link Class#getComponentType()}. For collections and maps, target class can be determined
     * through the parameterized type with {@link Field#getGenericType()} if the generic type is not 
     * parameterized. For complex parameterized types, target class must be specified.
     * <p>
     * The following are examples of fields where targetClass=Something.class is optional in
     * cascade annotations because it can be determined through reflection:
     * <blockquote><pre>
     * Something scalar;
     * Something[] array;
     * List&lt;Something&gt; list;
     * Map&lt;String, Something&gt; map;
     * </pre></blockquote>
     * 
     * @return class of field that is involved in cascade; Object.class to indicate that class is
     * to be obtained from scalar field at runtime by relfection
     */
    Class<?> targetClass() default Object.class;
    
    
    /**
     * Select cascade operations for target fields. Use empty array
     * to perform no select cascades.
     * 
     * @return select annotations for cascade
     */
    SelectCascade[] selects() default {};
    
    
    /**
     * Insert cascade operations for target fields. Use empty array
     * to perform no insert cascades.
     * 
     * @return insert annotations for cascade
     */
    InsertCascade[] inserts() default {};
    
    
    /**
     * Update cascade operations for target fields. Use empty array
     * to perform no update cascades.
     * 
     * @return update annotations for cascade
     */
    UpdateCascade[] updates() default {};
    
    
    /**
     * Delete cascade operations for target fields. Use empty array
     * to perform no delete cascades.
     * 
     * @return delete annotations for cascade
     */
    DeleteCascade[] deletes() default {};
}
