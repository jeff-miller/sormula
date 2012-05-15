package org.sormula.annotation.cascade;

import java.lang.reflect.Field;


/**
 * Reads select cascade annotation information for a field.
 * 
 * @author Jeff Miller
 * @since 1.8
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
    
    
    protected void initOneToManyCascade()
    {
        OneToManyCascade cascadesAnnotation = source.getAnnotation(OneToManyCascade.class);
        initTargetClass(cascadesAnnotation.targetClass());
        selectCascades = cascadesAnnotation.selects();            
    }
    
    
    protected void initOneToOneCascade()
    {
        OneToOneCascade cascadesAnnotation = source.getAnnotation(OneToOneCascade.class);
        initTargetClass(source.getType());
        selectCascades = cascadesAnnotation.selects();            
    }
    
    
    protected void initCascade()
    {
        Cascade cascadesAnnotation = source.getAnnotation(Cascade.class);
        initTargetClass(cascadesAnnotation.targetClass());
        selectCascades = cascadesAnnotation.selects();
    }


    /**
     * @return array of select cascades; empty array if no select cascades defined for field
     */
    public SelectCascade[] getSelectCascades()
    {
        return selectCascades;
    }
}
