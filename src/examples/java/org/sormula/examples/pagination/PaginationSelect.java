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

import java.util.List;

import org.sormula.Database;
import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.examples.ExampleBase;
import org.sormula.selector.PaginatedListSelector;


public class PaginationSelect extends ExampleBase
{
    Table<Book> table;
    static int pageSize = 6; // rows per page, small for simple command line example
    
    
    public static void main(String[] args) throws Exception
    {
        new PaginationInsert(); // create table and rows
        new PaginationSelect();
    }
    
    
    public PaginationSelect() throws Exception
    {
        // init
        openDatabase();

        try (Database database = new Database(getConnection(), getSchema()))
        {
            table = database.getTable(Book.class);
            selectAsPages();
        }
        
        // clean up
        closeDatabase();
    }
    
    
    void selectAsPages() throws SormulaException
    {
        PaginatedListSelector<Book> selector = new PaginatedListSelector<>(pageSize, table);
        selector.setWhereConditionName("top15");
        selector.setOrderByName("titleOrder");
        selector.execute();
        
        int pageNumber = 1;
        while (true)
        {
            selector.setPageNumber(pageNumber);
            List<Book> pageBooks = selector.selectPage();
            if (pageBooks.isEmpty()) break;
            
            System.out.println("Page " + pageNumber);
            printAll(pageBooks);
            System.out.println();
            ++pageNumber;
        }
    }
}
