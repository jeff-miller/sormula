/* sormula - Simple object relational mapping
 * Copyright (C) 2011-2020 Jeff Miller
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
package org.sormula.operation.builder;

import java.util.Collection;
import java.util.Map;

import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.operation.ModifyOperation;


/**
 * Base class for builders of {@link ModifyOperation} objects.
 * 
 * @author Jeff Miller
 * @since 4.4
 * @param <R> type of row in table
 * @param <B> type of builder
 * @param <T> type returned by {@link #build()}
 */
public abstract class ModifyOperationBuilder<R, B extends ModifyOperationBuilder, T extends ModifyOperation<R>>
    extends SqlOperationBuilder<R, B, T>
{
    Boolean batch;
    Object[] parameters;
    R row;
    Collection<R> rowCollection;
    R[] rowArray;
    Map<?, R> rowMap;
    
    
    public ModifyOperationBuilder(Table<R> table) 
    {
        super(table);
    }


    @Override
    protected void init(T operation) throws SormulaException
    {
        super.init(operation);
        if (batch != null) operation.setBatch(batch);
        
        if (parameters != null) operation.setParameters(parameters);
        else if (row != null) operation.setRow(row);
        else if (rowCollection != null) operation.setRows(rowCollection);
        else if (rowArray != null) operation.setRows(rowArray);
        else if (rowMap != null) operation.setRows(rowMap);
    }
    
    
    @SuppressWarnings("unchecked")
    public B batch(boolean batch)
    {
        this.batch = batch;
        return (B)this;
    }
    
    
    @SuppressWarnings("unchecked")
    public B parameters(Object... parameters)
    {
        this.parameters = parameters;
        return (B)this;
    }
    
    
    @SuppressWarnings("unchecked")
    public B row(R row)
    {
        this.row = row;
        return (B)this;
    }
    
    
    @SuppressWarnings("unchecked")
    public B rows(Collection<R> rows)
    {
        rowCollection = rows;
        return (B)this;
    }
    
    
    @SuppressWarnings("unchecked")
    public B rows(Map<?, R> rows)
    {
        rowMap = rows;
        return (B)this;
    }
    
    
    @SuppressWarnings("unchecked")
    public B rows(R[] rows)
    {
        rowArray = rows;
        return (B)this;
    }
}
