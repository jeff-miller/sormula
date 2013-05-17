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

import org.sormula.Database;
import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.examples.ExampleBase;
import org.sormula.operation.ArrayListSelectOperation;
import org.sormula.operation.ListSelectOperation;


/**
 * Same as SelectExample1 but does not always use default names. See {@link Student3}
 * for details about name differences.
 */
public class ComplexSelect extends ExampleBase
{
    Table<Student3> table;
    
    
    public static void main(String[] args) throws Exception
    {
        new ComplexInsert(); // create table and rows
        new ComplexSelect();
    }
    
    
    public ComplexSelect() throws Exception
    {
        // init
        openDatabase();
        
        try (Database database = new Database(getConnection(), getSchema()))
        {
            table = database.getTable(Student3.class);
            
            selectRow();
            selectAllRows();
            selectWhere();
        }
        
        // clean up
        closeDatabase();
    }
    
    
    void selectRow() throws SormulaException
    {
        System.out.println("table.select(1234)=" + table.select(1234));
    }
    
    
    void selectAllRows() throws SormulaException
    {
        System.out.println("table.selectAll():");
        printAll(table.selectAll());
    }
    
    
    void selectWhere() throws SormulaException
    {
        String whereParameter = "John";
        System.out.println("select where first name = " + whereParameter);
        
        try (ListSelectOperation<Student3> operation = new ArrayListSelectOperation<>(table, "fn"))
        {
            operation.setParameters(whereParameter);
            
            System.out.println("read as a collection");
            operation.execute();
            for (Student3 s: operation.readAll())
                System.out.println(s);
            
            System.out.println("read one row at a time");
            operation.execute();
            for (Student3 s = operation.readNext(); s != null; s = operation.readNext())
                System.out.println(s);
        }
    }
}
