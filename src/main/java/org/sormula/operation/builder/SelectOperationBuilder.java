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

import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.operation.SelectOperation;


/**
 * Base class for builders of {@link SelectOperation} objects.
 * 
 * @author Jeff Miller
 * @since 4.4
 * @param <R> type of row in table
 * @param <C> collection type returned 
 * @param <B> type of builder
 * @param <T> type returned by {@link #build()}
 */
public abstract class SelectOperationBuilder<R, C, B extends SelectOperationBuilder, T extends SelectOperation<R, C>>
    extends AbstractScalarSelectOperationBuilder<R, B, T>
{
    Integer defaultReadAllSize;
    Integer fetchSize;
    Integer resultSetType;
    
    
    public SelectOperationBuilder(Table<R> table) 
    {
        super(table);
    }


    @Override
    protected void init(T operation) throws SormulaException
    {
        super.init(operation);
        
        if (defaultReadAllSize != null) operation.setDefaultReadAllSize(defaultReadAllSize);
        if (fetchSize != null) operation.setFetchSize(fetchSize);
        if (resultSetType != null) operation.setResultSetType(resultSetType);
    }
    
    
    @SuppressWarnings("unchecked")
    public B defaultReadAllSize(int defaultReadAllSize)
    {
        this.defaultReadAllSize = defaultReadAllSize;
        return (B)this;
    }
    
    
    @SuppressWarnings("unchecked")
    public B fetchSize(int fetchSize)
    {
        this.fetchSize = fetchSize;
        return (B)this;
    }
    
    
    @SuppressWarnings("unchecked")
    public B resultSetType(int resultSetType)
    {
        this.resultSetType = resultSetType;
        return (B)this;
    }
}
