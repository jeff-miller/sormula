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

import java.sql.Connection;

import org.sormula.Database;
import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.examples.ExampleBase;


public class CascadeDelete extends ExampleBase
{
    Table<Student4> table;
    
    
    public static void main(String[] args) throws Exception
    {
        new CascadeInsert(); // create table and rows
        new CascadeDelete();
        new CascadeSelect(); // display results
    }
    
    
    public CascadeDelete() throws Exception
    {
        // init
        openDatabase();
        Connection connection = getConnection();
        Database database = new Database(connection, getSchema());
        
        table = database.getTable(Student4.class);
        deleteRow();
        
        // clean up
        closeDatabase();
    }
    
    
    void deleteRow() throws SormulaException
    {
        int id = 8888;
        System.out.println("table.delete(student) id=" + id);
        Student4 student = table.select(id);
        
        // deletes student and enrolled
        table.delete(student);
    }
}
