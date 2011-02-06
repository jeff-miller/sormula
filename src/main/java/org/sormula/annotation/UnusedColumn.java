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
package org.sormula.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Define column that requires values for insert or update but is not used by row object.
 * Used with {@linkplain UnusedColumns} annotation.
 * <p>
 * To insert null into unused column, omit UnusedColumn annotation. null values are not
 * permittted by Annotation syntax.
 * 
 * @since 1.0
 * @author Jeff Miller
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface UnusedColumn
{
    /**
     * @return column name
     */
    String name();

    
    /**
     * @return value to use within sql statement for column when inserting or updating record; use all required sql
     * syntax (for example surround character columns with single quotes like value="'ABC'")
     */
    String value() default "";
}
