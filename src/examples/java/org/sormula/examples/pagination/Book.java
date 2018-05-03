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

import org.sormula.annotation.Column;
import org.sormula.annotation.OrderBy;
import org.sormula.annotation.Where;
import org.sormula.annotation.WhereField;


@Where(name="top", whereFields={@WhereField(name="id", comparisonOperator="<=")})
@OrderBy(name="titleOrder", ascending="title")
public class Book
{
    @Column(primaryKey=true)
    int id;
    String title;
    String author;
    
    
    public Book()
    {
    }

    
    public Book(int id, String title, String author)
    {
        this.id = id;
        this.title = title;
        this.author = author;
    }
    
    
    public int getId()
    {
        return id;
    }
    public void setId(int id)
    {
        this.id = id;
    }
    
    
    public String getTitle()
    {
        return title;
    }
    public void setTitle(String title)
    {
        this.title = title;
    }
    
    
    public String getAuthor()
    {
        return author;
    }
    public void setAuthor(String author)
    {
        this.author = author;
    }
    
    
    @Override
    public String toString()
    {
        return String.format("%2d", id) + "   " + title + "   " + author;
    }
}
