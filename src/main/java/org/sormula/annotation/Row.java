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

import org.sormula.Table;
import org.sormula.operation.SelectOperation;
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
}
