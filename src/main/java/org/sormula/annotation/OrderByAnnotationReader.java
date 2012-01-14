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
 * Reads {@link OrderBy} annotations from a class.
 * 
 * @author Jeff Miller
 * @since 1.3
 */
public class OrderByAnnotationReader
{
    Class<?> source;
    

    /**
     * Constructs for a class.
     * 
     * @param source class that contains {@link OrderBy} or {@link OrderBys} annotations
     */
    public OrderByAnnotationReader(Class<?> source)
    {
        this.source = source;
    }
    

    /**
     * Gets annotation for a specific name.
     * 
     * @param name look for annotation where name equals {@link OrderBy#name()}
     * @return OrderBy annotation or null if none found
     */
    public OrderBy getAnnotation(String name)
    {
        // look for single OrderBy annotation
        OrderBy orderByAnnotation = source.getAnnotation(OrderBy.class);
        
        if (orderByAnnotation == null || !orderByAnnotation.name().equals(name))
        {
            // no single annotation or name does not match
            OrderBys orderBysAnnotation = source.getAnnotation(OrderBys.class);
            
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
                        orderByAnnotation = o;
                        break;
                    }
                }
            }
        }
        
        return orderByAnnotation;
    }
}
