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
import org.sormula.Table;
import org.sormula.annotation.Column;
import org.sormula.operation.SqlOperation;


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
     * The cascade operations to perform when source row is saved.
     * 
     * @return save annotations for cascade; use empty array for no save cascade
     * @since 1.9.3 and 2.3.3
     */
    SaveCascade[] saves() default @SaveCascade();
    
    
    /**
     * The cascade operations to perform when source row is deleted.
     * 
     * @return delete annotations for cascade; use empty array for no delete cascade
     */
    DeleteCascade[] deletes() default @DeleteCascade();
    
    
    /** 
     * Defines foreign key(s) on target (child) rows. Used by cascades when the following is true:
     * {@link SelectCascade#setForeignKeyValues()}, {@link InsertCascade#setForeignKeyValues()},
     * {@link UpdateCascade#setForeignKeyValues()}, {@link DeleteCascade#setForeignKeyValues()},
     * {@link SaveCascade#setForeignKeyValues()}.
     * <p>
     * When target (parent) row is cascaded, then each target (child) row
     * foreign key setters are invoked with values from source (parent) primary key. 
     * <p>
     * Source row key(s) are primary keys in source row where {@link Column#primaryKey()} is true.
     * <p>
     * When asterisk (*) is used, then cascade assumes that source key field names are the
     * same as target (child) key field names. For example: Parent.parentId --> Child.parentId.
     * <p>
     * If explicit fields are named, then they must be in same order as source row primary key fields.
     * 
     * @return names of foreign key fields in child (target) row; asterisk "*" means use
     * same foreign key names as source (parent) field names; empty array means no foreign keys are defined 
     * 
     * @since 3.0
     */
    String[] foreignKeyValueFields() default {}; 
    
    
    /** 
     * Defines foreign key reference on target (child) rows. Used by cascades when the following is true:
     * {@link SelectCascade#setForeignKeyReference()}, {@link InsertCascade#setForeignKeyReference()},
     * {@link UpdateCascade#setForeignKeyReference()}, {@link DeleteCascade#setForeignKeyReference()},
     * {@link SaveCascade#setForeignKeyReference()}.
     * <p>
     * When target (parent) row is cascaded, then each target (child) row
     * foreign key reference setter is invoked with reference to source (parent). 
     * <p>
     * When asterisk (*) is used, then cascade assumes that target (child) key reference field
     * name is parent class simple name (begins with lower case). For example: SomeParent (source) of
     * SomeChild (target) will use SomeChild.someParent field and invoke SomeChild.setSomeParent(SomeParent). 
     * 
     * @return name of foreign key reference field in child (target) row; asterisk "*" means use
     * source (parent) class name; empty string means no foreign key reference is defined 
     * 
     * @since 3.0
     */
    String foreignKeyReferenceField() default "";
    
    
    /**
     * Names the cacacade so that it occurs only when desired. Set desired cascades with 
     * {@link Table#setRequiredCascades(String...)} or {@link SqlOperation#setRequiredCascades(String...)}.
     * If no name is specified (empty string) and no required cascades are specified, then cascade will 
     * occur by default since since the default required cascade for {@link Table} is an empty string.
     *  
     * @return name of cascade
     * @since 3.0
     */
    String name() default "";
}
