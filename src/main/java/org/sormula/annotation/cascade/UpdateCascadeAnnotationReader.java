package org.sormula.annotation.cascade;

import java.lang.reflect.Field;


/**
 * Reads update cascade annotation information for a field.
 * 
 * @author Jeff Miller
 * @since 1.8
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
    
    
    protected void initOneToManyCascade()
    {
        OneToManyCascade cascadesAnnotation = source.getAnnotation(OneToManyCascade.class);
        
        if (!cascadesAnnotation.readOnly())
        {
            initTargetClass(cascadesAnnotation.targetClass());
            updateCascades = cascadesAnnotation.updates();
        }
    }
    
    
    protected void initOneToOneCascade()
    {
        OneToOneCascade cascadesAnnotation = source.getAnnotation(OneToOneCascade.class);
        
        if (!cascadesAnnotation.readOnly())
        {
            initTargetClass(source.getType());
            updateCascades = cascadesAnnotation.updates();
        }
    }
    
    
    protected void initCascade()
    {
        Cascade cascadesAnnotation = source.getAnnotation(Cascade.class);
        initTargetClass(cascadesAnnotation.targetClass());
        updateCascades = cascadesAnnotation.updates();
    }


    /**
     * @return array of update cascades; empty array if no update cascades defined for field
     */
    public UpdateCascade[] getUpdateCascades()
    {
        return updateCascades;
    }
}
