package org.sormula.annotation.cascade;

import java.lang.reflect.Field;


/**
 * Reads save cascade annotation information for a field.
 * 
 * @author Jeff Miller
 * @since 1.9.3 and 2.3.3
 */
public class SaveCascadeAnnotationReader extends CascadeAnnotationReader
{
    SaveCascade[] saveCascades;

    
    /**
     * Constructs for a field.
     * 
     * @param source field with save cascade annotation
     */
    public SaveCascadeAnnotationReader(Field source)
    {
        super(source);
        
        if (saveCascades == null)
        {
            // none
            saveCascades = new SaveCascade[0];
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
            saveCascades = cascadeAnnotation.saves();
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
            saveCascades = cascadeAnnotation.saves();
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
        saveCascades = cascadeAnnotation.saves();
    }


    /**
     * @return array of save cascades; empty array if no save cascades defined for field
     */
    public SaveCascade[] getSaveCascades()
    {
        return saveCascades;
    }
}
