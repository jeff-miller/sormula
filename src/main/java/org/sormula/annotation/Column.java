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
import java.lang.reflect.Field;
import java.sql.Statement;

import org.sormula.Table;
import org.sormula.translator.ColumnTranslator;
import org.sormula.translator.NameTranslator;
import org.sormula.translator.standard.StandardColumnTranslator;


/**
 * Defines column attributes for a row class variable. If no {@link Column} annotaion is
 * used for a row class variable, that is equivalent to "@Column" (all defaults are used). 
 * Annotates a class variable.
 * 
 * @since 1.0
 * @author Jeff Miller
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column
{
    /**
     * SQL name of column. If not specified then name is {@link Field#getName()} or
     * is obtained from {@link NameTranslator#translate(String, Class)} if translator
     * is configured for table.
     * 
     * @return name of column 
     */
    String name() default "";
    
    
    /**
     * Indicates that column is the primary key for row. A composite primary key may
     * be defined if key is composed of more than one column by annotation each key field 
     * and setting this true.
     * <p>
     * This must be set to true for all columns needed with {@link Table#select(Object...)} 
     * or if update or delete operations are to be performed.
     *  
     * @return true if column is primary key
     */
    boolean primaryKey() default false;
    
    
    /**
     * Indicates that column is declared as "GENERATED ... AS IDENTITY ...". Column gets generated by the 
     * databases when rows are inserted. Only one identity column is allowed per row class. When
     * identity==true, implies that primaryKey==true so that primaryKey does not need to be specified.
     * 
     * @return true to set field with value from {@link Statement#getGeneratedKeys()}
     */
    boolean identity() default false;
    
    
    /**
     * Defines class that will read/write row members from/to database.
     * 
     * @return translator to use for reading and writing values from/to the database
     */
    Class<? extends ColumnTranslator> translator() default StandardColumnTranslator.class;
}
