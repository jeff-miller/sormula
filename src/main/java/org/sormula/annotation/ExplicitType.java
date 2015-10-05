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
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.sormula.Database;
import org.sormula.Table;
import org.sormula.translator.TypeTranslator;


/**
 * Associates a type with a {@link TypeTranslator}. Annotate row class,
 * subclass of {@link Table} or subclass of {@link Database}. A type only
 * needs to be annotated once for a database or only once per table.
 * <p>
 * When used on row or table class, then type is defined with
 * {@link Table#putTypeTranslator(Class, TypeTranslator)}. When used on
 * a database, then type is added with 
 * {@link Database#putTypeTranslator(Class, TypeTranslator)}.
 * 
 * @since 1.6 and 2.0
 * @author Jeff Miller
 */
@Repeatable(ExplicitTypes.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface ExplicitType
{
    /**
     * @return class type that will be converted with translator
     */
    Class<?> type();
    
    
    /**
     * @return type translator to use on class
     */
    Class<? extends TypeTranslator> translator();
}
