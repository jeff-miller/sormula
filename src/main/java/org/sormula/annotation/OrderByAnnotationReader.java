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
 * Reads {@link OrderBy} annotations from a class.
 * 
 * @author Jeff Miller
 * @since 1.3
 */
public class OrderByAnnotationReader
{
    private static final ClassLogger log = new ClassLogger();
    Class<?>[] sources;
    

    /**
     * Constructs for classes that may contain the annotation.
     * 
     * @param sources classes that contain {@link OrderBy} or {@link OrderBys} annotations
     * @since 1.6
     */
    public OrderByAnnotationReader(Class<?>... sources)  
    {
        this.sources = sources;
    }
    

    /**
     * Constructs for a class.
     * 
     * @param source class that contains {@link OrderBy} or {@link OrderBys} annotations
     */
    @Deprecated
    public OrderByAnnotationReader(Class<?> source)
    {
        sources = new Class<?>[1];
        sources[0] = source;
    }
    

    /**
     * Gets annotation for a specific name.
     * 
     * @param name look for annotation where name equals {@link OrderBy#name()}
     * @return OrderBy annotation or null if none found
     */
    public OrderBy getAnnotation(String name)
    {
        OrderBy orderByAnnotation = null;
        
        for (Class<?> s : sources)
        {
            // look for single OrderBy annotation
            orderByAnnotation = s.getAnnotation(OrderBy.class);
            
            if (orderByAnnotation != null && orderByAnnotation.name().equals(name))
            {
                // found in single OrderBy annoation
                if (log.isDebugEnabled()) log.debug(name + " order annotation from " + s.getCanonicalName());
                return orderByAnnotation;
            }
            else
            {
                // no single annotation or name does not match
                OrderBys orderBysAnnotation = s.getAnnotation(OrderBys.class);
                
                if (orderBysAnnotation != null)
                {
                    // look for name
                    OrderBy[] orderbys = orderBysAnnotation.value();
                    if (orderbys.length == 0) orderbys = orderBysAnnotation.orderByConditions();
                    for (OrderBy o: orderbys)
                    {
                        if (o.name().equals(name))
                        {
                            // found
                            if (log.isDebugEnabled()) log.debug(name + " order annotation from " + s.getCanonicalName());
                            return o;
                        }
                    }
                }
            }
        }
        
        // not found
        return null;
    }
}
