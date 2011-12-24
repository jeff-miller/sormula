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


/**
 * Reads {@link Where} annotations from a class.
 * 
 * @author Jeff Miller
 * @since 1.3
 */
public class WhereAnnotationReader
{
    Class<?> source;
    

    /**
     * Constructs for a class.
     * 
     * @param source class that contains {@link Where} or {@link Wheres} annotations
     */
    public WhereAnnotationReader(Class<?> source)
    {
        this.source = source;
    }
    

    /**
     * Gets annotation for a specific name.
     * 
     * @param name look for annotation where name equals {@link Where#name()}
     * @return Where annotation or null if none found
     */
    public Where getAnnotation(String name)
    {
        // look for single where annotation
        Where whereAnnotation = source.getAnnotation(Where.class);
        
        if (whereAnnotation == null || !whereAnnotation.name().equals(name))
        {
            // no single annotation or name does not match
            Wheres wheresAnnotation = source.getAnnotation(Wheres.class);
            
            if (wheresAnnotation != null)
            {
                // look for name
                for (Where w: wheresAnnotation.whereConditions())
                {
                    if (w.name().equals(name))
                    {
                        // found
                        whereAnnotation = w;
                        break;
                    }
                }
            }
        }
        
        return whereAnnotation;
    }
}
