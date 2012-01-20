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
package org.sormula.tests.translator.card;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Rank 
{
    static List<String> rank = new ArrayList<String>(
            Arrays.asList("", "A", "2", "3", "4", "5", "6", "7", "8", "9", "J", "Q", "K"));
    int value;
    
    
    public Rank(int value)
    {
        this.value = value;
    }
    public Rank(String description)
    {
        value = rank.indexOf(description);
    }

    public int getValue()
    {
        return value;
    }

    public void setValue(int value)
    {
        this.value = value;
    }

    public String getDescription()
    {
        return rank.get(value);
    }
}
