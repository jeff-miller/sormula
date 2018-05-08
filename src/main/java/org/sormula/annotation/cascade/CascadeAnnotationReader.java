package org.sormula.annotation.cascade;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

import org.sormula.log.SormulaLogger;
import org.sormula.log.SormulaLoggerFactory;
import org.sormula.operation.HashMapSelectOperation;
import org.sormula.translator.TypeTranslatorMap;


/**
 * Base class for cascade annotation readers.
 * 
 * @author Jeff Miller
 * @since 1.8 and 2.2
 */
abstract public class CascadeAnnotationReader
{
    private static final SormulaLogger log = SormulaLoggerFactory.getClassLogger();
    
    @OneToOneCascade
    static Object defaultOneToOneCascadeHolder;
    static OneToOneCascade defaultOneToOneCascade;
    
    @OneToManyCascade
    static Object defaultListOneToManyCascadeHolder;
    static OneToManyCascade defaultListOneToManyCascade;
    
    @OneToManyCascade(selects=@SelectCascade(operation=HashMapSelectOperation.class,
            sourceParameterFieldNames="#primaryKeyFields", targetWhereName="#sourceFieldNames"))
    static Object defaultMapOneToManyCascadeHolder;
    static OneToManyCascade defaultMapOneToManyCascade;
    
    static
    {
        try
        {
            defaultOneToOneCascade = CascadeAnnotationReader.class.getDeclaredField(
                    "defaultOneToOneCascadeHolder").getAnnotation(OneToOneCascade.class);
            defaultListOneToManyCascade = CascadeAnnotationReader.class.getDeclaredField(
                    "defaultListOneToManyCascadeHolder").getAnnotation(OneToManyCascade.class);
            defaultMapOneToManyCascade = CascadeAnnotationReader.class.getDeclaredField(
                    "defaultMapOneToManyCascadeHolder").getAnnotation(OneToManyCascade.class);
        }
        catch (NoSuchFieldException e)
        {
            log.error("error initializing default cascade annotations", e);
        }
    }
    
