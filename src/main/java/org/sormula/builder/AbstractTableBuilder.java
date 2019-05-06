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
package org.sormula.builder;

import org.sormula.Database;
import org.sormula.SormulaException;
import org.sormula.Table;


/**
 * TODO
 * @since 4.4
 * @author Jeff Miller
 *
 * @param <R>
 * @param <B>
 * @param <T>
 */
public abstract class AbstractTableBuilder<R, B extends AbstractTableBuilder, T extends Table<R>>
{
    Database database;
    Class<R> rowClass;
    boolean autoGeneratedKeys;
    
    
    public AbstractTableBuilder(Database database, Class<R> rowClass)
    {
        this.database = database;
        this.rowClass = rowClass;
        autoGeneratedKeys = database.isAutoGeneratedKeys(); // default
    }

    
    public abstract T build() throws SormulaException;
    
    
    protected void init(T table)
    {
        table.setAutoGeneratedKeys(autoGeneratedKeys);
    }
    
    
    @SuppressWarnings("unchecked")
    public B autoGeneratedKeys(boolean autoGeneratedKeys)
    {
        this.autoGeneratedKeys = autoGeneratedKeys;
        return (B)this;
    }
}
