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
package org.sormula.examples.cascade;

import java.util.List;

import org.sormula.Database;
import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.examples.ExampleBase;


public class CascadeUpdate extends ExampleBase
{
    Table<Student4> table;
    
    
    public static void main(String[] args) throws Exception
    {
        new CascadeInsert(); // create table and rows
        new CascadeUpdate();
        new CascadeSelect(); // display results
    }
    
    
    public CascadeUpdate() throws Exception
    {
        // init
        openDatabase();
        
        try (Database database = new Database(getConnection(), getSchema()))
        {
            database.setTimings(true); // enable timings for all operations 
            
            table = database.getTable(Student4.class);
            updateRows();
            database.logTimings();  // log all timings
        }
        
        // clean up
        closeDatabase();
    }
    
    
    void updateRows() throws SormulaException
    {
        List<Student4> list = table.selectAll();
        
        // change all enrollment to semester 4
        for (Student4 s: list)
        {
            for (Enrolled e: s.getEnrollment())
            {
                e.setSemester(4);
            }
        }
        
        // updates students and enrolled
        table.updateAll(list);
    }
}
