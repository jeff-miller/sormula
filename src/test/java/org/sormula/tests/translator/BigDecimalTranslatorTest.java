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

import java.math.BigDecimal;
import java.util.List;

import org.sormula.SormulaException;
import org.sormula.log.SormulaLogger;
import org.sormula.log.SormulaLoggerFactory;
import org.sormula.tests.DatabaseTest;
import org.sormula.translator.standard.BigDecimalTranslator;
import org.testng.annotations.Test;


/**
 * Tests {@link BigDecimalTranslator}. This test is separate from
 * {@link ColumnTranslatorTest} because some databases don't support 
 * {@link BigDecimal}. This test is condition based upon testBigDecimal property
 * in jdbc.properties.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="translator")
public class BigDecimalTranslatorTest extends DatabaseTest<SormulaTestBD>
{
    private static final SormulaLogger log = SormulaLoggerFactory.getClassLogger();
	SormulaTestBD inserted;
    
    
	@Override
    protected void open() throws Exception
    {
        if (isTestBigDecimal())
        {
            super.open();
            
            createTable(SormulaTestBD.class, 
                "CREATE TABLE " + getSchemaPrefix() + SormulaTestBD.class.getSimpleName() + " (" +
                " testBigDecimal1 DECIMAL(18,8)," + // firebird only allows max precesion of 18
                " testBigDecimal2 DECIMAL(18,8)"  + // firebird only allows max precesion of 18
                ")"
            );
        }
    }
    
    
    @Test
    public void insertTest() throws SormulaException
    {
        if (isTestBigDecimal())
        {
            begin();
            inserted = new SormulaTestBD();
            inserted.setTestBigDecimal1(new BigDecimal("1234567890.01234567"));
            inserted.setTestBigDecimal2(null);
            assert getTable().insert(inserted) == 1 : "1 row not inserted";
            commit();
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
            begin();
            List<SormulaTestBD> list = getTable().selectAll();
            assert list.size() == 1 : "unexpected row count";
            SormulaTestBD selected = list.get(0);
            String message = " column inserted != selected";
            assert inserted.getTestBigDecimal1().equals(selected.getTestBigDecimal1()) : "testBigDecimal1" + message;
            assert selected.getTestBigDecimal2() == null : "testBigDecimal2 should be null";
            commit();
        }
        else
        {
            log.info("skipping selectTest for BigDecimal");
        }
    }
}
