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

import java.time.LocalDate;
import java.util.GregorianCalendar;

import org.sormula.annotation.Column;
import org.sormula.annotation.EnumType;
import org.sormula.annotation.Transient;
import org.sormula.annotation.Where;
import org.sormula.translator.standard.BooleanYNColumnTranslator;


/**
 * Row class for {@link ColumnTranslatorTest}.
 * @author Jeff Miller
 */
@Where(name="forTestSting1", fieldNames="testString1")
public class SormulaTest1
{
    boolean testBoolean1;
    Boolean testBoolean2;
    
    @Column(translator=BooleanYNColumnTranslator.class) // equivalent to @ImplicitType(translator=BooleanYNTranslator)
    boolean testBooleanYN1;
    @Column(translator=BooleanYNColumnTranslator.class)
    Boolean testBooleanYN2;
    
    byte testByte1;
    Byte testByte2;
    double testDouble1;
    Double testDouble2;
    float testFloat1;
    Float testFloat2;
    int testInteger1;
    Integer testInteger2;
    short testShort1;
    Short testShort2;
    java.util.Date testDate;
    java.util.Date testNullDate;
    java.sql.Date testSqlDate;
    java.sql.Timestamp testSqlTimestamp;
    GregorianCalendar testGc;
    LocalDate testLocalDate;
    String testString1;
    EnumField testEnum1;
    
    @EnumType(defaultEnumName="Bad")
    EnumField testEnum2;
    
    @Column(name="ts2") // test explicit name
    String testString2;
    
    // no database i/o for this member
    @Transient
    String testTransient;
    
    // test that static members are not mapped
    static int staticMember;
    
    
    public boolean isTestBoolean1()
    {
        return testBoolean1;
    }
    public void setTestBoolean1(boolean testBoolean1)
    {
        this.testBoolean1 = testBoolean1;
    }
    public Boolean getTestBoolean2()
    {
        return testBoolean2;
    }
    public void setTestBoolean2(Boolean testBoolean2)
    {
        this.testBoolean2 = testBoolean2;
    }
    public boolean isTestBooleanYN1()
    {
        return testBooleanYN1;
    }
    public void setTestBooleanYN1(boolean testBooleanYN1)
    {
        this.testBooleanYN1 = testBooleanYN1;
    }
    public Boolean getTestBooleanYN2()
    {
        return testBooleanYN2;
    }
    public void setTestBooleanYN2(Boolean testBooleanYN2)
    {
        this.testBooleanYN2 = testBooleanYN2;
    }
    public byte getTestByte1()
    {
        return testByte1;
    }
    public void setTestByte1(byte testByte1)
    {
        this.testByte1 = testByte1;
    }
    public Byte getTestByte2()
    {
        return testByte2;
    }
    public void setTestByte2(Byte testByte2)
    {
        this.testByte2 = testByte2;
    }
    public double getTestDouble1()
    {
        return testDouble1;
    }
    public void setTestDouble1(double testDouble1)
    {
        this.testDouble1 = testDouble1;
    }
    public Double getTestDouble2()
    {
        return testDouble2;
    }
    public void setTestDouble2(Double testDouble2)
    {
        this.testDouble2 = testDouble2;
    }
    public float getTestFloat1()
    {
        return testFloat1;
    }
    public void setTestFloat1(float testFloat1)
    {
        this.testFloat1 = testFloat1;
    }
    public Float getTestFloat2()
    {
        return testFloat2;
    }
    public void setTestFloat2(Float testFloat2)
    {
        this.testFloat2 = testFloat2;
    }
    public int getTestInteger1()
    {
        return testInteger1;
    }
    public void setTestInteger1(int testInteger1)
    {
        this.testInteger1 = testInteger1;
    }
    public Integer getTestInteger2()
    {
        return testInteger2;
    }
    public void setTestInteger2(Integer testInteger2)
    {
        this.testInteger2 = testInteger2;
    }
    public short getTestShort1()
    {
        return testShort1;
    }
    public void setTestShort1(short testShort1)
    {
        this.testShort1 = testShort1;
    }
    public Short getTestShort2()
    {
        return testShort2;
    }
    public void setTestShort2(Short testShort2)
    {
        this.testShort2 = testShort2;
    }
    public java.util.Date getTestDate()
    {
        return testDate;
    }
    public void setTestDate(java.util.Date testDate)
    {
        this.testDate = testDate;
    }
    public java.util.Date getTestNullDate()
    {
        return testNullDate;
    }
    public void setTestNullDate(java.util.Date testNullDate)
    {
        this.testNullDate = testNullDate;
    }
    public java.sql.Date getTestSqlDate()
    {
        return testSqlDate;
    }
    public void setTestSqlDate(java.sql.Date testSqlDate)
    {
        this.testSqlDate = testSqlDate;
    }
    public java.sql.Timestamp getTestSqlTimestamp()
    {
        return testSqlTimestamp;
    }
    public void setTestSqlTimestamp(java.sql.Timestamp testSqlTimestamp)
    {
        this.testSqlTimestamp = testSqlTimestamp;
    }
    public GregorianCalendar getTestGc()
    {
        return testGc;
    }
    public void setTestGc(GregorianCalendar testGc)
    {
        this.testGc = testGc;
    }
    public LocalDate getTestLocalDate() 
    {
		return testLocalDate;
	}
	public void setTestLocalDate(LocalDate testLocalDate) 
	{
		this.testLocalDate = testLocalDate;
	}
	public String getTestString1()
    {
        return testString1;
    }
    public void setTestString1(String testString1)
    {
        this.testString1 = testString1;
    }
    public String getTestString2()
    {
        return testString2;
    }
    public void setTestString2(String testString2)
    {
        this.testString2 = testString2;
    }
    public EnumField getTestEnum1()
    {
        return testEnum1;
    }
    public void setTestEnum1(EnumField testEnum1)
    {
        this.testEnum1 = testEnum1;
    }
    public EnumField getTestEnum2()
    {
        return testEnum2;
    }
    public void setTestEnum2(EnumField testEnum2)
    {
        this.testEnum2 = testEnum2;
    }
}
