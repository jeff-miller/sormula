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
package org.sormula.tests.translator;



/**
 * Row class for {@link LongTranslatorTest}.
 * @author Jeff Miller
 */
public class SormulaTestLong
{
    long testLong1;
    Long testLong2;
    
    public long getTestLong1()
    {
        return testLong1;
    }
    public void setTestLong1(long testLong1)
    {
        this.testLong1 = testLong1;
    }
    public Long getTestLong2()
    {
        return testLong2;
    }
    public void setTestLong2(Long testLong2)
    {
        this.testLong2 = testLong2;
    }
}
