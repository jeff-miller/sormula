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
     * @param sources classes that contain {@link Where} annotations
     * @since 1.6 and 2.0
     */
    public WhereAnnotationReader(Class<?>... sources)  
    {
        this.sources = sources;
    }
    

    /**
     * Gets annotation for a specific name.
     * 
     * @param name look for annotation where name equals {@link Where#name()}
     * @return Where annotation or null if none found
     */
    public Where getAnnotation(String name)
    {
        for (Class<?> s : sources)
        {
            // look for name
            for (Where w: s.getAnnotationsByType(Where.class))
            {
                if (w.name().equals(name))
                {
                    // found
                    if (log.isDebugEnabled()) log.debug(name + " where annotation from " + s.getCanonicalName());
                    return w;
                }
            }
        }
        
        // not found
        return null;
    }
}
