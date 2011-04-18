/* sormula - Simple object relational mapping
 * Copyright (C) 2011 Jeff Miller
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.sormula.operation;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.sormula.Table;
import org.sormula.annotation.cascade.Cascade;
import org.sormula.annotation.cascade.OneToManyCascade;
import org.sormula.annotation.cascade.OneToOneCascade;
import org.sormula.annotation.cascade.UpdateCascade;
import org.sormula.operation.cascade.CascadeOperation;
import org.sormula.operation.cascade.UpdateCascadeOperation;
import org.sormula.reflect.SormulaField;
import org.sormula.translator.RowTranslator;


/**
 * SQL update operation for row of type R. By default all rows will be affected unless
 * {@link #setWhere(String)} is used.
 *
 * @param <R> class type which contains members for columns of a row in a table
 * 
 * @since 1.0
 * @author Jeff Miller
 */
public class UpdateOperation<R> extends ModifyOperation<R>
{
    /**
     * Constructs for standard sql update statement as:<br>
     * UPDATE table SET c1=?, c2=?, c3...
     * 
     * @param table update this table
     * @throws OperationException if error
     */
    public UpdateOperation(Table<R> table) throws OperationException
    {
        super(table);
        
        if (table.getRowTranslator().getIdentityColumnTranslator() != null)
        {
            // has identity column, don't update it
            setIncludeIdentityColumns(false);
        }
        
        initBaseSql();
    }


    /**
     * Sets base sql with {@link #setBaseSql(String)}.
     */
    protected void initBaseSql()
    {
        String tableName = getTable().getQualifiedTableName();
        RowTranslator<R> rowTranslator = getTable().getRowTranslator();
        rowTranslator.setIncludeIdentityColumns(isIncludeIdentityColumns()); // usually true for updates
        String columnParameterPhrase = rowTranslator.createColumnParameterPhrase();
        
        StringBuilder sql = new StringBuilder(columnParameterPhrase.length() +  tableName.length() + 50);
        sql.append("UPDATE ");
        sql.append(tableName);
        sql.append(" SET ");
        sql.append(columnParameterPhrase);
        
        setBaseSql(sql.toString());
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected List<CascadeOperation<R, ?>> prepareCascades(Field field) throws OperationException
    {
        List<CascadeOperation<R, ?>> co = null;
        Table<?> targetTable = null;
        UpdateCascade[] updateCascades = null;
        
        if (field.isAnnotationPresent(OneToManyCascade.class))
        {
            OneToManyCascade cascadesAnnotation = field.getAnnotation(OneToManyCascade.class);
            targetTable = getTargetTable(cascadesAnnotation.targetClass(), field);
            updateCascades = cascadesAnnotation.updates();            
        }
        else if (field.isAnnotationPresent(OneToOneCascade.class))
        {
            OneToOneCascade cascadesAnnotation = field.getAnnotation(OneToOneCascade.class);
            targetTable = getTargetTable(field.getType(), field);
            updateCascades = cascadesAnnotation.updates();            
        }
        else if (field.isAnnotationPresent(Cascade.class))
        {
            Cascade cascadesAnnotation = field.getAnnotation(Cascade.class);
            targetTable = getTargetTable(cascadesAnnotation.targetClass(), field);
            updateCascades = cascadesAnnotation.updates();
        }
        
        if (targetTable != null && updateCascades != null)
        {
            SormulaField<R, ?> targetField = createTargetField(field);
            co = new ArrayList<CascadeOperation<R, ?>>(updateCascades.length);
            
            // for each cascade operation
            for (UpdateCascade c: updateCascades)
            {
                @SuppressWarnings("unchecked") // target field type is not known at compile time
                CascadeOperation<R, ?> operation = new UpdateCascadeOperation(targetField, targetTable, c);
                operation.prepare();
                co.add(operation);
            }
        }
        
        return co;
    }        
}
