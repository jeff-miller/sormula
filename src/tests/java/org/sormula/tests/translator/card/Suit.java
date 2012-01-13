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
package org.sormula.tests.translator.card;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.sormula.annotation.Type;


@Type(translator=SuitTranslator.class) 
public class Suit 
{
    static List<String> suit = new ArrayList<String>(
            Arrays.asList("", "Hearts", "Diamonds", "Clubs", "Spades"));
    int id;
    
    
    public Suit(int id)
    {
        this.id = id;
    }
    public Suit(String description)
    {
        id = suit.indexOf(description);
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
        return suit.get(id);
    }
}

