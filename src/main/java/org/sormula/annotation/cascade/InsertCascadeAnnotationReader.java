package org.sormula.annotation.cascade;

import java.lang.reflect.Field;


/**
 * Reads insert cascade annotation information for a field.
 * 
 * @author Jeff Miller
 * @since 1.8 and 2.2
 */
public class InsertCascadeAnnotationReader extends CascadeAnnotationReader
{
    InsertCascade[] insertCascades;

    
    /**
     * Constructs for a field.
     * 
     * @param source field with insert cascade annotation
     */
    public InsertCascadeAnnotationReader(Field source)
    {
        super(source);
        
        if (insertCascades == null)
        {
            // none
            insertCascades = new InsertCascade[0];
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
            insertCascades = cascadeAnnotation.inserts();
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
            insertCascades = cascadeAnnotation.inserts();
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
        insertCascades = cascadeAnnotation.inserts();
    }


    /**
     * @return array of insert cascades; empty array if no insert cascades defined for field
     */
    public InsertCascade[] getInsertCascades()
    {
        return insertCascades;
    }
}
