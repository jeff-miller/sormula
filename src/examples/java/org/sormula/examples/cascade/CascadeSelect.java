/* sormula - Simple object relational mapping
 * Copyright (C) 2011 Jeff Miller
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

import java.sql.Connection;

import org.sormula.Database;
import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.examples.ExampleBase;


public class CascadeSelect extends ExampleBase
{
    Table<Student4> table;
    
    
    public static void main(String[] args) throws Exception
    {
        new CascadeInsert(); // create table and rows
        new CascadeSelect();
    }
    
    
    public CascadeSelect() throws Exception
    {
        // init
        openDatabase();
        Connection connection = getConnection();
        Database database = new Database(connection, getSchema());
        table = database.getTable(Student4.class);
        
        selectAllRows();
        
        // clean up
        closeDatabase();
    }
    
    
    void selectAllRows() throws SormulaException
    {
        System.out.println("table.selectAll():");
        for (Student4 s: table.selectAll())
        {
            System.out.println(s);
            System.out.println("  enrolled:");
            
            for (Enrolled e: s.getEnrollment())
            {
                System.out.println("  " + e);    
            }
        }
    }
}
