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
package org.sormula.examples.complex;

import java.sql.Connection;
import java.util.List;

import org.sormula.Database;
import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.examples.ExampleBase;


/**
 * Same as DeleteExample1 but does not always use default names. See {@link Student3}
 * for details about name differences.
 */
public class ComplexDelete extends ExampleBase
{
    Table<Student3> table;
    
    
    public static void main(String[] args) throws Exception
    {
        new ComplexInsert(); // create table and rows
        new ComplexDelete();
    }
    
    
    public ComplexDelete() throws Exception
    {
        // init
        openDatabase();
        Connection connection = getConnection();
        Database database = new Database(connection, getSchema());
        table = database.getTable(Student3.class);
        
        deleteByPrimaryKey();
        deleteRow();
        deleteRows();
        
        // clean up
        database.close();
        closeDatabase();
    }
    
    
    void deleteByPrimaryKey() throws SormulaException
    {
        int id = 9999;
        System.out.println("table.delete(" + id + ")");
        table.delete(id);
        printAll(table.selectAll());
    }
    
    
    void deleteRow() throws SormulaException
    {
        int id = 8888;
        System.out.println("table.delete(student) id=" + id);
        Student3 student = table.select(id);
        table.delete(student);
        printAll(table.selectAll());
    }
    
    
    void deleteRows() throws SormulaException
    {
        System.out.println("table.deleteAll()");
        List<Student3> list = table.selectAll();
        table.deleteAll(list);
        printAll(table.selectAll());
    }
}
