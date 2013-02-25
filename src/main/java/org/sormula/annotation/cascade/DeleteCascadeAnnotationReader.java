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
        OneToManyCascade cascadesAnnotation = source.getAnnotation(OneToManyCascade.class);
        setForeignKeyValueFields(cascadesAnnotation.foreignKeyValueFields());
        setForeignKeyReferenceField(cascadesAnnotation.foreignKeyReferenceField());
        
        if (!cascadesAnnotation.readOnly())
        {
            initTargetClass(cascadesAnnotation.targetClass());
            deleteCascades = cascadesAnnotation.deletes();
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
            deleteCascades = cascadesAnnotation.deletes();
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
        deleteCascades = cascadesAnnotation.deletes();
    }
    

    /**
     * @return array of delete cascades; empty array if no delete cascades defined for field
     */
    public DeleteCascade[] getDeleteCascades()
    {
        return deleteCascades;
    }
}
