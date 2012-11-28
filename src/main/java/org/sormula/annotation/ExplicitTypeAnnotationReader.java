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

import java.lang.reflect.AnnotatedElement;

import org.sormula.Database;
import org.sormula.Table;
import org.sormula.log.ClassLogger;
import org.sormula.translator.TypeTranslator;
import org.sormula.translator.TypeTranslatorMap;


/**
 * Reads {@link ExplicitType} annotations from classes. The first ExplicitType
 * annotation found for a class is used. If ExplicitType is already defined for
 * a class type, then it is ignored.
 * 
 * @author Jeff Miller
 * @since 1.6 and 2.0
 */
public class ExplicitTypeAnnotationReader
{
    private static final ClassLogger log = new ClassLogger();
    TypeTranslatorMap typeTranslatorMap;
    Class<?>[] sources;
    

    /**
     * Constructs for classes that may contain the annotation.
     * 
     * @param typeTranslatorMap a class that implements {@link TypeTranslatorMap}; typically
     * {@link Database} and {@link Table}
     * @param sources classes that contain {@link ExplicitType} or {@link ExplicitTypes} annotations
     */
    public ExplicitTypeAnnotationReader(TypeTranslatorMap typeTranslatorMap, Class<?>... sources)  
    {
        this.typeTranslatorMap = typeTranslatorMap;
        this.sources = sources;
    }
    

    /**
     * Reads {@link ExplicitType} annotations from sources and adds them to
     * type map if they are not already defined.
     * 
     * @throws Exception if error creating {@link TypeTranslator}
     */
    public void install() throws Exception
    {
        for (AnnotatedElement ae : sources)
        {
            ExplicitType typeAnnotation = ae.getAnnotation(ExplicitType.class);
            if (typeAnnotation != null) 
            {
                if (log.isDebugEnabled()) log.debug("read ExplicitType for " + ae);
                updateMap(typeAnnotation);
            }

            // look in Types in all sources
            ExplicitTypes typesAnnotation = ae.getAnnotation(ExplicitTypes.class);
            
            if (typesAnnotation != null)
            {
                if (log.isDebugEnabled()) log.debug("read ExplicitTypes for " + ae);
                for (ExplicitType t: typesAnnotation.value())
                {
                    updateMap(t);    
                }
            }
        }
    }
    
    
    /**
     * Adds a new instance of {@link TypeTranslator} to type translator map if not already 
     * defined for {@link ExplicitType#type()}.
     * <p>
     * Versions prior to 1.9.2 did not allow type translators to be replaced. Starting with
     * version 1.9.2 and 2.3.2, this method will replace existing type translator with new
     * one {@link ExplicitType#translator()} is different from existing.
     * 
     * @param typeAnnotation definition of type to add
     * @throws Exception if error creating new instance of {@link TypeTranslator}
     */
    protected void updateMap(ExplicitType typeAnnotation) throws Exception
    {
        if (log.isDebugEnabled()) log.debug("check " + typeAnnotation);
        Class<?> typeClass = typeAnnotation.type();
        Class<? extends TypeTranslator> newTranslatorClass = typeAnnotation.translator();
        TypeTranslator<?> oldTranslator = typeTranslatorMap.getTypeTranslator(typeClass);
        
        if (oldTranslator == null)
        {
            // no type translator for type, add it
            if (log.isDebugEnabled()) log.debug("add translator=" + newTranslatorClass + " for type=" + typeClass);
            typeTranslatorMap.putTypeTranslator(typeClass, newTranslatorClass.newInstance());
        }
        else if (oldTranslator.getClass() != newTranslatorClass)
        {
            // new is different from old, replace it
            if (log.isDebugEnabled()) log.debug("replace " + oldTranslator.getClass() + " with translator=" + newTranslatorClass + " for type=" + typeClass);
            typeTranslatorMap.putTypeTranslator(typeClass, newTranslatorClass.newInstance());
        }
    }
}
