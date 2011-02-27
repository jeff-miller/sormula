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
import org.sormula.translator.standard.BooleanYNColumnTranslator;


/**
 * Row class for {@linkplain NameTranslatorTestA}.
 * @author Jeff Miller
 */
public class SormulaTest2A
{
    double testDouble;
    java.util.Date testDate;
    String testString;
    
    @Column(translator=BooleanYNColumnTranslator.class)
    boolean testBooleanYesNo;
    
    @Column(name="altname") // test explicit name
    int testInteger;
    
    public boolean isTestBooleanYesNo()
    {
        return testBooleanYesNo;
    }
    public void setTestBooleanYesNo(boolean testBooleanYesNo)
    {
        this.testBooleanYesNo = testBooleanYesNo;
    }
    public double getTestDouble()
    {
        return testDouble;
    }
    public void setTestDouble(double testDouble)
    {
        this.testDouble = testDouble;
    }
    public int getTestInteger()
    {
        return testInteger;
    }
    public void setTestInteger(int testInteger)
    {
        this.testInteger = testInteger;
    }
    public java.util.Date getTestDate()
    {
        return testDate;
    }
    public void setTestDate(java.util.Date testDate)
    {
        this.testDate = testDate;
    }
    public String getTestString()
    {
        return testString;
    }
    public void setTestString(String testString)
    {
        this.testString = testString;
    }
}
