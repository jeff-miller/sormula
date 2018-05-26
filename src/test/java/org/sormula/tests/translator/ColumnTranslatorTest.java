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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.Instant;
import java.time.LocalDate;
import java.util.GregorianCalendar;

import org.sormula.SormulaException;
import org.sormula.log.SormulaLogger;
import org.sormula.log.SormulaLoggerFactory;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests column translators in org.sorm.translator.standard by using a table with a
 * column for each type of translator.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="translator")
public class ColumnTranslatorTest extends DatabaseTest<SormulaTest1>
{
    private static final SormulaLogger log = SormulaLoggerFactory.getClassLogger();
    private static final int TEST_STRING_COLUMN_LENGTH = 20;
    
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        String secondsPrecisionDDL = getSecondsPrecisionDDL();
        String timestampNullKeyword = getTimestampNullKeyword(); // insure null values are stored as null
        createTable(SormulaTest1.class, 
            "CREATE TABLE " + getSchemaPrefix() + SormulaTest1.class.getSimpleName() + " (" +
            " testBoolean1 " + getBooleanDDL() + "," +
            " testBoolean2 " + getBooleanDDL() + "," +
            " testBooleanYN1 CHAR(1)," +
            " testBooleanYN2 CHAR(1)," +
            " testByte1 SMALLINT," +
            " testByte2 SMALLINT," +
            " testDouble1 DECIMAL(8,3)," +
            " testDouble2 DECIMAL(8,3)," +
            " testFloat1 DECIMAL(6,2)," +
            " testFloat2 DECIMAL(6,2)," +
            " testInteger1 INTEGER," +
            " testInteger2 INTEGER," +
            " testShort1 SMALLINT," +
            " testShort2 SMALLINT," +
            " testDate TIMESTAMP"         + secondsPrecisionDDL + "," +
            " testNullDate TIMESTAMP"     + secondsPrecisionDDL + " " + timestampNullKeyword + "," +
            " testSqlDate DATE," +
            " testSqlTimestamp TIMESTAMP" + secondsPrecisionDDL + " " + timestampNullKeyword + "," +
            " testGc TIMESTAMP"           + secondsPrecisionDDL + " " + timestampNullKeyword + "," +
            " testLocalDate DATE," +
            " testInstant TIMESTAMP"      + secondsPrecisionDDL + " " + timestampNullKeyword + "," +
            " testString1 VARCHAR(" + TEST_STRING_COLUMN_LENGTH + ")," +
            " ts2 CHAR(" + TEST_STRING_COLUMN_LENGTH + ")," +
            " testEnum1 VARCHAR(10)," +
            " testEnum2 VARCHAR(10)," +
            " testEnumTS1 CHAR(1)," +
            " testEnumTS2 CHAR(1)" +
            ")"
        );
    }
    
    
    @AfterClass
    public void tearDown() throws Exception
    {
        closeDatabase();
    }
    
    
    protected SormulaTest1 insertTest(String key) throws SormulaException
    {
        SormulaTest1 inserted = new SormulaTest1();
        
        // primitives and object equivalents
        inserted.setTestBoolean1(true);
        inserted.setTestBoolean2(true);
        inserted.setTestBooleanYN1(true);
        inserted.setTestBooleanYN2(true);
        inserted.setTestByte1((byte)123);
        inserted.setTestByte2((byte)-123);
        inserted.setTestDouble1(12345.678);
        inserted.setTestDouble2(-98765.432);
        inserted.setTestFloat1(123.45f);
        inserted.setTestFloat2(-567.89f);
        inserted.setTestInteger1(1234567890);
        inserted.setTestInteger2(-1234567890);
        inserted.setTestShort1((short)12345);
        inserted.setTestShort2((short)-12345);
        
        // enums
        inserted.setTestEnum1(EnumField.Good);
        inserted.setTestEnum2(EnumField.Ugly);
        inserted.setTestEnumTS1(EnumFieldTS.Hot);
        inserted.setTestEnumTS2(EnumFieldTS.Cold);
        
        // string
        inserted.setTestString1(key);
        inserted.setTestString2("wxyz");
        
        // date/time
        inserted.setTestDate(new java.util.Date(System.currentTimeMillis()));
        inserted.setTestNullDate(null);
        inserted.setTestSqlDate(new java.sql.Date(new GregorianCalendar(2010, 6, 4).getTimeInMillis()));
        inserted.setTestSqlTimestamp(new java.sql.Timestamp(System.currentTimeMillis()));
        inserted.setTestGc(new GregorianCalendar());
        inserted.setTestLocalDate(LocalDate.now());
        inserted.setTestInstant(Instant.now());
        
        begin();
        assert getTable().insert(inserted) == 1 : "1 row not inserted";
        commit();
        
        return inserted;
    }
    

    @Test //(dependsOnMethods="insertTest")
    public void selectTest() throws SormulaException
    {
        SormulaTest1 inserted = insertTest("selectTest"); // use this instead, dependsOnMethods caused problems
        
        begin();
        SormulaTest1 selected = getTable().selectWhere("forTestSting1", inserted.getTestString1());
        assert selected != null : "no test row";
        String message = " column inserted != selected";
         
        // primitive column tests
        assert inserted.isTestBoolean1()   == selected.isTestBoolean1()   : "testBoolean1" + message;
        assert inserted.isTestBooleanYN1() == selected.isTestBooleanYN1() : "testBooleanYN1" + message;
        assert inserted.getTestByte1()     == selected.getTestByte1()     : "testByte1" + message;
        assert inserted.getTestInteger1()  == selected.getTestInteger1()  : "testInteger1" + message;
        assert inserted.getTestShort1()    == selected.getTestShort1()    : "testShort1" + message;
         
        float deltaFloat1 = Math.abs(inserted.getTestFloat1()  - selected.getTestFloat1()); 
        assert deltaFloat1  < 0.01 : "testFloat1" + message + " " +
            inserted.getTestFloat1() + " " + selected.getTestFloat1() + " delta="+deltaFloat1;

        double deltaDouble1 = Math.abs(inserted.getTestDouble1()  - selected.getTestDouble1()); 
        assert deltaDouble1  < 0.001 : "testDouble1" + message + " " +
            inserted.getTestDouble1() + " " + selected.getTestDouble1() + " delta="+deltaDouble1;

        // object column tests
        assert inserted.getTestBoolean2()  .equals(selected.getTestBoolean2())   : "testBoolean2" + message;
        assert inserted.getTestBooleanYN2().equals(selected.getTestBooleanYN2()) : "testBooleanYN2" + message;
        assert inserted.getTestByte2()     .equals(selected.getTestByte2())      : "testByte2" + message;
        assert inserted.getTestInteger2()  .equals(selected.getTestInteger2())   : "testInteger2" + message;
        assert inserted.getTestShort2()    .equals(selected.getTestShort2())     : "testShort2" + message;

        float deltaFloat2 = Math.abs(inserted.getTestFloat2()  - selected.getTestFloat2()); 
        assert deltaFloat2  < 0.01 : "testFloat2" + message + " " +
            inserted.getTestFloat2() + " " + selected.getTestFloat2() + " delta="+deltaFloat2;
         
        double deltaDouble2 = Math.abs(inserted.getTestDouble2()  - selected.getTestDouble2()); 
        assert deltaDouble2  < 0.001 : "testDouble2" + message + " " +
            inserted.getTestDouble2() + " " + selected.getTestDouble2() + " delta="+deltaDouble2;
         
        // string column tests
        assert inserted.getTestString1()   .equals(selected.getTestString1())    : "testString1" + message;
        if (selected.getTestString2().length() != TEST_STRING_COLUMN_LENGTH)
        {
            // CHAR not always implemented consistently in all db's
            log.error("CHAR not padded with blanks length=" + selected.getTestString2().length());
        }
        assert inserted.getTestString2()   .equals(selected.getTestString2().trim()) : "testString2" + message;
         
        // date/time tests
        assert selected.getTestNullDate() == null : "testNullDate" + message;
        assert inserted.getTestDate()        .equals(selected.getTestDate())         : "testDate" + message;
        assert inserted.getTestSqlDate()     .equals(selected.getTestSqlDate())      : "testSqlDate" + message;
        assert inserted.getTestSqlTimestamp().equals(selected.getTestSqlTimestamp()) : "testSqlTimestamp" + message;
        assert inserted.getTestGc()          .equals(selected.getTestGc())           : "testGc" + message;
        assert inserted.getTestLocalDate()   .equals(selected.getTestLocalDate())    : "testLocalDate" + message;
        assert inserted.getTestInstant()     .equals(selected.getTestInstant())      : "testInstant" + message;
        
        // enum tests
        assert inserted.getTestEnum1().equals(selected.getTestEnum1()) : "testEnum1" + message;
        assert inserted.getTestEnum2().equals(selected.getTestEnum2()) : "testEnum2" + message;
        assert inserted.getTestEnumTS1().equals(selected.getTestEnumTS1()) : "testEnumTS1" + message;
        assert inserted.getTestEnumTS2().equals(selected.getTestEnumTS2()) : "testEnumTS2" + message;
        
        commit();
    }
    

    @Test
    public void nullTest() throws Exception
    {
        begin();
        
        // non primitive members will be null
        SormulaTest1 inserted = new SormulaTest1();
        inserted.setTestString1("nullTest"); // id for select
        
        if (!isBooleanDDL())
        {
            // don't test null for non boolean column types 
            // nulls cause exception since BooleanTranslator uses Types.BOOLEAN
            log.info("skipping null boolean tests");
            inserted.setTestBoolean2(false);
        }
        
        // insert row with null for non primitives 
        getTable().insert(inserted);
        
        SormulaTest1 selected = getTable().selectWhere("forTestSting1", inserted.getTestString1());
        assert selected != null : "no test row";

        String message = " should be null";
        if (isBooleanDDL()) assert selected.getTestBoolean2() == null : "testBoolean2 " + message;
        assert selected.getTestBooleanYN2() == null : "testBooleanYN2 " + message;
        assert selected.getTestByte2() == null : "testByte2 " + message;
        assert selected.getTestDouble2() == null : "testDouble2 " + message;
        assert selected.getTestFloat2() == null : "testFloat2" + message;
        assert selected.getTestInteger2() == null : "testInteger2" + message;
        assert selected.getTestShort2() == null : "testShort2" + message;
        assert selected.getTestGc() == null : "testGc" + message;
        assert selected.getTestLocalDate() == null : "testLocalDate" + message;
        assert selected.getTestInstant() == null : "testInstant" + message;
        // DateTranslator null is tested in selectTest()
        assert selected.getTestSqlDate() == null : "testSqlDate" + message;
        assert selected.getTestSqlTimestamp() == null : "testSqlTimestamp" + message;
        assert selected.getTestEnum1() == null : "testEnum1" + message;
        assert selected.getTestEnum2() == null : "testEnum2" + message;
        assert selected.getTestEnumTS1() == null : "testEnumTS1" + message;
        assert selected.getTestEnumTS2() == null : "testEnumTS2" + message;
        
        commit();
    }
    
    
    @Test //(dependsOnMethods="insertTest")
    public void defaultEnumTest() throws Exception
    {
        SormulaTest1 inserted = insertTest("defaultEnumTest"); // use this instead, dependsOnMethods caused problems
        
        begin();
        
        // change database column value to a non enum 
        Connection connection = getDatabase().getConnection();
        PreparedStatement statement = connection.prepareStatement("update " + 
                getSchemaPrefix() + SormulaTest1.class.getSimpleName() + " set testEnum2='zzz', testEnumTS2='z' where testString1=?");
        statement.setString(1, inserted.getTestString1());
        assert statement.executeUpdate() == 1 : "testEnum2 and testEnumTS2 columns were not changed";
        statement.close();
        
        // testEnum2 should be read as 'zzz' and converted to default enum
        SormulaTest1 selected = getTable().selectWhere("forTestSting1", inserted.getTestString1());
        assert selected != null : "no test row";
        assert selected.getTestEnum2().equals(EnumField.Bad) : "testEnum2 did not use default enum";
        assert selected.getTestEnumTS2().equals(EnumFieldTS.Warm) : "testEnumTS2 did not use default enum";
        commit();
    }
}
