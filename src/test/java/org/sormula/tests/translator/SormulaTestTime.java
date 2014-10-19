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
 * Row class for {@link TimeTranslatorTest}.
 * @author Jeff Miller
 */
public class SormulaTestTime
{
    java.sql.Time testSqlTime1;
    java.sql.Time testSqlTime2;
    
    public java.sql.Time getTestSqlTime1()
    {
        return testSqlTime1;
    }
    public void setTestSqlTime1(java.sql.Time testSqlTime1)
    {
        this.testSqlTime1 = testSqlTime1;
    }
    public java.sql.Time getTestSqlTime2()
    {
        return testSqlTime2;
    }
    public void setTestSqlTime2(java.sql.Time testSqlTime2)
    {
        this.testSqlTime2 = testSqlTime2;
    }
}
