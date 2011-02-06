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
package org.sormula.examples.example4;

import org.sormula.annotation.Column;
import org.sormula.annotation.Where;


/**
 * Row class for cascade example. Enrolled is many side of 1 to many relationship with Student4.
 */
@Where(name="studentSearch", fieldNames="studentId")
public class Enrolled
{
    @Column(primaryKey=true)
    int enrolledId;
    int studentId;
    int courseId;
    int year;
    int semester;
    
    
    public int getEnrolledId()
    {
        return enrolledId;
    }
    public void setEnrolledId(int enrolledId)
    {
        this.enrolledId = enrolledId;
    }


    public int getStudentId()
    {
        return studentId;
    }
    public void setStudentId(int studentId)
    {
        this.studentId = studentId;
    }


    public int getCourseId()
    {
        return courseId;
    }
    public void setCourseId(int courseId)
    {
        this.courseId = courseId;
    }


    public int getYear()
    {
        return year;
    }
    public void setYear(int year)
    {
        this.year = year;
    }


    public int getSemester()
    {
        return semester;
    }
    public void setSemester(int semester)
    {
        this.semester = semester;
    }


    @Override
    public String toString()
    {
        return courseId + " " + year + " " + semester;
    }
}
