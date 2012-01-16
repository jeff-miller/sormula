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

import java.math.BigDecimal;
import java.util.List;

import org.sormula.SormulaException;
import org.sormula.log.ClassLogger;
import org.sormula.tests.DatabaseTest;
import org.sormula.translator.standard.BigDecimalTranslator;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests {@link BigDecimalTranslator}. This test is separate from
 * {@link ColumnTranslatorTest} because some database JDBC drivers don't support 
 * {@link BigDecimal}. This test is condition based upon TODO
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="translator")
public class BigDecimalTranslatorTest extends DatabaseTest<SormulaTestBD>
{
    private static final ClassLogger log = new ClassLogger();
	SormulaTestBD inserted;
    
    
    @BeforeClass
    public void setUp() throws Exception
    {
        if (isTestBigDecimal())
        {
            openDatabase();
            createTable(SormulaTestBD.class, 
                "CREATE TABLE " + getSchemaPrefix() + SormulaTestBD.class.getSimpleName() + " (" +
                " testBigDecimal DECIMAL(18,8)" + // firebird only allows max precesion of 18
                ")"
            );
        }
    }
    
    
    @AfterClass
    public void tearDown() throws Exception
    {
        if (isTestBigDecimal())
        {
            closeDatabase();
        }
    }
    
    
    @Test
    public void insertTest() throws SormulaException
    {
        if (isTestBigDecimal())
        {
            inserted = new SormulaTestBD();
            inserted.setTestBigDecimal(new BigDecimal("1234567890.01234567"));
            assert getTable().insert(inserted) == 1 : "1 row not inserted";
        }
        else
        {
            log.info("skipping insertTest for BigDecimal");
        }
    }
    
    
    @Test(dependsOnMethods="insertTest")
    public void selectTest() throws SormulaException
    {
        if (isTestBigDecimal())
        {
            List<SormulaTestBD> list = getTable().selectAll();
            assert list.size() == 1 : "unexpected row count";
            SormulaTestBD selected = list.get(0);
            String message = " column inserted != selected";
            assert inserted.getTestBigDecimal().equals(selected.getTestBigDecimal()) : "testBigDecimal" + message;
        }
        else
        {
            log.info("skipping selectTest for BigDecimal");
        }
    }
}
