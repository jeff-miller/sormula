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

import java.util.List;

import org.sormula.SormulaException;
import org.sormula.annotation.Row;
import org.sormula.tests.DatabaseTest;
import org.sormula.translator.ExpandedNameTranslator;
import org.sormula.translator.Sql92KeywordNameTranslator;
import org.sormula.translator.UpperCaseNameTranslator;
import org.testng.annotations.Test;


/**
 * Tests {@link ExpandedNameTranslator}, {@link Sql92KeywordNameTranslator}, and 
 * {@link UpperCaseNameTranslator} for table and column names.  
 * {@link SormulaTest2} has translators defined with {@link Row} annotation.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="translator")
public class NameTranslatorTest extends DatabaseTest<SormulaTest2>
{
    SormulaTest2 inserted;
    
    
    @Override
    protected void open() throws Exception
    {
        super.open();
        createTable(SormulaTest2.class, 
            "CREATE TABLE " + getSchemaPrefix() + "sormula_test2 (" +
            " test_boolean_yes_no CHAR(1)," +
            " test_double DECIMAL(8,3)," +
            " altname INTEGER," +
            " test_date TIMESTAMP" + getSecondsPrecisionDDL() + "," +
            " \"BETWEEN\" VARCHAR(10)" + // tests translators when column name is sql keyword 
            ")"
        );
    }
    
    
    @Test
    public void insertTest() throws SormulaException
    {
        inserted = new SormulaTest2();
        
        inserted.setTestBooleanYesNo(true);
        inserted.setTestDouble(12345.678);
        inserted.setTestInteger(1234567890);
        inserted.setBetween("abcdef");
        inserted.setTestDate(new java.util.Date(System.currentTimeMillis()));
        
        begin();
        assert getTable().insert(inserted) == 1 : "1 row not inserted";
        commit();
    }
    
    
    @Test(dependsOnMethods="insertTest")
    public void selectTest() throws SormulaException
    {
        begin();
        List<SormulaTest2> list = getTable().selectAll();
        assert list.size() == 1 : "unexpected row count";
        SormulaTest2 selected = list.get(0);
        String message = " column inserted != selected";
         
        assert inserted.isTestBooleanYesNo() == selected.isTestBooleanYesNo() : "testBooleanYN" + message;
        assert inserted.getTestInteger() == selected.getTestInteger() : "testInteger" + message;
        assert Math.abs(inserted.getTestDouble() - selected.getTestDouble()) < 0.001 : "testDouble" + message;
        assert inserted.getBetween().equals(selected.getBetween()) : "between" + message;
        assert inserted.getTestDate().equals(selected.getTestDate()) : "testDate" + message;
        commit();
    }
}
