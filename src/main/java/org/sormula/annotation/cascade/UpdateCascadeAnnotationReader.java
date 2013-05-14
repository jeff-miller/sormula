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
     * @param source field with delete cascade annotation
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
    protected void initOneToManyCascade()
    {
        OneToManyCascade cascadeAnnotation = source.getAnnotation(OneToManyCascade.class);
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
    protected void initOneToOneCascade()
    {
        OneToOneCascade cascadeAnnotation = source.getAnnotation(OneToOneCascade.class);
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
    protected void initCascade()
    {
        Cascade cascadeAnnotation = source.getAnnotation(Cascade.class);
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
