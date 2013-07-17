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
import org.sormula.active.ActiveException;
import org.sormula.active.ActiveRecord;
import org.sormula.annotation.ExplicitTypeAnnotationReader;
import org.sormula.log.ClassLogger;
import org.sormula.translator.NameTranslator;


/**
 * {@link Database} for one transaction within an {@link ActiveOperation}. Creates {@link OperationTable}
 * for {@link Database#getTable(Class)} for all tables in operation. 
 * <p>
 * Intended for use by {@link ActiveOperation} only.
 * 
 * @author Jeff Miller
 * @since 1.7 and 2.1
 */
public class OperationDatabase extends Database
{
    private static final ClassLogger log = new ClassLogger();
    ActiveDatabase activeDatabase;
    
    
    /**
     * Constructs for an active database.
     * 
     * @param activeDatabase active database determines data source
     * @throws SQLException if error
     */
    public OperationDatabase(ActiveDatabase activeDatabase) throws Exception
    {
        super(activeDatabase.getDataSource().getConnection(), activeDatabase.getSchema());
        this.activeDatabase = activeDatabase;
        
        // configure explicit types from active database object
        new ExplicitTypeAnnotationReader(this, activeDatabase.getClass()).install();
        
        // initialized from active database
        setReadOnly(activeDatabase.isReadOnly());
        
        // copy all name translators 
        for (Class<? extends NameTranslator> ntc: activeDatabase.getNameTranslatorClasses())
        {
            addNameTranslatorClass(ntc);
        }
        
        setTimings(activeDatabase.isTimings());
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
    @SuppressWarnings("unchecked") // can't change signature to R<? extends ActiveRecord>
    public <R> Table<R> getTable(Class<R> rowClass) throws SormulaException
    {
        Table<R> table = getTable(rowClass, false);
        
        if (table == null)
        {
            // init row translator that sets the active database
            if (log.isDebugEnabled()) log.debug("create OperationTable for " + rowClass.getCanonicalName());
            table = (Table<R>)new OperationTable<ActiveRecord>(this, (Class<ActiveRecord>)rowClass);

            // add it for reuse
            addTable(table);
        }

        return table;
    }


    /**
     * Closes connection and then invokes {@link Database#close()}.
     */
    @Override
    public void close()
    {
        // super.close() will not close connection since this was created with connection constructor
        // so close connection now
        try
        {
            getConnection().close();
        }
        catch (SQLException e)
        {
            throw new ActiveException("error closing connection", e);
        }
        
        super.close();
    }
}
