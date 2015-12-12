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
package org.sormula.examples.basic2;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

import org.sormula.Database;
import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.examples.ExampleBase;


public class BasicInsert2 extends ExampleBase
{
    Table<Student2> table;
    
    
    public static void main(String[] args) throws Exception
    {
        new BasicInsert2();
    }
    
    
    public BasicInsert2() throws Exception
    {
        openDatabase();
        
        // create table
        String tableName = getSchemaPrefix() + "student2";
        dropTable(tableName);
        createTable("CREATE TABLE " + tableName + 
                "(id INTEGER NOT NULL PRIMARY KEY," +
                " firstname VARCHAR(30)," +
                " lastname VARCHAR(30)," +
                " graduationdate TIMESTAMP," +
                " active CHAR(1))"
        );
        
        try (Database database = new Database(getConnection(), getSchema()))
        {
            table = database.getTable(Student2.class);
            
            insertRow();
            insertRows();
        }
        
        // clean up
        closeDatabase();
    }
    
    
    void insertRow() throws SormulaException
    {
        Student2 student2 = new Student2();
        student2.setId(1234);
        student2.setFirstName("Jeff");
        student2.setLastName("Miller");
        student2.setGraduationDate(new Date(System.currentTimeMillis()));
        student2.setActive("N");
        System.out.println(table.insert(student2) + " row inserted");
    }
    
    
    void insertRows() throws SormulaException
    {
        ArrayList<Student2> list = new ArrayList<>();
        Student2 student2;
        
        student2 = new Student2();
        student2.setId(9999);
        student2.setFirstName("John");
        student2.setLastName("Miller");
        student2.setActive("Y");
        list.add(student2);
        
        student2 = new Student2();
        student2.setId(8888);
        student2.setFirstName("John");
        student2.setLastName("Smith");
        student2.setActive("Y");
        list.add(student2);
        
        student2 = new Student2();
        student2.setId(7777);
        student2.setFirstName("Rita");
        student2.setLastName("Miller");
        student2.setGraduationDate(new GregorianCalendar(2000, 11, 31).getTime());
        student2.setActive("N");
        list.add(student2);
        
        System.out.println(table.insertAll(list) + " rows inserted");
    }
}
