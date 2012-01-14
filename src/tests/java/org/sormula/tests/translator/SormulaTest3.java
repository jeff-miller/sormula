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
package org.sormula.tests.translator;

import org.sormula.annotation.Column;
import org.sormula.annotation.UnusedColumn;
import org.sormula.annotation.UnusedColumns;


/**
 * Row class for {@linkplain UnusedColumnTest}.
 * 
 * @author Jeff Miller
 */
@UnusedColumns({    
    @UnusedColumn(name="unusedInt", value="123"),
    @UnusedColumn(name="unusedString", value="'unused'")
})
public class SormulaTest3
{
    @Column(primaryKey=true)
    int testInteger;

    public int getTestInteger()
    {
        return testInteger;
    }
    public void setTestInteger(int testInteger)
    {
        this.testInteger = testInteger;
    }
}
