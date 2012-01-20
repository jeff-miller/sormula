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
package org.sormula.examples.jodatime;

import org.joda.time.LocalDate;
import org.sormula.annotation.ExplicitType;


/**
 * Row class that uses Joda Time {@link LocalDate}.
 */
@ExplicitType(type=LocalDate.class, translator=LocalDateTranslator.class)
public class StudentLD
{
    int id;
    String firstName;
    String lastName;
    
    //@ImplicitType(translator=LocalDateTranslator.class) // alternative to @ExplicitType
    LocalDate graduationDate;
    
    
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
    
    
    public LocalDate getGraduationDate()
    {
        return graduationDate;
    }
    public void setGraduationDate(LocalDate graduationDate)
    {
        this.graduationDate = graduationDate;
    }
    
    
    @Override
    public String toString()
    {
        return id + " " + firstName + " " + lastName + " " + graduationDate;
    }
}
