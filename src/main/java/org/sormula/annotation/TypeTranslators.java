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

import org.sormula.Table;


/**
 * TODO
 * Defines columns that are required for a table but are not used by row object. Allows updates
 * and inserts into a table where columns are required but not used by row class. 
 * Annotates a row class or {@link Table} subclass.
 * 
 * @since 1.6
 * @author Jeff Miller
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TypeTranslators
{
    /**
     * TODO
     * Columns that are not used by row class. value() is the default and does not need to be specified. 
     * <p> 
     * "default {}" allows either method to be used. It will be removed when 
     * {@link #unusedColumns()} is removed.
     * 
     * @return array of {@link ExplicitType} annotations for row class
     */
    ExplicitType[] value();
}
