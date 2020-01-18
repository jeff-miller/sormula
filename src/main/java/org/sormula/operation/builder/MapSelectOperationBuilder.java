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

import java.util.Map;
import java.util.function.Function;

import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.operation.MapSelectOperation;


/**
 * Base class for builders of {@link MapSelectOperation} objects.
 * 
 * @author Jeff Miller
 * @since 4.4
 * @param <K> type of key in map
 * @param <R> type of row in table
 * @param <B> type of builder
 * @param <T> type returned by {@link #build()}
 */
public abstract class MapSelectOperationBuilder<K, R, B extends MapSelectOperationBuilder, T extends MapSelectOperation<K, R>>
    extends SelectOperationBuilder<R, Map<K, R>, B, T>
{
    String getKeyMethodName;
    Function<R, K> keyFunction;
    
    
    public MapSelectOperationBuilder(Table<R> table) 
    {
        super(table);
    }
    
    
    @Override
    protected void init(T operation) throws SormulaException
    {
        super.init(operation);
        if (getKeyMethodName != null) operation.setGetKeyMethodName(getKeyMethodName);
        if (keyFunction != null) operation.setKeyFunction(keyFunction);
    }
    
    
    @SuppressWarnings("unchecked")
    public B getKeyMethodName(String getKeyMethodName)
    {
        this.getKeyMethodName = getKeyMethodName;
        return (B)this;
    }
    
    
    @SuppressWarnings("unchecked")
    public B keyFunction(Function<R, K> keyFunction)
    {
        this.keyFunction = keyFunction;
        return (B)this;
    }
}
