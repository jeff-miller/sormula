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

    
    public AbstractPaginatedListSelectorBuilder()
    {
        parameterMap = new HashMap<>();
    }
    
    
    @Override
    protected void init(T selector) throws SelectorException
    {
        super.init(selector);
        selector.setWhere(whereConditionName);;
        if (parameters != null) selector.setParameters(parameters);
        parameterMap.forEach((k, v) -> selector.setParameter(k, v));
        if (orderByName != null) selector.setOrderByName(orderByName);
    }
    
    
    @SuppressWarnings("unchecked")
    public B where(String whereConditionName)
    {
        this.whereConditionName = whereConditionName;
        return (B)this;
    }
    
    
    @SuppressWarnings("unchecked")
    public B parameters(Object... parameters)
    {
        this.parameters = parameters;
        return (B)this;
    }
    
    
    @SuppressWarnings("unchecked")
    public B parameter(String name, Object value)
    {
        parameterMap.put(name, value);
        return (B)this;
    }
    
    
    @SuppressWarnings("unchecked")
    public B orderByName(String orderByName)
    {
        this.orderByName = orderByName;
        return (B)this;
    }
}
