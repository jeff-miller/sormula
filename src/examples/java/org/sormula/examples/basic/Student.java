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
package org.sormula.examples.basic;

import java.util.Date;

import org.sormula.annotation.Where;
import org.sormula.annotation.WhereField;
import org.sormula.annotation.Wheres;


/**
 * Row class where table name is the same as class name and column names are the same class field names.
 * See {@link BasicInsert} for table definition.
 */
@Wheres({
    @Where(name="fn",   fieldNames="firstName"), // where firstname=?
    @Where(name="idin", whereFields=@WhereField(name="id", comparisonOperator="IN")) // where id in (?, ?, ...)
})
public class Student
{
    int id; // defaults to @Column(primaryKey=true)
    String firstName;
    String lastName;
    Date graduationDate;
    
    
    public int getId()
    {
        return id;
    }
    public void setId(int id)
    {
        this.id = id;
    }
    
    
    public String getFirstName()
    {
        return firstName;
    }
    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }
    
    
    public String getLastName()
    {
        return lastName;
    }
    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }
    
    
    public Date getGraduationDate()
    {
        return graduationDate;
    }
    public void setGraduationDate(Date graduationDate)
    {
        this.graduationDate = graduationDate;
    }
    
    
    @Override
    public String toString()
    {
        return id + " " + firstName + " " + lastName;
    }
}
