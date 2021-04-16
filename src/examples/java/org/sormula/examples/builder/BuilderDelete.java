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
package org.sormula.examples.builder;

import java.util.List;

import org.sormula.Database;
import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.examples.ExampleBase;


public class BuilderDelete extends ExampleBase
{
    Table<BuilderStudent> table;
    
    
    public static void main(String[] args) throws Exception
    {
        new BuilderInsert(); // create table and rows
        new BuilderDelete();
    }
    
    
    public BuilderDelete() throws Exception
    {
        // init
        openDatabase();
        
        try (Database database = Database.builder(getConnection()).schema(getSchema()).build())
        {
            table = Table.builder(database, BuilderStudent.class).build();
            deleteByPrimaryKey();
            deleteRow();
            deleteRows();
        }
        
        // clean up
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
        BuilderStudent student = table.select(id);
        table.delete(student);
        printAll(table.selectAll());
    }
    
    
    void deleteRows() throws SormulaException
    {
        System.out.println("table.deleteAll()");
        List<BuilderStudent> list = table.selectAll();
        table.deleteAll(list);
        printAll(table.selectAll());
    }
}
