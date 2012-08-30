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
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

import org.sormula.Database;
import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.examples.ExampleBase;


/**
 * Same as InsertExample1 but does not always use default names. See {@link Student3}
 * for details about name differences.
 */
public class ComplexInsert extends ExampleBase
{
    Table<Student3> table;
    
    
    public static void main(String[] args) throws Exception
    {
        new ComplexInsert();
    }
    
    
    public ComplexInsert() throws Exception
    {
        openDatabase();
        
        // create table
        String tableName = getSchemaPrefix() + "studentthree";
        dropTable(tableName);
        createTable("CREATE TABLE " + tableName + 
                "(student_id INTEGER NOT NULL PRIMARY KEY," +
                " fname VARCHAR(30)," +
                " lname VARCHAR(30)," +
                " graduationdate TIMESTAMP," +
                " ssn INTEGER NOT NULL)"
        );
        
        // init
        Connection connection = getConnection();
        Database database = new Database(connection, getSchema());
        table = database.getTable(Student3.class);
        
        insertRow();
        insertRows();
        
        // clean up
        closeDatabase();
    }
    
    
    void insertRow() throws SormulaException
    {
        Student3 student = new Student3();
        student.setId(1234);
        student.setFirstName("Jeff");
        student.setLastName("Miller");
        student.setGraduationDate(new Date(System.currentTimeMillis()));
        System.out.println(table.insert(student) + " row inserted");
    }
    
    
    void insertRows() throws SormulaException
    {
        ArrayList<Student3> list = new ArrayList<>();
        Student3 student;
        
        student = new Student3();
        student.setId(9999);
        student.setFirstName("John");
        student.setLastName("Miller");
        list.add(student);
        
        student = new Student3();
        student.setId(8888);
        student.setFirstName("John");
        student.setLastName("Smith");
        list.add(student);
        
        student = new Student3();
        student.setId(7777);
        student.setFirstName("Rita");
        student.setLastName("Miller");
        student.setGraduationDate(new GregorianCalendar(2000, 11, 31).getTime());
        list.add(student);
        
        System.out.println(table.insertAll(list) + " rows inserted");
    }
}
