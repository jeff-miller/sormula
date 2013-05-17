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
     * @param source field with delete cascade annotation
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
    protected void initOneToManyCascade()
    {
        OneToManyCascade cascadeAnnotation = source.getAnnotation(OneToManyCascade.class);
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
    protected void initOneToOneCascade()
    {
        OneToOneCascade cascadeAnnotation = source.getAnnotation(OneToOneCascade.class);
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
    protected void initCascade()
    {
        Cascade cascadeAnnotation = source.getAnnotation(Cascade.class);
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
