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

import org.sormula.SormulaException;
import org.sormula.log.ClassLogger;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests {@link LongTranslator}. This test is separate from
 * {@link ColumnTranslatorTest} because some databases don't support 
 * BIGINT data type. This test is condition based upon testLong property
 * in jdbc.properties.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="translator")
public class LongTranslatorTest extends DatabaseTest<SormulaTestLong>
{
    private static final ClassLogger log = new ClassLogger();
	SormulaTestLong inserted;
    
    
    @BeforeClass
    public void setUp() throws Exception
    {
        if (isTestBigDecimal())
        {
            openDatabase();
            createTable(SormulaTestLong.class, 
                "CREATE TABLE " + getSchemaPrefix() + SormulaTestLong.class.getSimpleName() + " (" +
                " testLong1 BIGINT," +
                " testLong2 BIGINT" +
                ")"
            );
        }
    }
    
    
    @AfterClass
    public void tearDown() throws Exception
    {
        if (isTestLong())
        {
            closeDatabase();
        }
    }
    
    
    @Test
    public void insertTest() throws SormulaException
    {
        if (isTestLong())
        {
            begin();
            inserted = new SormulaTestLong();
            inserted.setTestLong1(123456789012345678L);
            inserted.setTestLong2(-123456789012345678L);
            assert getTable().insert(inserted) == 1 : "1 row not inserted";
            commit();
        }
        else
        {
            log.info("skipping insertTest for long");
        }
    }
    
    
    @Test(dependsOnMethods="insertTest")
    public void selectTest() throws SormulaException
    {
        if (isTestLong())
        {
            begin();
            List<SormulaTestLong> list = getTable().selectAll();
            assert list.size() == 1 : "unexpected row count";
            SormulaTestLong selected = list.get(0);
            String message = " column inserted != selected";
            assert inserted.getTestLong1() == selected.getTestLong1()     : "testLong1" + message;
            assert inserted.getTestLong2().equals(selected.getTestLong2()): "testLong2" + message;
            commit();
        }
        else
        {
            log.info("skipping selectTest for long");
        }
    }
}
