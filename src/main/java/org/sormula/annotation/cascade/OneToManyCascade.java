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
import java.util.Collection;

import org.sormula.Database;


/**
 * Cascade annotation for a one to many relationship between source row and 
 * target row. Insert, update, and delete cascades default to standard primary key operations. Select
 * cascade must be defined since {@link SelectCascade#sourceParameterFieldNames()} are not known
 * by default. For more complex cascade relationships, use {@link Cascade}.
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
public @interface OneToManyCascade
{
    /**
     * Indicates if cascade should never modify the database. An equivalent to using readOnly=true would
     * be to set inserts={}, updates={}, and deletes={}.
     * 
     * @return true to use only the select operations (never modify); false to cascade all operations
     */
    boolean readOnly() default false;

    
    /**
     * Class type of target field to affect. Used as parameter to {@link Database#getTable(Class)} to
     * get table for cascade operation. {@link #targetClass()} is optional for scalar fields since
     * target class can be obtained from target field at runtime. For nonscalar target field types, 
     * like {@link Collection} types, {@link #targetClass()} must be specified.
     * 
     * @return class of field that is involved in cascade; Object.class to indicate that class is
     * to be obtained from scalar field at runtime by relfection
     */
    Class<?> targetClass() default Object.class;
    
    
    /**
     * Select cascade operations that will select target rows. Typical values would be:
     * <ul>
     * <li>@SelectCascade(operation=ArrayListSelectOperation.class, sourceParameterFieldNames="...")</li>
     * <li>@SelectCascade(operation=HashMapSelectOperation.class, sourceParameterFieldNames="...")</li>
     * </ul>
     * 
     * @return select annotations for cascade; use empty array for no select cascade
     */
    SelectCascade[] selects();
    
    
    /**
     * The cascade operations to perform when source row is inserted.
     * 
     * @return insert annotations for cascade; use empty array for no insert cascade
     */
    InsertCascade[] inserts() default @InsertCascade();
    
    
    /**
     * The cascade operations to perform when source row is updated.
     * 
     * @return update annotations for cascade; use empty array for no update cascade
     */
    UpdateCascade[] updates() default @UpdateCascade();
    
    
    /**
     * The cascade operations to perform when source row is deleted.
     * 
     * @return delete annotations for cascade; use empty array for no delete cascade
     */
    DeleteCascade[] deletes() default @DeleteCascade();
}
