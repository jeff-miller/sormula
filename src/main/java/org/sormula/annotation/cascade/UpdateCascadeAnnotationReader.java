package org.sormula.annotation.cascade;

import java.lang.reflect.Field;


/**
 * Reads update cascade annotation information for a field.
 * 
 * @author Jeff Miller
 * @since 1.8 and 2.2
 */
public class UpdateCascadeAnnotationReader extends CascadeAnnotationReader
{
    UpdateCascade[] updateCascades;

    
    /**
     * Constructs for a field.
     * 
     * @param source field with delete cascade annotation (or default cascade)
     */
    public UpdateCascadeAnnotationReader(Field source)
    {
        super(source);
        
        if (updateCascades == null)
        {
            // none
            updateCascades = new UpdateCascade[0];
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
        
        if (!cascadeAnnotation.readOnly())
        {
            initTargetClass(cascadeAnnotation.targetClass());
            updateCascades = cascadeAnnotation.updates();
        }
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
        
        if (!cascadeAnnotation.readOnly())
        {
            initTargetClass(source.getType());
            updateCascades = cascadeAnnotation.updates();
        }
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
        updateCascades = cascadeAnnotation.updates();
    }


    /**
     * @return array of update cascades; empty array if no update cascades defined for field
     */
    public UpdateCascade[] getUpdateCascades()
    {
        return updateCascades;
    }
}
