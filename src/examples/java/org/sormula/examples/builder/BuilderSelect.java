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

import java.util.ArrayList;

import org.sormula.Database;
import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.examples.ExampleBase;
import org.sormula.operation.ArrayListSelectOperation;
import org.sormula.operation.ListSelectOperation;


public class BuilderSelect extends ExampleBase
{
    Table<BuilderStudent> table;
    
    
    public static void main(String[] args) throws Exception
    {
        new BuilderInsert(); // create table and rows
        new BuilderSelect();
    }
    
    
    public BuilderSelect() throws Exception
    {
        // init
        openDatabase();

        try (Database database = Database.builder(getConnection()).schema(getSchema()).build())
        {
            table = Table.builder(database, BuilderStudent.class).build();
            selectRow();
            selectAllRows();
            selectWhere();
            selectIn();
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
        
        try (ListSelectOperation<BuilderStudent> operation = 
                ArrayListSelectOperation.builder(table).where("fn").parameters(whereParameter).build())
        {
            System.out.println("read as a collection");
            operation.execute();
            for (BuilderStudent s: operation.readAll())
                System.out.println(s);
            
            System.out.println("read one row at a time");
            operation.execute();
            for (BuilderStudent s = operation.readNext(); s != null; s = operation.readNext())
                System.out.println(s);
        }
    }
    
    
    void selectIn() throws SormulaException
    {
        ArrayList<Integer> idList = new ArrayList<>();
        idList.add(1234);
        idList.add(8888);
        
        System.out.println("select where id in = " + idList);
        try (ListSelectOperation<BuilderStudent> operation = 
                ArrayListSelectOperation.builder(table).where("idin").timings(true).build())
        {
            printAll(operation.selectAll(idList));
            operation.logTimings();
		}
    }
}
