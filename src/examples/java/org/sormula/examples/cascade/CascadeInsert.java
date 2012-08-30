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
import java.util.ArrayList;
import java.util.GregorianCalendar;

import org.sormula.Database;
import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.examples.ExampleBase;


public class CascadeInsert extends ExampleBase
{
    Table<Student4> table;
    int enrolledId = 1001;
    
    
    public static void main(String[] args) throws Exception
    {
        new CascadeInsert();
    }
    
    
    public CascadeInsert() throws Exception
    {
        openDatabase();
        
        // create table
        String tableName = getSchemaPrefix() + "student4";
        dropTable(tableName);
        createTable("CREATE TABLE " + tableName + 
                "(id INTEGER NOT NULL PRIMARY KEY," +
                " firstname VARCHAR(30)," +
                " lastname VARCHAR(30)," +
                " graduationdate TIMESTAMP)" 
        );
        
        tableName = getSchemaPrefix() + "enrolled";
        dropTable(tableName);
        createTable("CREATE TABLE " + tableName + 
                "(enrolledid INTEGER NOT NULL PRIMARY KEY," +
                " studentid INTEGER NOT NULL," +
                " courseid INTEGER NOT NULL," +
                " semester INTEGER NOT NULL," +
                " yr INTEGER NOT NULL)"
        );
        
        // init
        Connection connection = getConnection();
        Database database = new Database(connection, getSchema());
        
        table = database.getTable(Student4.class);
        insertRows();
        
        // clean up
        closeDatabase();
    }
    
    
    void insertRows() throws SormulaException
    {
        ArrayList<Student4> list = new ArrayList<>();
        ArrayList<Enrolled> enrollment;
        Student4 student;
        
        student = new Student4();
        student.setId(9999);
        student.setFirstName("John");
        student.setLastName("Miller");
        list.add(student);
        
        // courses for student
        enrollment = new ArrayList<>();
        enrollment.add(enroll202(student));
        student.setEnrollment(enrollment);
        
        student = new Student4();
        student.setId(8888);
        student.setFirstName("John");
        student.setLastName("Smith");
        list.add(student);
        
        // courses for student
        enrollment = new ArrayList<>();
        enrollment.add(enroll202(student));
        enrollment.add(enroll601(student));
        student.setEnrollment(enrollment);
        
        student = new Student4();
        student.setId(7777);
        student.setFirstName("Rita");
        student.setLastName("Miller");
        student.setGraduationDate(new GregorianCalendar(2000, 11, 31).getTime());
        list.add(student);
        
        // inserts students and enrolled
        System.out.println(table.insertAll(list) + " rows inserted");
    }
    
    
    Enrolled enroll202(Student4 student)
    {
        Enrolled enrolled = new Enrolled();
        enrolled.setEnrolledId(enrolledId++);
        enrolled.setStudentId(student.getId());
        enrolled.setCourseId(202);
        enrolled.setYr(2011);
        enrolled.setSemester(1);
        return enrolled;
    }
    
    
    Enrolled enroll601(Student4 student)
    {
        Enrolled enrolled = new Enrolled();
        enrolled.setEnrolledId(enrolledId++);
        enrolled.setStudentId(student.getId());
        enrolled.setCourseId(601);
        enrolled.setYr(2012);
        enrolled.setSemester(2);
        return enrolled;
    }
}
