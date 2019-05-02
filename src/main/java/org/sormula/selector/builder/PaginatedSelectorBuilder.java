package org.sormula.selector.builder;

import org.sormula.selector.PaginatedSelector;
import org.sormula.selector.SelectorException;


/**
 * Base class for builders of {@link PaginatedSelector} objects.
 * 
 * @author Jeff Miller
 * @since 4.4
 * @param <R> Class associated with a row in table
 * @param <B> Class of builder
 * @param <T> Class of object returned by {@link #build()}
 */
public abstract class PaginatedSelectorBuilder<R, B extends PaginatedSelectorBuilder, T extends PaginatedSelector<R, ?>>
{
    protected boolean scrollSensitive;
    int pageNumber;
    
    
    public PaginatedSelectorBuilder()
    {
        pageNumber = 1;
    }
    
    
    public abstract T build() throws SelectorException;
    
    
    protected void init(T selector) throws SelectorException
    {
        selector.setPageNumber(pageNumber);
    }
    
    
    @SuppressWarnings("unchecked")
    public B scrollSensitive(boolean scrollSensitive)
    {
        this.scrollSensitive = scrollSensitive;
        return (B)this;
    }   
    
    
    @SuppressWarnings("unchecked")
    public B pageNumber(int pageNumber)
    {
        this.pageNumber = pageNumber;
        return (B)this;
    }
}
