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
package org.sormula.tests.annotation;

import java.util.Arrays;
import java.util.List;

/**
 * Type for testing {@link Test3Translator}. Red, Green, Blue string values are stored in
 * database as R, G, B strings.
 * 
 * @author Jeff Miller
 */
public class Test3
{
    static List<String> names = Arrays.asList("", "Red", "Green", "Blue");
    static List<String> codes = Arrays.asList("-", "R", "G", "B");
    String value;


    public Test3(String value)
    {
        if (value.length() > 1)
        {
            // assume name supplied
            this.value = value;
        }
        else
        {
            // assume code supplied, convert to name
            this.value = names.get(codes.indexOf(value));
        }
    }

    public String getValue()
    {
        return value;
    }
    
    public String codeValue()
    {
        return codes.get(names.indexOf(value));
    }
}
