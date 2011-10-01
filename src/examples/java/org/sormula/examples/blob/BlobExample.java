/* sormula - Simple object relational mapping
 * Copyright (C) 2011 Jeff Miller
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
package org.sormula.examples.blob;

import java.sql.Blob;
import java.sql.Connection;

import org.sormula.Database;
import org.sormula.SormulaException;
import org.sormula.examples.ExampleBase;


/**
 * Inserts a row into a table and then selects the row from a table where {@link Widget} 
 * field is stored as a blob. {@link WidgetColumnTranslator1} and {@link WidgetColumnTranslator2}
 * are custom column translators that converts {@link Widget} objects to/from {@link Blob}.
 */
public class BlobExample extends ExampleBase
{
    Database database;
    
        
    public static void main(String[] args) throws Exception
    {
        new BlobExample();
    }
    
    
    public BlobExample() throws Exception
    {
        openDatabase();
        
        // create table
        String tableName = getSchemaPrefix() + "blobexample";
        dropTable(tableName);
        createTable("CREATE TABLE " + tableName + 
                "(id INTEGER NOT NULL PRIMARY KEY," +
                " widget BLOB)"
        );
        
        // init
        Connection connection = getConnection();
        database = new Database(connection, getSchema());
        
        insert1();
        select1();
        insert2();
        select2();
        
        // clean up
        closeDatabase();
    }
    
    
    void insert1() throws SormulaException
    {
        SomeRow1 someRow = new SomeRow1();
        someRow.setId(1);
        Widget widget = new Widget(111, "test widget 111");
        someRow.setWidget(widget);
        
        if (database.getTable(SomeRow1.class).insert(someRow) == 1)
            System.out.println("inserted row with blob: test=" + widget.getTest() + " something=" + widget.getSomething());
        else
            System.out.println("row was not inserted");
    }
    
    
    void select1() throws SormulaException
    {
        SomeRow1 someRow = database.getTable(SomeRow1.class).select(1);
        
        if (someRow != null)
        {
            Widget widget = someRow.getWidget();
            System.out.println("seleted  row with blob: test=" + widget.getTest() + " something=" + widget.getSomething());
        }
        else
        {
            System.out.println("row was not selected");
        }
    }
    
    
    void insert2() throws SormulaException
    {
        SomeRow2 someRow = new SomeRow2();
        someRow.setId(2);
        Widget widget = new Widget(222, "test widget 222");
        someRow.setWidget(widget);
        
        if (database.getTable(SomeRow2.class).insert(someRow) == 1)
            System.out.println("inserted row with blob: test=" + widget.getTest() + " something=" + widget.getSomething());
        else
            System.out.println("row was not inserted");
    }
    
    
    void select2() throws SormulaException
    {
        SomeRow2 someRow = database.getTable(SomeRow2.class).select(2);
        
        if (someRow != null)
        {
            Widget widget = someRow.getWidget();
            System.out.println("seleted  row with blob: test=" + widget.getTest() + " something=" + widget.getSomething());
        }
        else
        {
            System.out.println("row was not selected");
        }
    }
}
