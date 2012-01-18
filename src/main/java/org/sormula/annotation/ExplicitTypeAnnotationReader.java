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

import java.lang.reflect.AnnotatedElement;


/**
 * Reads {@link ExplicitType} annotations TODO from a class.
 * first Type annotation for a class is used; others are ignored
 * 
 * @author Jeff Miller
 * @since 1.6
 */
public class ExplicitTypeAnnotationReader
{
    //private static final ClassLogger log = new ClassLogger();
    AnnotatedElement[] sources;
    

    /**
     * Constructs for classes that may contain the annotation.
     * 
     * @param sources classes that contain {@link ExplicitType} or {@link TypeTranslators} annotations
     */
    public ExplicitTypeAnnotationReader(AnnotatedElement... sources)  
    {
        this.sources = sources;
    }
    

    // TODO name install?
    public ExplicitType getAnnotation()
    {
        ExplicitType typeAnnotation = null;
        
        for (AnnotatedElement ae : sources)
        {
            typeAnnotation = ae.getAnnotation(ExplicitType.class);
            if (typeAnnotation != null) break;
        }
        
        return typeAnnotation;
    }
    
    
    // TODO name install?
    public ExplicitType getAnnotation2()
    {
        ExplicitType typeAnnotation = null;
        
        for (AnnotatedElement ae : sources)
        {
            typeAnnotation = ae.getAnnotation(ExplicitType.class);
            if (typeAnnotation != null) break;

            // look in Types in all sources
            TypeTranslators typesAnnotation = ae.getAnnotation(TypeTranslators.class);
            
            if (typesAnnotation != null)
            {
                for (ExplicitType t: typesAnnotation.value())
                {
                    
                }
            }
        }
        
        return typeAnnotation;
    }
}
