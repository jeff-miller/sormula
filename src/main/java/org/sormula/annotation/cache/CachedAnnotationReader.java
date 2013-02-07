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
package org.sormula.annotation.cache;

import org.sormula.log.ClassLogger;


/**
 * Reads {@link Cached} annotation from a class.
 * 
 * @author Jeff Miller
 * @since 3.0
 */
public class CachedAnnotationReader
{
    private static final ClassLogger log = new ClassLogger();
    Class<?>[] sources;
    

    /**
     * Constructs for classes that may contain the annotation.
     * 
     * @param sources classes that contain {@link Cached} annotation
     */
    public CachedAnnotationReader(Class<?>... sources)  
    {
        this.sources = sources;
    }
    

    /**
     * Gets annotation.
     * 
     * @return Cached annotation or null if none found
     */
    public Cached getAnnotation()
    {
        Cached cachedAnnotation = null;
        
        for (Class<?> s : sources)
        {
            cachedAnnotation = s.getAnnotation(Cached.class);
            
            if (cachedAnnotation != null)
            {
                // found it
                if (log.isDebugEnabled()) log.debug("using Cached annotation from " + s.getCanonicalName());
                return cachedAnnotation;
            }
        }
        
        // not found
        return null;
    }
}
