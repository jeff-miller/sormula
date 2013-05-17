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
     * @param source field with select cascade annotation
     */
    public SelectCascadeAnnotationReader(Field source)
    {
        super(source);
        
        if (selectCascades == null)
        {
            selectCascades = new SelectCascade[0];
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
        initTargetClass(cascadeAnnotation.targetClass());
        selectCascades = cascadeAnnotation.selects();            
    }
    
    
    /**
     * {@inheritDoc}
     */
    protected void initOneToOneCascade()
    {
        OneToOneCascade cascadeAnnotation = source.getAnnotation(OneToOneCascade.class);
        init(cascadeAnnotation);
        initTargetClass(source.getType());
        selectCascades = cascadeAnnotation.selects();            
    }
    
    
    /**
     * {@inheritDoc}
     */
    protected void initCascade()
    {
        Cascade cascadeAnnotation = source.getAnnotation(Cascade.class);
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
