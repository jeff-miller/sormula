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
import org.sormula.annotation.cascade.InsertCascade;
import org.sormula.annotation.cascade.OneToManyCascade;
import org.sormula.annotation.cascade.OneToOneCascade;
import org.sormula.operation.cascade.CascadeOperation;
import org.sormula.operation.cascade.InsertCascadeOperation;
import org.sormula.reflect.SormulaField;
import org.sormula.translator.RowTranslator;


/**
 * Sql insert operation for row of type R. 
 *
 * @since 1.0
 * @author Jeff Miller
 * @param <R> class type which contains members for columns of a row in a table
 */
public class InsertOperation<R> extends ModifyOperation<R>
{
    /**
     * Constructs for standard sql insert statement as:<br>
     * INSERT INTO table (c1, c2, c3, ...) VALUES (?, ?, ?, ...)
     * 
     * @param table insert into this table
     * @throws OperationException if error
     */
    public InsertOperation(Table<R> table) throws OperationException
    {
        super(table);
        initBaseSql();
    }

    
    /**
     * Sets base sql with {@link #setBaseSql(String)}.
     */
    protected void initBaseSql()
    {
        String tableName = getTable().getQualifiedTableName();
        RowTranslator<R> rowTranslator = getTable().getRowTranslator();
        String columnPhrase = rowTranslator.createColumnPhrase();
        String unusedColumnNames = rowTranslator.getUnusedColumnInsertNamesSql();
        String parameterPhrase = rowTranslator.createParameterPhrase();
        String unusedColumnValues = rowTranslator.getUnusedColumnInsertValuesSql();
        
        StringBuilder sql = new StringBuilder(columnPhrase.length() + unusedColumnNames.length() +
                parameterPhrase.length() + unusedColumnValues.length() + tableName.length() + 50);
        
        sql.append("INSERT INTO ");
        sql.append(tableName);
        sql.append("(");
        sql.append(columnPhrase);
        sql.append(unusedColumnNames);
        sql.append(") VALUES (");
        sql.append(parameterPhrase);
        sql.append(unusedColumnValues);
        sql.append(")");
        
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
        InsertCascade[] insertCascades = null;
        
        if (field.isAnnotationPresent(OneToManyCascade.class))
        {
            OneToManyCascade cascadesAnnotation = field.getAnnotation(OneToManyCascade.class);
            targetTable = getTargetTable(cascadesAnnotation.targetClass(), field);
            insertCascades = cascadesAnnotation.inserts();            
        }
        else if (field.isAnnotationPresent(OneToOneCascade.class))
        {
            OneToOneCascade cascadesAnnotation = field.getAnnotation(OneToOneCascade.class);
            targetTable = getTargetTable(field.getType(), field);
            insertCascades = cascadesAnnotation.inserts();            
        }
        else if (field.isAnnotationPresent(Cascade.class))
        {
            Cascade cascadesAnnotation = field.getAnnotation(Cascade.class);
            targetTable = getTargetTable(cascadesAnnotation.targetClass(), field);
            insertCascades = cascadesAnnotation.inserts();
        }
        
        if (targetTable != null && insertCascades != null)
        {
            SormulaField<R, ?> targetField = createTargetField(field);
            co = new ArrayList<CascadeOperation<R, ?>>(insertCascades.length);
            
            // for each cascade operation
            for (InsertCascade c: insertCascades)
            {
                @SuppressWarnings("unchecked") // target field type is not known at compile time
                CascadeOperation<R, ?> operation = new InsertCascadeOperation(targetField, targetTable, c);
                operation.prepare();
                co.add(operation);
            }
        }
        
        return co;
    }        
}
