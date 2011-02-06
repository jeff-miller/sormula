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
package org.sormula.tests.operation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sormula.SormulaException;
import org.sormula.tests.DatabaseTest;


/**
 * Base class for all operations tests.
 * 
 * @author Jeff Miller
 */
public class OperationTest<R> extends DatabaseTest<R>
{
    List<R> all;
    
    
    public void selectTestRows() throws SormulaException
    {
        all = getTable().selectAll();
    }
    
    
    public R getRandom()
    {
        return all.get(randomInt(all.size()));
    }
    
    
    public Set<R> getRandomSet()
    {
        int size = 10;
        Set<R> set = new HashSet<R>(size * 2);
        
        // choose random set
        for (int i = 0; i < size; ++i)
        {
            set.add(getRandom());
        }
        
        return set;
    }
}
