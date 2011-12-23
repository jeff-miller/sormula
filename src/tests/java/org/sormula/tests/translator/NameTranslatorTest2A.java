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

import java.util.List;

import org.sormula.Database;
import org.sormula.SormulaException;
import org.sormula.tests.DatabaseTest;
import org.sormula.translator.StandardNameTranslator;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests {@linkplain StandardNameTranslator} for table and column names. StandardNameTranslator
 * is defined as default in {@link Database} instead of at row class as in {@link NameTranslatorTest}.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="translator")
public class NameTranslatorTest2A extends DatabaseTest<SormulaTest2A>
{
    SormulaTest2A inserted;
    
    
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        
        // name translator specified here instead of in row annotation
        getDatabase().setNameTranslatorClass(StandardNameTranslator.class);
        
        createTable(SormulaTest2A.class, 
            "CREATE TABLE " + getSchemaPrefix() + "sormula_test2_A (" +
            " test_boolean_yes_no CHAR(1)," +
            " test_double DECIMAL(8,3)," +
            " altname INTEGER," +
            " test_date TIMESTAMP," +
            " test_string VARCHAR(10)" +
            ")"
        );
    }
    
    
    @AfterClass
    public void tearDown() throws Exception
    {
        closeDatabase();
    }
    
    
    @Test
    public void insertTest() throws SormulaException
    {
        inserted = new SormulaTest2A();
        
        inserted.setTestBooleanYesNo(true);
        inserted.setTestDouble(12345.678);
        inserted.setTestInteger(1234567890);
        inserted.setTestString("abcdef");
        inserted.setTestDate(new java.util.Date(System.currentTimeMillis()));
        
        assert getTable().insert(inserted) == 1 : "1 row not inserted";
    }
    
    
    @Test(dependsOnMethods="insertTest")
    public void selectTest() throws SormulaException
    {
         List<SormulaTest2A> list = getTable().selectAll();
         assert list.size() == 1 : "unexpected row count";
         SormulaTest2A selected = list.get(0);
         String message = " column inserted != selected";
         
         assert inserted.isTestBooleanYesNo() == selected.isTestBooleanYesNo() : "testBooleanYN" + message;
         assert inserted.getTestInteger() == selected.getTestInteger() : "testInteger" + message;
         assert Math.abs(inserted.getTestDouble() - selected.getTestDouble()) < 0.001 : "testDouble" + message;
         assert inserted.getTestString().equals(selected.getTestString()) : "testString" + message;
         assert inserted.getTestDate().equals(selected.getTestDate()) : "testDate" + message;
    }
}
