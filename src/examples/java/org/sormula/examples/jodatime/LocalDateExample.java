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
package org.sormula.examples.jodatime;

import java.sql.Connection;
import java.util.ArrayList;

import org.joda.time.LocalDate;
import org.sormula.Database;
import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.examples.ExampleBase;


/**
 * Reads and writes {@link StudentLD} which contains a Joda Time {@link LocalDate}
 * using {@link LocalDateTranslator}.
 * 
 * @author Jeff Miller
 */
public class LocalDateExample extends ExampleBase
{
    Table<StudentLD> table;
    
    
    public static void main(String[] args) throws Exception
    {
        new LocalDateExample();
    }
    
    
    public LocalDateExample() throws Exception
    {
        openDatabase();
        
        // create table
        String tableName = getSchemaPrefix() + "studentLD";
        dropTable(tableName);
        createTable("CREATE TABLE " + tableName + 
                "(id INTEGER NOT NULL PRIMARY KEY," +
                " firstname VARCHAR(30)," +
                " lastname VARCHAR(30)," +
                " graduationdate TIMESTAMP)" 
        );
        
        // init
        Connection connection = getConnection();
        Database database = new Database(connection, getSchema());
        table = database.getTable(StudentLD.class);
        
        insertRow();
        insertRows();
        printAll(table.selectAll());
        
        // clean up
        closeDatabase();
    }
    
    
    void insertRow() throws SormulaException
    {
        StudentLD student = new StudentLD();
        student.setId(1234);
        student.setFirstName("Jeff");
        student.setLastName("Miller");
        student.setGraduationDate(new LocalDate());
        System.out.println(table.insert(student) + " row inserted");
        System.out.println(table.select(1234));
    }
    
    
    void insertRows() throws SormulaException
    {
        ArrayList<StudentLD> list = new ArrayList<>();
        StudentLD student;
        
        student = new StudentLD();
        student.setId(9999);
        student.setFirstName("John");
        student.setLastName("Miller");
        list.add(student);
        
        student = new StudentLD();
        student.setId(8888);
        student.setFirstName("John");
        student.setLastName("Smith");
        list.add(student);
        
        student = new StudentLD();
        student.setId(7777);
        student.setFirstName("Rita");
        student.setLastName("Miller");
        student.setGraduationDate(new LocalDate(2000, 12, 31));
        list.add(student);
        
        System.out.println(table.insertAll(list) + " rows inserted");
    }
}
