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
package org.sormula.tests.active;

import org.sormula.active.ActiveDatabase;
import org.sormula.active.ActiveRecord;
import org.sormula.active.ActiveTable;
import org.sormula.tests.DatabaseTest;


/** 
 * Base class for all org.sormula.active tests. 
 * 
 * @author Jeff Miller
 */
public class ActiveDatabaseTest<R extends ActiveRecord<R>> extends DatabaseTest<R>
{
    ActiveDatabase activeDatabase;
    ActiveTable<R> activeTable;
    
    
    @Override
    public void openDatabase() throws Exception
    {
        super.openDatabase();
        activeDatabase = new ActiveDatabase(getDataSource(), getSchema());
        activeDatabase.setTimings(Boolean.parseBoolean(System.getProperty("timings")));
    }
    
    
    @Override
    protected void createTable(Class<R> rowClass) throws Exception
    {
        super.createTable(rowClass);
        activeTable = new ActiveTable<>(activeDatabase, rowClass);
        assert activeTable != null : "active table creation error";
    }


    public ActiveDatabase getActiveDatabase()
    {
        return activeDatabase;
    }


    public ActiveTable<R> getActiveTable()
    {
        return activeTable;
    }


    @Override
    public void selectTestRows() 
    {
        setAll(activeTable.selectAll());
    }
}