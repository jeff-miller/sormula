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
package org.sormula.operation.cascade.lazy;

import org.sormula.Database;
import org.sormula.Table;
import org.sormula.annotation.cascade.SelectCascade;
import org.sormula.operation.ScalarSelectOperation;


/**
 * Interface for notifying and performing lazy select cascades. Used by {@link ScalarSelectOperation#readNext()}
 * when a least one field has a {@link SelectCascade#lazy()} set to true.
 * 
 * @author Jeff Miller
 * @since 1.8
 */
public interface LazySelectable
{
    /**
     * Informs a lazy selector that there are lazy selects to be performed. Use {@link Table#getLazySelectCascadeFields()}
     * to know which fields are to be lazily selected.
     * 
     * @param database the database where lazy select fields are to be read
     * @throws LazyCascadeException if error
     */
    public void pendingLazySelects(Database database) throws LazyCascadeException;
    
    
    /**
     * Checks if lazy select needs to be performed. Perform the select for field if it has not yet been selected.
     * Otherwise do nothing.
     * 
     * @param fieldName check field with this name
     * @throws LazyCascadeException if error
     */
    public void checkLazySelects(String fieldName) throws LazyCascadeException;
}
