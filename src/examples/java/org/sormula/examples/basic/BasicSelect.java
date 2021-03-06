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
package org.sormula.examples.basic;

import java.util.ArrayList;

import org.sormula.Database;
import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.annotation.Where;
import org.sormula.examples.ExampleBase;
import org.sormula.operation.ArrayListSelectOperation;
import org.sormula.operation.ListSelectOperation;
import org.sormula.operation.OperationException;


public class BasicSelect extends ExampleBase
{
    Table<Student> table;
    
    
    public static void main(String[] args) throws Exception
    {
        new BasicInsert(); // create table and rows
        new BasicSelect();
    }
    
    
    public BasicSelect() throws Exception
    {
        // init
        openDatabase();

        try (Database database = new Database(getConnection(), getSchema()))
        {
            table = database.getTable(Student.class);
            
            selectRow();
            selectAllRows();
            selectWhere();
            selectIn();
            selectWhere2();
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
        
        try (ListSelectOperation<Student> operation = new ArrayListSelectOperation<>(table, "fn"))
        {
            operation.setParameters(whereParameter);
        
            System.out.println("read as a collection");
            operation.execute();
            for (Student s: operation.readAll())
                System.out.println(s);
            
            System.out.println("read one row at a time");
            operation.execute();
            for (Student s = operation.readNext(); s != null; s = operation.readNext())
                System.out.println(s);
        }
    }
    
    
    void selectIn() throws SormulaException
    {
        ArrayList<Integer> idList = new ArrayList<>();
        idList.add(1234);
        idList.add(8888);
        
        System.out.println("select where id in = " + idList);
		try (ListSelectOperation<Student> operation = new ArrayListSelectOperation<>(table, "idin"))
		{
            operation.setTimings(true);
            printAll(operation.selectAll(idList));
            operation.logTimings();
		}
    }
    
    
    @SuppressWarnings("resource")
    void selectWhere2() throws SormulaException
    {
        System.out.println("select using SelectJohns class");
        printAll(new SelectJohns(table).selectAll());
    }
}


// where annotations may be used on operation class
@Where(name="fn2", fieldNames="firstName")
class SelectJohns extends ArrayListSelectOperation<Student>
{
    public SelectJohns(Table<Student> table) throws OperationException
    {
        super(table, "fn2");
        setParameters("John");
    }
}
