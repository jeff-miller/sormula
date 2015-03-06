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
package org.sormula.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.PreparedStatement;
import java.sql.Statement;

import org.sormula.Table;
import org.sormula.operation.ModifyOperation;
import org.sormula.operation.SelectOperation;
import org.sormula.operation.SqlOperation;
import org.sormula.translator.NameTranslator;


/**
 * Defines class attributes for a row class. Annotates a row class or {@link Table} subclass. 
 * This annotation is only required if table name is different from class name or if a name
 * translator is needed.
 * 
 * @since 1.0
 * @author Jeff Miller
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Row
{
    /**
     * SQL table name for row. If not specified then tableName is 
     * {@link Class#getSimpleName()} or obtained by {@link NameTranslator#translate(String, Class)}
     * 
     * @return name of table for row
     */
    String tableName() default "";
    
    
    /**
     * Classes for providing sql names in place of class and field names. Each translator
     * is applied in the order provided.
     * 
     * @return translators for mapping java names to sql names
     */
    Class<? extends NameTranslator>[] nameTranslators() default {};
    
    
    /**
     * Initial capacity of collection/map that will contain the results from {@link Table#selectAll()},
     * {@link Table#selectAllCustom(String, Object...)}, or other {@link Table} select method that
     * does not use a where condition. Sets this value with {@link SelectOperation#setDefaultReadAllSize(int)}.
     * <p>
     * For queries that return large result sets, setting the initial capacity close to the number of
     * rows in result set may improve performance since collection/map that holds results will not 
     * need to be resized as much.
     * 
     * @return initial capacity of results colllection/map for a select
     * @since 3.0
     */
    int selectInitialCapacity() default 20;
    
    
    /**
     * Indicates that declared fields from the entire class hierarchy are to be used. Super class fields
     * are read prior to subclass fields.
     * 
     * @return true to use declared fields in super class(es); false to use declared fields only from
     * class with {@link Row} annotation
     * @since 3.0
     */
    boolean inhertedFields() default false;
    
    
    /**
     * JDBC fetch size to use for prepared statement {@link Table#selectAll()},
     * {@link Table#selectAllCustom(String, Object...)}, or other {@link Table} select method that
     * does not use a where condition.. Setting fetch size may improve memory
     * and/or performance for large result sets.
     * 
     * @return fetchSize number of rows that should be fetched from the database when more rows are needed; zero
     * to ignore
     * @since 3.0
     * @see PreparedStatement#setFetchSize(int)
     */
    int fetchSize() default 0;
    
    
    /**
     * Indicates that post execute methods are invoked when {@link Statement#executeUpdate(String)} returns a
     * row count of zero. A value of true means to invoke post execute methods unconditionally. A value of 
     * false means that post execute methods are not invoked when no rows are modified.
     * Post execute methods are postExecute and postExecuteCascade of {@link ModifyOperation}.
     * <p>
     * Typically this should be false. Set to true for pre-version 3.0 behavior.
     * 
     * @return true if post execute methods are performed unconditionally; false to
     * perform post execute methods only when database is modified
     * @since 3.0
     */
    boolean zeroRowCountPostExecute() default false;
    
    
    /**
     * Defines the primary keys for table. Use this instead of {@link Column#primaryKey()} 
     * or {@link Column#identity()}.
     * 
     * The advantage of this annotation is that the order of the keys listed is the order used 
     * by {@link SqlOperation#setParameters(Object...)}.
     * Some JVM's do not reflect the fields in order of declaration so this method provides a 
     * predictable order for primary keys.
     * 
     * @return names of fields that are the primary keys; empty array to obtain primary keys from
     * {@link Column} annotations
     * @since 3.0
     */
    String[] primaryKeyFields() default {};
}
