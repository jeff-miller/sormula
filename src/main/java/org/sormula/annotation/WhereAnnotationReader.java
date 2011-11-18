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
