package org.sormula.annotation.cascade;

import java.lang.reflect.Field;


/**
 * Reads select cascade annotation information for a field.
 * 
 * @author Jeff Miller
 * @since 1.8 and 2.2
 */
public class SelectCascadeAnnotationReader extends CascadeAnnotationReader
{
    SelectCascade[] selectCascades;

    
    /**
     * Constructs for a field.
     * 
     * @param source field with select cascade annotation (or default cascade)
     */
    public SelectCascadeAnnotationReader(Field source)
    {
        super(source); // uses deprecated constructor for backward compatability (don't use new constructor)
        
        if (selectCascades == null)
        {
            selectCascades = new SelectCascade[0];
        }
        
        checkDefaultTargetClass();
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Deprecated
    protected void initOneToManyCascade()
    {
        initOneToManyCascade(source.getAnnotation(OneToManyCascade.class));
    }
    /**
     * {@inheritDoc}
     * @since 3.1
     */
    protected void initOneToManyCascade(OneToManyCascade cascadeAnnotation)
    {
        init(cascadeAnnotation);
        initTargetClass(cascadeAnnotation.targetClass());
        selectCascades = cascadeAnnotation.selects();            
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Deprecated
    protected void initOneToOneCascade()
    {
        initOneToOneCascade(source.getAnnotation(OneToOneCascade.class));
    }
    /**
     * {@inheritDoc}
     * @since 3.1
     */
    protected void initOneToOneCascade(OneToOneCascade cascadeAnnotation)
    {
        init(cascadeAnnotation);
        initTargetClass(source.getType());
        selectCascades = cascadeAnnotation.selects();            
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Deprecated
    protected void initCascade()
    {
        initCascade(source.getAnnotation(Cascade.class));
    }
    /**
     * {@inheritDoc}
     * @since 3.1
     */
    protected void initCascade(Cascade cascadeAnnotation)
    {
        init(cascadeAnnotation);
        initTargetClass(cascadeAnnotation.targetClass());
        selectCascades = cascadeAnnotation.selects();
    }


    /**
     * @return array of select cascades; empty array if no select cascades defined for field
     */
    public SelectCascade[] getSelectCascades()
    {
        return selectCascades;
    }
}
