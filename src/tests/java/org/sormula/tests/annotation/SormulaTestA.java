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
package org.sormula.tests.annotation;


/**
 * Row class for {@link InsertTest} and {@link SelectTest}.
 * 
 * @author Jeff Miller
 */
public class SormulaTestA
{
    int id;
    int type;
    String description;
    Test1 test1;
    Test2 test2;
    Test3 test3;
    
    
    public SormulaTestA()
    {
    }

    
    public SormulaTestA(int id, int type, String description)
    {
        this.id = id;
        this.type = type;
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
    
    
    public String getDescription()
    {
        return description;
    }
    public void setDescription(String description)
    {
        this.description = description;
    }


    public int getType()
    {
        return type;
    }
    public void setType(int type)
    {
        this.type = type;
    }


    public Test1 getTest1()
    {
        return test1;
    }
    public void setTest1(Test1 type1)
    {
        this.test1 = type1;
    }


    public Test2 getTest2()
    {
        return test2;
    }
    public void setTest2(Test2 test2)
    {
        this.test2 = test2;
    }


    public Test3 getTest3()
    {
        return test3;
    }
    public void setTest3(Test3 test3)
    {
        this.test3 = test3;
    }


    @Override
    public int hashCode()
    {
        return id;
    }


    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof SormulaTestA)
        {
            SormulaTestA other = (SormulaTestA) obj;
            return id == other.id;
        }
        
        return false;
    }
}
