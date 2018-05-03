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
package org.sormula.examples.pagination;

import java.util.ArrayList;

import org.sormula.Database;
import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.examples.ExampleBase;


/**
 * Inserts {@link Book} rows into database for use by related selector examples.
 * 
 * @author Jeff Miller
 */
public class PaginationInsert extends ExampleBase
{
    Table<Book> table;
    
    
    public static void main(String[] args) throws Exception
    {
        new PaginationInsert();
    }
    
    
    public PaginationInsert() throws Exception
    {
        openDatabase();
        
        // create table
        String tableName = getSchemaPrefix() + "Book";
        dropTable(tableName);
        createTable("CREATE TABLE " + tableName + 
                "(id INTEGER NOT NULL PRIMARY KEY," +
                " title VARCHAR(100)," +
                " author VARCHAR(100))"
        );
        
        try (Database database = new Database(getConnection(), getSchema()))
        {
            table = database.getTable(Book.class);
            insertRows();
        }
        
        // clean up
        closeDatabase();
    }
    
    
    void insertRows() throws SormulaException
    {
        // https://stackoverflow.com/questions/1711/what-is-the-single-most-influential-book-every-programmer-should-read?answertab=votes#tab-top
        ArrayList<Book> list = new ArrayList<>();
        int id = 1;
        list.add(new Book(id++, "Code Complete (2nd edition)", "Steve McConnell"));
        list.add(new Book(id++, "The Pragmatic Programmer", ""));
        list.add(new Book(id++, "Structure and Interpretation of Computer Programs", ""));
        list.add(new Book(id++, "The C Programming Language", "Kernighan and Ritchie"));
        list.add(new Book(id++, "Introduction to Algorithms", "Cormen, Leiserson, Rivest & Stein"));
        list.add(new Book(id++, "Design Patterns", "the Gang of Four"));
        list.add(new Book(id++, "Refactoring: Improving the Design of Existing Code", ""));
        list.add(new Book(id++, "The Mythical Man Month", ""));
        list.add(new Book(id++, "The Art of Computer Programming", "Donald Knuth"));
        list.add(new Book(id++, "Compilers: Principles, Techniques and Tools", "Alfred V. Aho, Ravi Sethi and Jeffrey D. Ullman"));
        list.add(new Book(id++, "Gödel, Escher, Bach", "Douglas Hofstadter"));
        list.add(new Book(id++, "Clean Code: A Handbook of Agile Software Craftsmanship", "Robert C. Martin"));
        list.add(new Book(id++, "Effective C++", ""));
        list.add(new Book(id++, "More Effective C++", ""));
        list.add(new Book(id++, "CODE", "Charles Petzold"));
        list.add(new Book(id++, "Programming Pearls", "Jon Bentley"));
        list.add(new Book(id++, "Working Effectively with Legacy Code", "Michael C. Feathers"));
        list.add(new Book(id++, "Peopleware", "Demarco and Lister"));
        list.add(new Book(id++, "Coders at Work", "Peter Seibel"));
        list.add(new Book(id++, "Surely You're Joking, Mr. Feynman!", ""));
        list.add(new Book(id++, "Effective Java 2nd edition", ""));
        list.add(new Book(id++, "Patterns of Enterprise Application Architecture", "Martin Fowler"));
        list.add(new Book(id++, "The Little Schemer", ""));
        list.add(new Book(id++, "The Seasoned Schemer", ""));
        list.add(new Book(id++, "Why's (Poignant) Guide to Ruby", ""));
        list.add(new Book(id++, "The Inmates Are Running The Asylum", ""));
        list.add(new Book(id++, "The Art of Unix Programming", ""));
        list.add(new Book(id++, "Test-Driven Development: By Example", "Kent Beck"));
        list.add(new Book(id++, "Practices of an Agile Developer", ""));
        list.add(new Book(id++, "Don't Make Me Think", ""));
        list.add(new Book(id++, "Agile Software Development, Principles, Patterns, and Practices", "Robert C. Martin"));
        list.add(new Book(id++, "Domain Driven Designs", "Eric Evans"));
        list.add(new Book(id++, "The Design of Everyday Things", "Donald Norman"));        
        System.out.println(table.insertAll(list) + " rows inserted");
    }
}
