package org.sormula.selector.builder;

import org.sormula.selector.PaginatedSelector;
import org.sormula.selector.SelectorException;


/**
 * Base class for builders of {@link PaginatedSelector} objects.
 * 
 * @author Jeff Miller
 * @since 4.4
 * @param <R> class associated with a row in table
 * @param <C> collection type for one page
 * @param <B> class of builder
 * @param <T> class of object returned by {@link #build()}
 */
public abstract class PaginatedSelectorBuilder<R, C, B extends PaginatedSelectorBuilder, T extends PaginatedSelector<R, C>>
{
    protected Boolean scrollSensitive;
    Integer pageNumber;
    
    
    public abstract T build() throws SelectorException;
    
    
    protected void init(T selector) throws SelectorException
    {
        if (pageNumber != null) selector.setPageNumber(pageNumber);
        // scrollSensitive used by superclasses as a constructor parameter
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
