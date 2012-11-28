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
        OneToManyCascade cascadesAnnotation = source.getAnnotation(OneToManyCascade.class);
        
        if (!cascadesAnnotation.readOnly())
        {
            initTargetClass(cascadesAnnotation.targetClass());
            insertCascades = cascadesAnnotation.inserts();
        }
    }
    
    
    /**
     * {@inheritDoc}
     */
    protected void initOneToOneCascade()
    {
        OneToOneCascade cascadesAnnotation = source.getAnnotation(OneToOneCascade.class);
        
        if (!cascadesAnnotation.readOnly())
        {
            initTargetClass(source.getType());
            insertCascades = cascadesAnnotation.inserts();
        }
    }
    
    
    /**
     * {@inheritDoc}
     */
    protected void initCascade()
    {
        Cascade cascadesAnnotation = source.getAnnotation(Cascade.class);
        initTargetClass(cascadesAnnotation.targetClass());
        insertCascades = cascadesAnnotation.inserts();
    }


    /**
     * @return array of insert cascades; empty array if no insert cascades defined for field
     */
    public InsertCascade[] getInsertCascades()
    {
        return insertCascades;
    }
}
