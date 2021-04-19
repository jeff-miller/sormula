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
package org.sormula.selector.builder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sormula.selector.AbstractPaginatedListSelector;
import org.sormula.selector.SelectorException;


/**
 * Base class for builders of {@link AbstractPaginatedListSelector} objects.
 * 
 * @author Jeff Miller
 * @since 4.4
 * @param <R> class associated with a row in table
 * @param <B> class of builder
 * @param <T> class of object returned by {@link #build()}
 */
public abstract class AbstractPaginatedListSelectorBuilder<R, B extends PaginatedSelectorBuilder, T extends AbstractPaginatedListSelector<R>> 
    extends PaginatedSelectorBuilder<R, List<R>, B, T>
{
    String whereConditionName;
    Object[] parameters;
    Map<String, Object> parameterMap;
    String orderByName;

    
    /**
     * Constructs.
     */
    public AbstractPaginatedListSelectorBuilder()
    {
        parameterMap = new HashMap<>();
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void init(T selector) throws SelectorException
    {
        super.init(selector);
        selector.setWhere(whereConditionName);;
        if (parameters != null) selector.setParameters(parameters);
        parameterMap.forEach((k, v) -> selector.setParameter(k, v));
        if (orderByName != null) selector.setOrderByName(orderByName);
    }
    
    
    /**
     * @param whereConditionName see {@link AbstractPaginatedListSelector}
     * @return this
     */
    @SuppressWarnings("unchecked")
    public B where(String whereConditionName)
    {
        this.whereConditionName = whereConditionName;
        return (B)this;
    }
    
    
    /**
     * @param parameters see {@link AbstractPaginatedListSelector}
     * @return this
     */
    @SuppressWarnings("unchecked")
    public B parameters(Object... parameters)
    {
        this.parameters = parameters;
        return (B)this;
    }
    

    /**
     * @param name see {@link AbstractPaginatedListSelector}
     * @param value see {@link AbstractPaginatedListSelector}
     * @return this
     */
    @SuppressWarnings("unchecked")
    public B parameter(String name, Object value)
    {
        parameterMap.put(name, value);
        return (B)this;
    }
    
    
    /**
     * @param orderByName see {@link AbstractPaginatedListSelector}
     * @return this
     */
    @SuppressWarnings("unchecked")
    public B orderByName(String orderByName)
    {
        this.orderByName = orderByName;
        return (B)this;
    }
}
