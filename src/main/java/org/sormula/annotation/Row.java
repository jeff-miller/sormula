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

import org.sormula.Table;
import org.sormula.translator.NameTranslator;
import org.sormula.translator.NoNameTranslator;


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
     * Class for providing sql names in place of class and field names.
     * Use {@link #nameTranslators()} instead of this method.
     * 
     * @return translator for mapping java names to sql names
     */
    @Deprecated
    Class<? extends NameTranslator> nameTranslator() default NoNameTranslator.class;
    
    
    /**
     * Classes for providing sql names in place of class and field names. Each translator
     * is applied in the order provided.
     * 
     * @return translators for mapping java names to sql names
     */
    Class<? extends NameTranslator>[] nameTranslators() default {};
}
