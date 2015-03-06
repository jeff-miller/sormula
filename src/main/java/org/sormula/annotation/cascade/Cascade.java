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
import org.sormula.annotation.Row;
import org.sormula.operation.SqlOperation;


/**
 * A general purpose cascade annotation. It may be used on any class variable that is either scalar 
 * or a collection. Use this as an alternative to {@link OneToManyCascade} or {@link OneToOneCascade}. 
 * <p>
 * More than one operation is allowed per field even though it is not likely that you would
 * need more than one. {@link #selects()}, {@link #updates()}, {@link #inserts()},
 * {@link #deletes()}, and {@link #saves()} accepts arrays which allow an empty array to mean "do nothing". 
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
     * For non-scalar target field types, like arrays, {@link Collection} types, and
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
     * to be obtained from field at runtime by relfection
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
     * Save cascade operations for target fields. Use empty array
     * to perform no update cascades.
     * 
     * @return update annotations for cascade
     * @since 1.9.3 and 2.3.3
     */
    SaveCascade[] saves() default {};
    
    
    /**
     * Delete cascade operations for target fields. Use empty array
     * to perform no delete cascades.
     * 
     * @return delete annotations for cascade
     */
    DeleteCascade[] deletes() default {};
    
    
    /** 
     * Defines foreign key(s) on target (child) rows. Used by cascades when the following is true:
     * {@link SelectCascade#setForeignKeyValues()}, {@link InsertCascade#setForeignKeyValues()},
     * {@link UpdateCascade#setForeignKeyValues()}, {@link DeleteCascade#setForeignKeyValues()},
     * {@link SaveCascade#setForeignKeyValues()}.
     * <p>
     * When target (parent) row is cascaded, then each target (child) row
     * foreign key setters are invoked with values from source (parent) primary key. 
     * <p>
     * Source row key(s) are primary keys in source row defined by {@link Column#primaryKey()}, 
     * {@link Column#identity()}, or {@link Row#primaryKeyFields()}.
     * <p>
     * When "#" is used, then cascade assumes that source key field names are the
     * same as target (child) key field names. For example: Parent.parentId --> Child.parentId.
     * <p>
     * If explicit fields are named, then they must be in same order as source row primary key fields.
     * 
     * @return names of foreign key fields in child (target) row;
     * "#" means target foreign key names are same as source (parent) primary key field names; 
     * empty array means no foreign keys are defined 
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
     * When "class" is used, then cascade assumes that target (child) key reference field
     * name is parent class simple name (begins with lower case). For example: SomeParent (source) of
     * SomeChild (target) will use SomeChild.someParent field and invoke SomeChild.setSomeParent(SomeParent). 
     * 
     * @return name of foreign key reference field in child (target) row;
     * "class" means target field name is same as source (parent) class name; 
     * empty string means no foreign key reference is defined 
     * 
     * @since 3.0
     */
    String foreignKeyReferenceField() default "";
    
    
    /**
     * Names the cascade so that it occurs only when desired. Set desired cascades with 
     * {@link Table#setRequiredCascades(String...)} or {@link SqlOperation#setRequiredCascades(String...)}.
     * If no name is specified (empty string) and no required cascades are specified, then cascade will 
     * occur by default since the default required cascade for {@link Table} is an empty string.
     *  
     * @return name of cascade; "*" to cascade always regardless of required cascade names 
     * @since 3.0
     */
    String name() default "";
}
