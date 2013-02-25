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
        OneToManyCascade cascadesAnnotation = source.getAnnotation(OneToManyCascade.class);
        setForeignKeyValueFields(cascadesAnnotation.foreignKeyValueFields());
        setForeignKeyReferenceField(cascadesAnnotation.foreignKeyReferenceField());
        
        if (!cascadesAnnotation.readOnly())
        {
            initTargetClass(cascadesAnnotation.targetClass());
            saveCascades = cascadesAnnotation.saves();
        }
    }
    
    
    /**
     * {@inheritDoc}
     */
    protected void initOneToOneCascade()
    {
        OneToOneCascade cascadesAnnotation = source.getAnnotation(OneToOneCascade.class);
        setForeignKeyValueFields(cascadesAnnotation.foreignKeyValueFields());
        setForeignKeyReferenceField(cascadesAnnotation.foreignKeyReferenceField());
        
        if (!cascadesAnnotation.readOnly())
        {
            initTargetClass(source.getType());
            saveCascades = cascadesAnnotation.saves();
        }
    }
    
    
    /**
     * {@inheritDoc}
     */
    protected void initCascade()
    {
        Cascade cascadesAnnotation = source.getAnnotation(Cascade.class);
        setForeignKeyValueFields(cascadesAnnotation.foreignKeyValueFields());
        setForeignKeyReferenceField(cascadesAnnotation.foreignKeyReferenceField());
        initTargetClass(cascadesAnnotation.targetClass());
        saveCascades = cascadesAnnotation.saves();
    }


    /**
     * @return array of save cascades; empty array if no save cascades defined for field
     */
    public SaveCascade[] getSaveCascades()
    {
        return saveCascades;
    }
}
