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
                for (OrderBy o: orderBysAnnotation.orderByConditions())
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
