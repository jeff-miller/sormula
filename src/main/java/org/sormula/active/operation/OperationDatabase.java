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
package org.sormula.active.operation;

import java.sql.SQLException;

import org.sormula.Database;
import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.active.ActiveDatabase;
import org.sormula.active.ActiveRecord;


/**
 * {@link Database} for one transaction within an {@link ActiveOperation}. Creates {@link OperationTable}
 * for {@link Database#getTable(Class)} for all tables in operation. 
 * <p>
 * Intended for use by {@link ActiveOperation} only.
 * 
 * @author Jeff Miller
 * @since 1.7
 */
public class OperationDatabase extends Database
{
    ActiveDatabase activeDatabase;
    
    
    /**
     * Constructs for an active database.
     * 
     * @param activeDatabase active database determines data source
     * @throws SQLException if error
     */
    public OperationDatabase(ActiveDatabase activeDatabase) throws SQLException
    {
        super(activeDatabase.getDataSource().getConnection(), activeDatabase.getSchema());
        this.activeDatabase = activeDatabase;
    }
    
    
    /**
     * Gets the active database supplied in the constructor.
     * 
     * @return active database
     */
    public ActiveDatabase getActiveDatabase()
    {
        return activeDatabase;
    }


    /**
     * {@inheritDoc}
     * Cascade operations for Table<R> may invoke getTable for some cascaded Table<T> where T is different from R.
     */
    @Override
    @SuppressWarnings("unchecked") // can't change signature to R<extends ActiveRecord>
    public <R> Table<R> getTable(Class<R> rowClass) throws SormulaException
    {
        Table<R> table = getTable(rowClass, false);
        
        if (table == null)
        {
            // init row translator that sets the active database
            table = (Table<R>)new OperationTable<ActiveRecord>(this, (Class<ActiveRecord>)rowClass);
            
            // add it for reuse
            addTable(table);
        }

        return table;
    }
}
