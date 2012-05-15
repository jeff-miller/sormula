package org.sormula.annotation.cascade;

import java.lang.reflect.Field;


/**
 * Base class for cascade annotation readers.
 * 
 * @author Jeff Miller
 * @since 1.8
 */
abstract public class CascadeAnnotationReader
{
    Field source;
    Class<?> targetClass;

    
    /**
     * Constructs for a field.
     * 
     * @param source field with cascade annotation(s)
     */
    public CascadeAnnotationReader(Field source)
    {
        this.source = source;
        
        if (source.isAnnotationPresent(OneToManyCascade.class))
        {
            initOneToManyCascade();
        }
        else if (source.isAnnotationPresent(OneToOneCascade.class))
        {
            initOneToOneCascade();
        }
        else if (source.isAnnotationPresent(Cascade.class))
        {
            initCascade();
        }
    }
    
    
    abstract protected void initOneToManyCascade();
    abstract protected void initOneToOneCascade();
    abstract protected void initCascade();


    protected void initTargetClass(Class<?> targetClass)
    {
        this.targetClass = targetClass;
    }

    
    protected void checkDefaultTargetClass()
    {
        if (targetClass != null && targetClass.getName().equals("java.lang.Object"))
        {
            // use default target class as field type
            // if field is parameterized, then getTable will NOT obtain correct table
            initTargetClass(source.getType());
        }
    }
    
    
    public Field getSource()
    {
        return source;
    }


    public Class<?> getTargetClass()
    {
        return targetClass;
    }
}
