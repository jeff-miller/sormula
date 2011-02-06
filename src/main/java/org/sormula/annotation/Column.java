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

import org.sormula.Table;
import org.sormula.translator.ColumnTranslator;
import org.sormula.translator.standard.StandardColumnTranslator;


/**
 * Defines a column attributes for a row class variable. If no {@link Column} annotaion is
 * used for a row class variable then that is equivalent to "@Column" (all defaults are used). 
 * 
 * @since 1.0
 * @author Jeff Miller
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column
{
    /**
     * @return name of column; if not specified then name is {@link Field#getName()}
     */
    String name() default "";
    
    
    /**
     * @return true if column is primary key; this must be true for columns needed 
     * with {@link Table#select(Object...)} or if update or delete operations are 
     * to be performed
     */
    boolean primaryKey() default false;
    
    
    /**
     * @return translator to use for reading and writing values to/from the database
     */
    Class<? extends ColumnTranslator> translator() default StandardColumnTranslator.class;
}
