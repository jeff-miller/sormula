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
package org.sormula.tests.column;

import org.sormula.annotation.Column;


/**
 * Row class for testing some {@link Column} attributes.
 * 
 * @author Jeff Miller
 */
public class ColumnTestRow
{
    public static int READ_ONLY_VALUE = 999;
    
    @Column(primaryKey=true)
    int id;
    
    @Column(readOnly=true)
    int readOnlyTest;
    
    String description;
    
    
    public ColumnTestRow()
    {
    }

    
    public ColumnTestRow(int id, String description)
    {
        this.id = id;
        this.description = description;
    }
    
    
    public int getId()
    {
        return id;
    }
    public void setId(int id)
    {
        this.id = id;
    }


    public int getReadOnlyTest()
    {
        return readOnlyTest;
    }
    public void setReadOnlyTest(int readOnlyTest)
    {
        this.readOnlyTest = readOnlyTest;
    }


    public String getDescription()
    {
        return description;
    }
    public void setDescription(String description)
    {
        this.description = description;
    }
}
