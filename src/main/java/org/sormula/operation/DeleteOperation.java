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
package org.sormula.operation;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.sormula.Table;
import org.sormula.annotation.cascade.Cascade;
import org.sormula.annotation.cascade.DeleteCascade;
import org.sormula.annotation.cascade.OneToManyCascade;
import org.sormula.annotation.cascade.OneToOneCascade;
import org.sormula.operation.cascade.CascadeOperation;
import org.sormula.operation.cascade.DeleteCascadeOperation;
import org.sormula.reflect.SormulaField;


/**
 * SQL delete operation for row of type R. 
 *
 * @since 1.0
 * @author Jeff Miller
 * @param <R> class type which contains members for columns of a row in a table
 */
public class DeleteOperation<R> extends ModifyOperation<R>
{
    /**
     * Constructs standard sql delete for primary key as:<br>
     * DELETE FROM table WHERE primary key clause
     * 
     * @param table delete from this table
     * @throws OperationException if error
     */
    public DeleteOperation(Table<R> table) throws OperationException
    {
        this(table, "primaryKey");
    }
    
    
    /**
     * Constructs for standard sql delete statement as:<br>
     * DELETE FROM table WHERE ...
     * 
     * @param table delete from this table
     * @param whereConditionName name of where condition to use ("primaryKey" to delete
     * by primary key; empty string to delete all rows in table)
     * @throws OperationException if error
     */
    public DeleteOperation(Table<R> table, String whereConditionName) throws OperationException
    {
        super(table);
        initBaseSql();
        setWhere(whereConditionName);
    }


    /**
     * Deletes a row. Set parameters, executes, closes. 
     * Alias for {@link #modify(Object)}.
     * 
     * @param row row to use for parameters
     * @return {@link #getRowsAffected()}
     * @throws OperationException if error
     * @since 1.4
     */
    public int delete(R row) throws OperationException
    {
        return super.modify(row);
    }


    /**
     * Deletes all rows in collection. Set parameters, executes, closes. 
     * Alias for {@link #modifyAll(Collection)}.
     * 
     * @param rows collection of rows to use as parameters 
     * @return {@link #getRowsAffected()}
     * @throws OperationException if error
     * @since 1.4
     */
    public int deleteAll(Collection<R> rows) throws OperationException
    {
        return super.modifyAll(rows);
    }


    /**
     * Deletes rows based upon parameters. Set parameters, executes, closes. 
     * Alias for {@link #modify(Object...)}.
     * 
     * @param parameters operation parameters as objects (see {@link #setParameters(Object...)})
     * @return count of rows affected
     * @throws OperationException if error
     * @since 1.4
     */
    public int delete(Object... parameters) throws OperationException
    {
        return super.modify(parameters);
    }


    @Override
    protected void writeColumns(R row) throws OperationException
    {
        // no columns are prepared for delete
    }


    /**
     * Sets base sql with {@link #setBaseSql(String)}.
     */
    protected void initBaseSql()
    {
        String tableName = getTable().getQualifiedTableName();
        StringBuilder sql = new StringBuilder(tableName.length() + 50);
        
        sql.append("DELETE FROM ");
        sql.append(tableName);
        
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
        DeleteCascade[] deleteCascades = null;
        
        if (field.isAnnotationPresent(OneToManyCascade.class))
        {
            OneToManyCascade cascadesAnnotation = field.getAnnotation(OneToManyCascade.class);
            
            if (!cascadesAnnotation.readOnly())
            {
                targetTable = getTargetTable(cascadesAnnotation.targetClass(), field);
                deleteCascades = cascadesAnnotation.deletes();
            }
        }
        else if (field.isAnnotationPresent(OneToOneCascade.class))
        {
            OneToOneCascade cascadesAnnotation = field.getAnnotation(OneToOneCascade.class);
            
            if (!cascadesAnnotation.readOnly())
            {
                targetTable = getTargetTable(field.getType(), field);
                deleteCascades = cascadesAnnotation.deletes();
            }
        }
        else if (field.isAnnotationPresent(Cascade.class))
        {
            Cascade cascadesAnnotation = field.getAnnotation(Cascade.class);
            targetTable = getTargetTable(cascadesAnnotation.targetClass(), field);
            deleteCascades = cascadesAnnotation.deletes();
        }
        
        if (targetTable != null && deleteCascades != null)
        {
            SormulaField<R, ?> targetField = createTargetField(field);
            co = new ArrayList<>(deleteCascades.length);
            
            // for each cascade operation
            for (DeleteCascade c: deleteCascades)
            {
                @SuppressWarnings("unchecked") // target field type is not known at compile time
                CascadeOperation<R, ?> operation = new DeleteCascadeOperation(targetField, targetTable, c);
                operation.prepare();
                co.add(operation);
            }
        }
        else
        {
            // no cascades
            co = Collections.emptyList();
        }
        
        return co;
    }
}
