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
package org.sormula.examples.identity;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

import org.sormula.Database;
import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.examples.ExampleBase;


public class IdentityInsert extends ExampleBase
{
    Table<Student> table;
    
    
    public static void main(String[] args) throws Exception
    {
        new IdentityInsert();
    }
    
    
    public IdentityInsert() throws Exception
    {
        openDatabase();
        
        // create table
        String tableName = getSchemaPrefix() + "student";
        dropTable(tableName);
        createTable("CREATE TABLE " + tableName + 
                "(id INTEGER GENERATED ALWAYS AS IDENTITY(START WITH 1001) PRIMARY KEY," +
                " firstname VARCHAR(30)," +
                " lastname VARCHAR(30)," +
                " graduationdate TIMESTAMP)" 
        );
        
        try (Database database = new Database(getConnection(), getSchema()))
        {
            table = database.getTable(Student.class);
            
            insertRow();
            insertRows();
        }
        
        // clean up
        closeDatabase();
    }
    
    
    void insertRow() throws SormulaException
    {
        Student student = new Student(); // id initialized to zero
        student.setFirstName("Jeff");
        student.setLastName("Miller");
        student.setGraduationDate(new Date(System.currentTimeMillis()));
        table.insert(student); // sormula invokes student.setId() with next generated value
        System.out.println("row inserted with id=" + student.getId());
    }
    
    
    void insertRows() throws SormulaException
    {
        ArrayList<Student> list = new ArrayList<>();
        Student student;
        
        student = new Student();
        student.setId(9999);
        student.setFirstName("John");
        student.setLastName("Miller");
        list.add(student);
        
        student = new Student();
        student.setId(8888);
        student.setFirstName("John");
        student.setLastName("Smith");
        list.add(student);
        
        student = new Student();
        student.setId(7777);
        student.setFirstName("Rita");
        student.setLastName("Miller");
        student.setGraduationDate(new GregorianCalendar(2000, 11, 31).getTime());
        list.add(student);
        
        // sormula invokes student.setId() with next generated value for each inserted row
        table.insertAll(list);
        
        System.out.print("rows inserted with ids=");
        for (Student s: list) System.out.print(s.getId() + " ");
        System.out.println();
    }
}
