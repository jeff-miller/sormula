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
import org.sormula.annotation.Transient;
import org.sormula.annotation.cascade.SelectCascade;


/**
 * A lazy cascade selector that uses the same {@link Database} as source row select operation to
 * perform the lazy select.
 * <p>
 * SimpleLazySelector is good for scenario's when source row is maintained in memory
 * and database/connection used to select the source row remains open. Scenario's like this
 * occur in stand-alone applications and web applications where source is never stored in web session.  
 *  
 * @author Jeff Miller
 * @since 1.8
 * 
 * @param <R> type of source row class
 */
public class SimpleLazySelector<R> extends AbstractLazySelector<R>
{
    private static final long serialVersionUID = 1L;

    @Transient
    Database database;
    
    
    /**
     * Constructs for use when SimpleLazySelector is base class of row that will contain lazy select fields. Typically 
     * the derived class is the one side of a one-to-many relationship or subclass has a reference to the 
     * other class in a one-to-one relationship. {@link #setUseTransaction(boolean)} is true by default.
     */
    public SimpleLazySelector()
    {
    }
    
    
    /**
     * Constructs for use as delegate for row that will contain lazy select fields. 
     * {@link #setUseTransaction(boolean)} is true by default.
     * 
     * @param source row that contains fields with {@link SelectCascade#lazy()} is true; typically source
     * is the one side of a one-to-many relationship or source has a reference to the other class in a 
     * one-to-one relationship
     */
    public SimpleLazySelector(R source)
    {
        super(source);
    }
    
    
    @Override
    public void pendingLazySelects(Database database) throws LazyCascadeException
    {
        super.pendingLazySelects(database);
        this.database = database;
    }
    
    
    @Override
    protected Database initDatabase() throws LazyCascadeException
    {
        return database;
    }
}
