/* sormula - Simple object relational mapping
 * Copyright (C) 2011-2012 Jeff Miller
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
package org.sormula.operation.cascade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.sormula.Table;
import org.sormula.annotation.cascade.InsertCascade;
import org.sormula.operation.InsertOperation;
import org.sormula.operation.OperationException;
import org.sormula.reflect.ReflectException;
import org.sormula.reflect.SormulaField;
import org.sormula.translator.ColumnTranslator;
import org.sormula.translator.RowTranslator;


/**
 * Cascade that inserts rows into target table when source operation initiates a cascade.
 * 
 * @author Jeff Miller
 *
 * @param <S> row class of table that is source of cascade
 * @param <T> row class of table that is target of cascade
 */
public class InsertCascadeOperation<S, T> extends ModifyCascadeOperation<S, T>
{
    InsertCascade insertCascadeAnnotation;
    List<SormulaField<S, Object>> sourceKeyFieldList;
    List<SormulaField<T, Object>> targetForeignKeyFieldList;
    int keyFieldCount;
    
    
    /**
     * Constructor used by {@link InsertOperation}.
     *  
     * @param targetField cascade insert operation uses row(s) from this field
     * @param targetTable cascade insert operation is performed on this table 
     * @param insertCascadeAnnotation cascade operation
     */
    @Deprecated // use constructor that contains source table
    public InsertCascadeOperation(SormulaField<S, ?> targetField, Table<T> targetTable, InsertCascade insertCascadeAnnotation)
    {
        super(targetField, targetTable, insertCascadeAnnotation.operation(), insertCascadeAnnotation.post());
        this.insertCascadeAnnotation = insertCascadeAnnotation;
    }
    
    
    /**
     * Constructor used by {@link InsertOperation}.
     *
     * @param sourceTable cascade orgininates on row from this table
     * @param targetField cascade insert operation uses row(s) from this field
     * @param targetTable cascade insert operation is performed on this table 
     * @param insertCascadeAnnotation cascade operation
     * @since 3.0
     */
    public InsertCascadeOperation(Table<S> sourceTable, SormulaField<S, ?> targetField, Table<T> targetTable, InsertCascade insertCascadeAnnotation)
    {
        super(sourceTable, targetField, targetTable, insertCascadeAnnotation.operation(), insertCascadeAnnotation.post());
        this.insertCascadeAnnotation = insertCascadeAnnotation;
    }

    
    @Override
    public void prepare() throws OperationException
    {
        super.prepare();
        prepareForeignKeyMapping();
    }

    
    /**
     * Prepares accessors that will set foreign key(s) on cascaded target rows as defined by
     * {@link InsertCascade#targetForeignKeyFields()}.
     * 
     * @throws OperationException if error
     */
    protected void prepareForeignKeyMapping() throws OperationException
    {
        String[] foreignKeyFieldNames = insertCascadeAnnotation.targetForeignKeyFields();
        
        if (foreignKeyFieldNames.length > 0)
        {
            // at least one foreign key in target
            List<ColumnTranslator<S>> sourceKeyColumnTranslators = getSourceTable().
                    getRowTranslator().getPrimaryKeyWhereTranslator().getColumnTranslatorList();
            RowTranslator<T> targetRowTranslator = getTargetTable().getRowTranslator();
            
            // parallel lists mapping source primary key(s) to target foreign key(s)
            sourceKeyFieldList = new ArrayList<SormulaField<S,Object>>(sourceKeyColumnTranslators.size());
            targetForeignKeyFieldList = new ArrayList<SormulaField<T, Object>>(sourceKeyColumnTranslators.size());
            
            try
            {
                // for all source primary key(s)
                int foreignKeyNameIndex = 0;
                boolean sametargetAndSourceNames = foreignKeyFieldNames[0].equals("*");
                for (ColumnTranslator<S> sct : sourceKeyColumnTranslators)
                {
                    String targetFieldName;
                    if (sametargetAndSourceNames) targetFieldName = sct.getField().getName();
                    else                          targetFieldName = foreignKeyFieldNames[foreignKeyNameIndex++];
                    
                    ColumnTranslator<T> tct = targetRowTranslator.getColumnTranslator(targetFieldName);
                    if (tct != null)
                    {
                        // target foreign key field exists for corresponding source primary key field
                        // add each to parallel lists
                        sourceKeyFieldList.add(new SormulaField<S, Object>(sct.getField()));
                        targetForeignKeyFieldList.add(new SormulaField<T, Object>(tct.getField()));
                    }
                    else
                    {
                        throw new OperationException(targetFieldName + " does not exist in " + getTargetTable().getRowClass() + 
                                " as specified with InsertCascade in " + sourceTable.getRowClass());
                    }
                }
            }
            catch (ReflectException e)
            {
                throw new OperationException("error creating method access for InsertCascade", e);
            }
            
            keyFieldCount = sourceKeyFieldList.size();
        }
    }
    

    @Override
    protected void modify(T row) throws OperationException
    {
        setForeignKeys(row);
    }

    
    @Override
    protected void modify(T[] rows) throws OperationException
    {
        if (keyFieldCount > 0)
        {
            // foreign key mapping is desired
            for (T t : rows) modify(t);
        }
    }

    
    @Override
    protected void modify(Collection<T> rows) throws OperationException
    {
        if (keyFieldCount > 0)
        {
            // foreign key mapping is desired
            for (T t : rows) modify(t);
        }
    }
    
    
    @Override
    protected void modify(Map<?, T> rowMap) throws OperationException
    {
        if (keyFieldCount > 0)
        {
            // foreign key mapping is desired
            modify(rowMap.values());
        }
    }
    
    
    /**
     * Sets the foreign key(s) in target (child) row from primary key(s) in source (parent) row.
     * 
     * @param row target (child) row to affect
     * @throws OperationException if error
     */
    protected void setForeignKeys(T row) throws OperationException
    {
        try
        {
            // for each key within a row
            for (int i = 0; i < keyFieldCount; ++i)
            {
                // set source key on target row foreign key field
                Object sourceKey = sourceKeyFieldList.get(i).invokeGetMethod(sourceRow);
                targetForeignKeyFieldList.get(i).invokeSetMethod(row, sourceKey);
            }
        }
        catch (ReflectException e)
        {
            throw new OperationException("TODO", e);
        }
    }
}
