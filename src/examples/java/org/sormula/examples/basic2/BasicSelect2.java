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

import org.sormula.Database;
import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.examples.ExampleBase;


public class BasicSelect2 extends ExampleBase
{
    Table<Student2> table;
    
    
    public static void main(String[] args) throws Exception
    {
        new BasicInsert2(); // create table and rows
        new BasicSelect2();
    }
    
    
    public BasicSelect2() throws Exception
    {
        // init
        openDatabase();

        try (Database database = new Database(getConnection(), getSchema()))
        {
            table = database.getTable(Student2.class);
            
            selectActive(1234);
            selectActive(7777);
            selectActive(8888);
            selectActive(9999);
            selectActive(1111);
        }
        
        // clean up
        closeDatabase();
    }
    
    
    void selectActive(int id) throws SormulaException
    {
        System.out.print("select active where id = " + id);
        Student2 student2 = table.selectWhere("activeById", id);
        if (student2 != null) 
            System.out.println(" found " + student2);
        else 
            System.out.println(" not found");
    }
}
