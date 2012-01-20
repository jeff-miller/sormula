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
package org.sormula.examples.complex;

import java.util.Date;

import org.sormula.annotation.Column;
import org.sormula.annotation.Row;
import org.sormula.annotation.Transient;
import org.sormula.annotation.UnusedColumn;
import org.sormula.annotation.UnusedColumns;
import org.sormula.annotation.Where;


/**
 * Row class where table name is different from class name and some column names are different from 
 * the corresponding class variables. Field, name, is transient and is not used in database
 * input/output. See {@link ComplexInsert} for table definition.
 */
@Row(tableName="studentthree")
@Where(name="fn", fieldNames="firstName")
@UnusedColumns(@UnusedColumn(name="ssn", value="0"))
public class Student3
{
    @Column(name="student_id", primaryKey=true)
    int id;
    
    @Column(name="fname")
    String firstName;
    
    @Column(name="lname")
    String lastName;
    
    Date graduationDate;
    
    @Transient
    String name;
    
    
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
        
        // contrived transient field
        if (firstName != null && lastName != null) name = firstName + " " + lastName;
    }
    
    
    public String getLastName()
    {
        return lastName;
    }
    public void setLastName(String lastName)
    {
        this.lastName = lastName;
        
        // contrived transient field
        if (firstName != null && lastName != null) name = firstName + " " + lastName;
    }
    
    
    public Date getGraduationDate()
    {
        return graduationDate;
    }
    public void setGraduationDate(Date graduationDate)
    {
        this.graduationDate = graduationDate;
    }
    
    
    public String getName()
    {
        return name;
    }
    
    
    @Override
    public String toString()
    {
        return id + " " + firstName + " " + lastName;
    }
}
