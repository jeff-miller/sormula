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
    
    
    /**
     * Initializes when {@link OneToManyCascade} is annotated on source field.
     */
    abstract protected void initOneToManyCascade();
    
    
    /**
     * Initializes when {@link OneToOneCascade} is annotated on source field.
     */
    abstract protected void initOneToOneCascade();
    
    
    /**
     * Initializes when {@link Cascade} is annotated on source field.
     */
    abstract protected void initCascade();


    /**
     * Sets the class type for target field that will be affected by cascade. For scalar fields, the
     * target type is the field type. For non-scalar, the type must be specified by
     * {@link OneToManyCascade#targetClass()} or {@link Cascade#targetClass()}.
     * 
     * @param targetClass class of target field
     */
    protected void initTargetClass(Class<?> targetClass)
    {
        this.targetClass = targetClass;
    }

    
    /**
     * Initializes target class as {@link Field#getType()} if target class is the default 
     * value of Object.class. {@link OneToManyCascade#targetClass()} and {@link Cascade#targetClass()}
     * have a default of Object.class.
     */
    protected void checkDefaultTargetClass()
    {
        if (targetClass != null && targetClass.getName().equals("java.lang.Object"))
        {
            // use default target class as field type
            // if field is parameterized, then getTable will NOT obtain correct table
            initTargetClass(source.getType());
        }
    }
    
    
    /**
     * Gets the source field supplied in constructor.
     * 
     * @return source field with cascade annotation(s)
     */
    public Field getSource()
    {
        return source;
    }


    /**
     * Gets the class of the field that will be affected by cascade.
     *  
     * @return {@link OneToManyCascade#targetClass()} or {@link Cascade#targetClass()}; 
     * the source type if field is scalar and target class was defined as Object.class
     */
    public Class<?> getTargetClass()
    {
        return targetClass;
    }
}
