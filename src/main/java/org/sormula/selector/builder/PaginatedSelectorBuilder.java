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
    Boolean scrollSensitive;
    Integer pageNumber;
    
    
    /**
     * Creates a {@link PaginatedSelector}.
     * @return a concrete implementation of PaginatedSelector
     * @throws SelectorException if error
     */
    public abstract T build() throws SelectorException;
    
    
    /**
     * Sets properties of selector parameter from corresponding builder properties.
     * 
     * @param selector selector to initialize
     * @throws SelectorException if error
     */
    protected void init(T selector) throws SelectorException
    {
        if (pageNumber != null) selector.setPageNumber(pageNumber);
        // scrollSensitive used by superclasses as a constructor parameter
    }
    
    
    /**
     * @param scrollSensitive see {@link PaginatedSelector}
     * @return this
     */
    @SuppressWarnings("unchecked")
    public B scrollSensitive(boolean scrollSensitive)
    {
        this.scrollSensitive = scrollSensitive;
        return (B)this;
    }   
    

    /**
     * @param pageNumber see {@link PaginatedSelector}
     * @return this
     */
    @SuppressWarnings("unchecked")
    public B pageNumber(int pageNumber)
    {
        this.pageNumber = pageNumber;
        return (B)this;
    }
}
