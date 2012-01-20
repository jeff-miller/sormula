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

import org.sormula.log.ClassLogger;


/**
 * Reads {@link Where} annotations from a class.
 * 
 * @author Jeff Miller
 * @since 1.3
 */
public class WhereAnnotationReader
{
    private static final ClassLogger log = new ClassLogger();
    Class<?>[] sources;
    

    /**
     * Constructs for classes that may contain the annotation.
     * 
     * @param sources classes that contain {@link Where} or {@link Wheres} annotations
     * @since 1.6
     */
    public WhereAnnotationReader(Class<?>... sources)  
    {
        this.sources = sources;
    }
    
    
    /**
     * Constructs for a class.
     * 
     * @param source class that contains {@link Where} or {@link Wheres} annotations
     */
    @Deprecated
    public WhereAnnotationReader(Class<?> source) 
    {
        sources = new Class<?>[1];
        sources[0] = source;
    }
    

    /**
     * Gets annotation for a specific name.
     * 
     * @param name look for annotation where name equals {@link Where#name()}
     * @return Where annotation or null if none found
     */
    public Where getAnnotation(String name)
    {
        Where whereAnnotation = null;
        
        for (Class<?> s : sources)
        {
            // look for single where annotation
            whereAnnotation = s.getAnnotation(Where.class);
            
            if (whereAnnotation != null && whereAnnotation.name().equals(name))
            {
                // found in single Where annoation
                if (log.isDebugEnabled()) log.debug(name + " where annotation from " + s.getCanonicalName());
                return whereAnnotation;
            }
            else
            {
                // no single annotation or name does not match
                Wheres wheresAnnotation = s.getAnnotation(Wheres.class);
                
                if (wheresAnnotation != null)
                {
                    // look for name
                    Where[] wheres = wheresAnnotation.value();
                    if (wheres.length == 0) wheres = wheresAnnotation.whereConditions(); // remove when deprecated removed
                    for (Where w: wheres)
                    {
                        if (w.name().equals(name))
                        {
                            // found
                            if (log.isDebugEnabled()) log.debug(name + " where annotation from " + s.getCanonicalName());
                            return w;
                        }
                    }
                }
            }
        }
        
        // not found
        return null;
    }
}
