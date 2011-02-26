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
import org.sormula.annotation.cascade.DeleteCascade;
import org.sormula.operation.cascade.CascadeOperation;
import org.sormula.operation.cascade.DeleteCascadeOperation;
import org.sormula.reflect.SormulaField;


/**
 * Sql delete operation for row of type R. By default all rows will be affected unless
 * {@link #setWhere(String)} is used.
 *
 * @since 1.0
 * @author Jeff Miller
 * @param <R> class type which contains members for columns of a row in a table
 */
public class DeleteOperation<R> extends ModifyOperation<R>
{
    /**
     * Constructs for standard sql delete statement as:<br>
     * DELETE FROM table 
     * 
     * @param table delete from this table
     * @throws OperationException if error
     */
    public DeleteOperation(Table<R> table) throws OperationException
    {
        super(table);
        initBaseSql();
    }


    @Override
    protected void prepareColumns(R row) throws OperationException
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
    	Cascade cascadesAnnotation = field.getAnnotation(Cascade.class);
        Table<?> targetTable = getTargetTable(cascadesAnnotation, field);
        SormulaField<R, ?> targetField = createTargetField(field);
        List<CascadeOperation<R, ?>> cascadeOperations = new ArrayList<CascadeOperation<R, ?>>(
                cascadesAnnotation.deletes().length);
        
        // for each cascade operation
        for (DeleteCascade c: cascadesAnnotation.deletes())
        {
            @SuppressWarnings("unchecked") // target field type is not known at compile time
            CascadeOperation<R, ?> o = new DeleteCascadeOperation(targetField, targetTable, c);
            o.prepare();
            cascadeOperations.add(o);
        }
        
        return cascadeOperations;
    }
}
