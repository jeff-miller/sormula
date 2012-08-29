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
package org.sormula.examples.cascade;

import java.util.Date;
import java.util.List;

import org.sormula.annotation.Column;
import org.sormula.annotation.cascade.OneToManyCascade;
import org.sormula.annotation.cascade.SelectCascade;


/**
 * Row class for cascade example. Student4 has one to many relationship with Enrolled class.
 * See {@link CascadeInsert} for table definition.
 */
public class Student4
{
    @Column(primaryKey=true)
    int id;
    String firstName;
    String lastName;
    Date graduationDate;
    
    @OneToManyCascade(targetClass=Enrolled.class, // targetClass is optional in v1.9 
            selects=@SelectCascade(sourceParameterFieldNames="id", targetWhereName="studentSearch"))
    List<Enrolled> enrollment;
    
    
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
    
    
    public List<Enrolled> getEnrollment()
    {
        return enrollment;
    }
    public void setEnrollment(List<Enrolled> enrollment)
    {
        this.enrollment = enrollment;
    }
    
    
    @Override
    public String toString()
    {
        return id + " " + firstName + " " + lastName;
    }
}
