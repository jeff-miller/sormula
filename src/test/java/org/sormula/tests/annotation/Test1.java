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

import java.util.Arrays;
import java.util.List;

/**
 * Type for testing {@link Test1Translator}. Number string values are stored in
 * database as their int equivalent.
 * 
 * @author Jeff Miller
 */
public class Test1
{
    static List<String> names = Arrays.asList("zero", "one", "two", "three");
    String value;


    public Test1(String value)
    {
        this.value = value;
    }
    
    public Test1(int value)
    {
        this.value = names.get(value);
    }

    public String getValue()
    {
        return value;
    }
    
    public int intValue()
    {
        return names.indexOf(value);
    }
}
