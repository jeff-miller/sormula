package org.sormula.annotation.cascade;

import java.lang.reflect.Field;


/**
 * Reads delete cascade annotation information for a field.
 * 
 * @author Jeff Miller
 * @since 1.8 and 2.2
 */
public class DeleteCascadeAnnotationReader extends CascadeAnnotationReader
{
    DeleteCascade[] deleteCascades;

    
    /**
     * Constructs for a field.
     * 
     * @param source field with delete cascade annotation (or default cascade)
     */
    public DeleteCascadeAnnotationReader(Field source)
    {
        super(source);
        
        if (deleteCascades == null)
        {
            // none
            deleteCascades = new DeleteCascade[0];
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
            deleteCascades = cascadeAnnotation.deletes();
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
            deleteCascades = cascadeAnnotation.deletes();
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
        deleteCascades = cascadeAnnotation.deletes();
    }
    

    /**
     * @return array of delete cascades; empty array if no delete cascades defined for field
     */
    public DeleteCascade[] getDeleteCascades()
    {
        return deleteCascades;
    }
}
