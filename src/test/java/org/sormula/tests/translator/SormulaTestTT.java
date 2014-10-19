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

import org.sormula.annotation.EnumType;
import org.sormula.annotation.ExplicitType;
import org.sormula.annotation.ImplicitType;
import org.sormula.translator.standard.BooleanYNTranslator;


/**
 * Row class for {@link TypeTranslatorTest}.
 * @author Jeff Miller
 */
@ExplicitType(type=Boolean.class, translator=BooleanYNTranslator.class) // for testBooleanYesNo2
public class SormulaTestTT
{
    java.util.Date testDate;
    
    @ImplicitType(translator=BooleanYNTranslator.class)
    boolean testBooleanYesNo1;
    
    Boolean testBooleanYesNo2;
    
    Boolean testBooleanYesNo3; // for null test
    
    int testInteger;
    String testString;
    
    @EnumType(translator=CustomEnumTranslator.class)
    EnumField testEnum; // test custom enum translator
    
    
    public boolean isTestBooleanYesNo1()
    {
        return testBooleanYesNo1;
    }
    public void setTestBooleanYesNo1(boolean testBooleanYesNo1)
    {
        this.testBooleanYesNo1 = testBooleanYesNo1;
    }
    public Boolean getTestBooleanYesNo2()
    {
        return testBooleanYesNo2;
    }
    public void setTestBooleanYesNo2(Boolean testBooleanYesNo2)
    {
        this.testBooleanYesNo2 = testBooleanYesNo2;
    }
    public Boolean getTestBooleanYesNo3()
    {
        return testBooleanYesNo3;
    }
    public void setTestBooleanYesNo3(Boolean testBooleanYesNo3)
    {
        this.testBooleanYesNo3 = testBooleanYesNo3;
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
    public EnumField getTestEnum()
    {
        return testEnum;
    }
    public void setTestEnum(EnumField testEnum)
    {
        this.testEnum = testEnum;
    }
}
