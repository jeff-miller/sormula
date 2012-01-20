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
package org.sormula.examples.name;

import java.sql.Connection;
import java.util.GregorianCalendar;
import java.util.List;

import org.sormula.Database;
import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.examples.ExampleBase;
import org.sormula.translator.StandardNameTranslator;


/**
 * Same as UpdateExample1 but uses {@link StandardNameTranslator} which derives
 * column names from row class names with underscores between words. See {@link Student2}.
 */
public class NameUpdate extends ExampleBase
{
    Table<Student2> table;
    
    
    public static void main(String[] args) throws Exception
    {
        new NameInsert(); // create table and rows
        new NameUpdate();
    }
    
    
    public NameUpdate() throws Exception
    {
        // init
        openDatabase();
        Connection connection = getConnection();
        Database database = new Database(connection, getSchema());
        table = database.getTable(Student2.class);
        
        updateRow();
        updateRows();
        printAll(table.selectAll());
        
        // clean up
        closeDatabase();
    }
    
    
    void updateRow() throws SormulaException
    {
        int id = 9999;
        System.out.println("table.update() " + id);
        Student2 student = table.select(id);
        student.setGraduationDate(new GregorianCalendar(2010, 0, 1).getTime());
        table.update(student);
    }
    
    
    void updateRows() throws SormulaException
    {
        String newLastName = "Jones";
        System.out.println("table.updateAll() set last name = " + newLastName);
        List<Student2> list = table.selectAll();
        
        for (Student2 s: list)
            s.setLastName(newLastName);
        
        table.updateAll(list);
    }
}