    Field source;
    TypeTranslatorMap typeTranslatorMap;
    Class<?> targetClass;
    String[] foreignKeyValueFields;
    String foreignKeyReferenceField;
    String name;
    
    
    /**
     * Constructs for a field. 
     * 
     * @param source field with cascade annotation(s) (or default cascade)
     */
    public CascadeAnnotationReader(Field source)
    {
        this.source = source;
        if (source.isAnnotationPresent(OneToManyCascade.class))
        {
            initOneToManyCascade(source.getAnnotation(OneToManyCascade.class)); 
        }
        else if (source.isAnnotationPresent(OneToOneCascade.class))
        {
            initOneToOneCascade(source.getAnnotation(OneToOneCascade.class));
        }
        else if (source.isAnnotationPresent(Cascade.class))
        {
            initCascade(source.getAnnotation(Cascade.class));
        }
        else
        {
            // check if default cascade is applicable
            Class<?> ft = source.getType();
            
            if (ft.isArray() || Collection.class.isAssignableFrom(ft))
            {
                // array or Collection
                // one to many default 
                if (log.isDebugEnabled()) log.debug("default OneToManyCascade for " + source);
                initOneToManyCascade(defaultListOneToManyCascade);
            }
            else if (Map.class.isAssignableFrom(ft))
            {
                // Map
                // one to many default 
                if (log.isDebugEnabled()) log.debug("default OneToManyCascade for " + source);
                initOneToManyCascade(defaultMapOneToManyCascade);
            }
            else
            {
                // one to one default
                if (log.isDebugEnabled()) log.debug("default OneToOneCascade for " + source);
                initOneToOneCascade(defaultOneToOneCascade);
            }
        }
    }
    
    
    /**
     * Initializes when {@link OneToManyCascade} is annotated on source field (or implied
     * as default when no annotation is used).
     * @param cascacdeAnnotation annotation to use for cascade
     * @since 3.1
     */
    abstract protected void initOneToManyCascade(OneToManyCascade cascacdeAnnotation);
    
    
    /**
     * Initializes when {@link OneToOneCascade} is annotated on source field (or implied
     * as default when no annotation is used).
     * @param cascacdeAnnotation annotation to use for cascade
     * @since 3.1
     */
    abstract protected void initOneToOneCascade(OneToOneCascade cascacdeAnnotation);
    
    
    /**
     * Initializes when {@link Cascade} is annotated on source field (or implied
     * as default when no annotation is used).
     * @param cascacdeAnnotation annotation to use for cascade
     * @since 3.1
     */
    abstract protected void initCascade(Cascade cascacdeAnnotation);
    
    
    /**
     * Initializes common values when {@link OneToManyCascade} is annotated on source field.
     * 
     * @param cascadeAnnotation cascade information
     * @since 3.0
     */
    protected void init(OneToManyCascade cascadeAnnotation)
    {
        setForeignKeyValueFields(cascadeAnnotation.foreignKeyValueFields());
        setForeignKeyReferenceField(cascadeAnnotation.foreignKeyReferenceField());
        setName(cascadeAnnotation.name());
    }
    
    
    /**
     * Initializes common values when {@link OneToOneCascade} is annotated on source field.
     * 
     * @param cascadeAnnotation cascade information
     * @since 3.0
     */
    protected void init(OneToOneCascade cascadeAnnotation)
    {
        setForeignKeyValueFields(cascadeAnnotation.foreignKeyValueFields());
        setForeignKeyReferenceField(cascadeAnnotation.foreignKeyReferenceField());
        setName(cascadeAnnotation.name());
    }
    
    
    /**
     * Initializes common values when {@link Cascade} is annotated on source field.
     * 
     * @param cascadeAnnotation cascade information
     * @since 3.0
     */
    protected void init(Cascade cascadeAnnotation)
    {
        setForeignKeyValueFields(cascadeAnnotation.foreignKeyValueFields());
        setForeignKeyReferenceField(cascadeAnnotation.foreignKeyReferenceField());
        setName(cascadeAnnotation.name());
    }


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
     * @since 1.9 and 2.3
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
     * Sets the name of the cascade as defined by {@link OneToManyCascade#name()}, or
     * {@link OneToManyCascade#name()}, or {@link Cascade#name()}. Invoked by {@link #init(Cascade)},
     * {@link #init(OneToManyCascade)}, and {@link #init(OneToOneCascade)}.
     * 
     * @param name of cascade or empty string if none
     * @since 3.0
     */
    public void setName(String name)
    {
        this.name = name;
    }

    
    /**
     * Gets the name of the cascade as defined by {@link OneToManyCascade#name()}, or
     * {@link OneToManyCascade#name()}, or {@link Cascade#name()}.
     * 
     * @return name of cascade or empty string if none
     * @since 3.0
     */
    public String getName()
    {
        return name;
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


    /**
     * Sets the name of fields that contain the foreign key values in target (child) rows.
     * 
     * @param foreignKeyValueFields field names from cascade annotation foreignKeyValueFields
     * @since 3.0
     * @see Cascade#foreignKeyValueFields()
     * @see OneToManyCascade#foreignKeyValueFields()
     * @see OneToOneCascade#foreignKeyValueFields()
     */
    public void setForeignKeyValueFields(String[] foreignKeyValueFields)
    {
        this.foreignKeyValueFields = foreignKeyValueFields;
    }


    /** 
     * Gets the field names of foreign key values in target (child) rows. See cascade annotations
     * for details.
     * 
     * @return names of foreign key value fields
     * @since 3.0
     */
    public String[] getForeignKeyValueFields()
    {
        return foreignKeyValueFields;
    }


    /**
     * Sets the field name that contains a reference to the foreign key object in target (child) rows.
     * 
     * @param foreignKeyReferenceField field name of foreign key reference from cascade 
     * annotation foreignKeyReferenceField
     * 
     * @since 3.0
     * @see Cascade#foreignKeyReferenceField()
     * @see OneToManyCascade#foreignKeyReferenceField()
     * @see OneToOneCascade#foreignKeyReferenceField()
     */
    public void setForeignKeyReferenceField(String foreignKeyReferenceField)
    {
        this.foreignKeyReferenceField = foreignKeyReferenceField;
    }

    
    /**
     * Gets the field name of that contains foreign key reference in target (child) rows. 
     * See cascade annotations for details.
     * 
     * @return names of foreign key value fields
     * @since 3.0
     */
    public String getForeignKeyReferenceField()
    {
        return foreignKeyReferenceField;
    }
}
