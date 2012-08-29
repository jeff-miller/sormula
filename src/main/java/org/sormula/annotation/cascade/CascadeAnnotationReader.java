package org.sormula.annotation.cascade;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.sormula.log.ClassLogger;


/**
 * Base class for cascade annotation readers.
 * 
 * @author Jeff Miller
 * @since 1.8
 */
abstract public class CascadeAnnotationReader
{
    private static final ClassLogger log = new ClassLogger();
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
     * Sets the class type for target field that will be affected by cascade. For scalar fields and
     * simple parameterized types, the target type can be determined. Otherwise the type must be specified by
     * {@link OneToManyCascade#targetClass()} or {@link Cascade#targetClass()}.
     * 
     * @param targetClass class of target field
     */
    protected void initTargetClass(Class<?> targetClass)
    {
        this.targetClass = targetClass;
    }

    
    /**
     * Initializes target class based upon {@link Field#getGenericType()} and {@link Field#getType()} when target class 
     * is not specified (the default value of Object.class). {@link OneToManyCascade#targetClass()} 
     * and {@link Cascade#targetClass()} have a default of Object.class.
     * @since 1.9
     */
    protected void checkDefaultTargetClass()
    {
        // note: targetClass is null if no cascades on source field
        if (targetClass != null && targetClass.getName().equals("java.lang.Object"))
        {
            Type genericType = source.getGenericType();
            
            if (genericType instanceof ParameterizedType)
            {
                // parameterized type
                Type[] typeArguments = ((ParameterizedType)genericType).getActualTypeArguments();
                
                // assume last is the target type
                Type lastArgument = typeArguments[typeArguments.length - 1];
                
                if (lastArgument instanceof Class)
                {
                    // parameterized type is a class 
                    Class lastArgumentClass = (Class)lastArgument;
                    if (lastArgumentClass.isArray()) initTargetClass(lastArgumentClass.getComponentType());
                    else initTargetClass(lastArgumentClass);
                }
                else
                {
                    // too vague to know target class
                    log.error("cannot determine target class for '" + lastArgument +
                            "' for " + source.getDeclaringClass().getName() + "." + source.getName() + 
                            "; specify targetClass in cascade annotation");
                }
            }
            else
            {
                // use default target class as field type
                Class sourceClass = source.getType();
                if (sourceClass.isArray()) initTargetClass(sourceClass.getComponentType());
                else initTargetClass(sourceClass);
            }
            
            if (log.isDebugEnabled()) log.debug("cascade targetClass defaults to " + targetClass);
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
     * @return {@link OneToManyCascade#targetClass()} or {@link Cascade#targetClass()} 
     */
    public Class<?> getTargetClass()
    {
        return targetClass;
    }
}
