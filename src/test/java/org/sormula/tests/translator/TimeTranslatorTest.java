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

import java.sql.Time;
import java.time.LocalTime;
import java.util.List;

import org.sormula.SormulaException;
import org.sormula.log.SormulaLogger;
import org.sormula.log.SormulaLoggerFactory;
import org.sormula.tests.DatabaseTest;
import org.sormula.translator.standard.LocalTimeTranslator;
import org.testng.annotations.Test;


/**
 * Tests {@link TimeTranslator} and {@link LocalTimeTranslator}. This test is separate from
 * {@link ColumnTranslatorTest} because some databases don't support 
 * {@link Time}.  This test is condition based upon testTime property
 * in jdbc.properties.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="translator")
public class TimeTranslatorTest extends DatabaseTest<SormulaTestTime>
{
    private static final SormulaLogger log = SormulaLoggerFactory.getClassLogger();
    SormulaTestTime inserted;
    
    
    @Override
    protected void open() throws Exception
    {
        if (isTestTime())
        {
            super.open();
            createTable(SormulaTestTime.class, 
                "CREATE TABLE " + getSchemaPrefix() + SormulaTestTime.class.getSimpleName() + " (" +
                " testSqlTime1 TIME, " +
                " testSqlTime2 TIME, " +
                " testLocalTime1 TIME, " +
                " testLocalTime2 TIME " +
                ")"
            );
        }
    }
    
    
    @Test
    public void insertTest() throws SormulaException
    {
        if (isTestTime())
        {
            begin();
            inserted = new SormulaTestTime();
            inserted.setTestSqlTime1(new java.sql.Time(13*60*60*1000L + 25*60*1000L + 11*1000L));
            inserted.setTestSqlTime2(null);
            inserted.setTestLocalTime1(LocalTime.now().withNano(0)); // don't use nanosecond since databases don't store nanoseconds
            inserted.setTestLocalTime2(null);
            assert getTable().insert(inserted) == 1 : "1 row not inserted";
            commit();
        }
        else
        {
            log.info("skipping insertTest for java.sql.Time and java.time.LocalTime");
        }
    }
    
    
    @Test(dependsOnMethods="insertTest")
    public void selectTest() throws SormulaException
    {
        if (isTestTime())
        {
            begin();
            List<SormulaTestTime> list = getTable().selectAll();
            assert list.size() == 1 : "unexpected row count";
            SormulaTestTime selected = list.get(0);
            String message = " column inserted != selected";
            assert inserted.getTestSqlTime1().equals(selected.getTestSqlTime1()) : "testSqlTime1" + message;
            assert selected.getTestSqlTime2() == null : "testSqlTime2 should be null";
            assert inserted.getTestLocalTime1().equals(selected.getTestLocalTime1()) : "testLocalTime1" + message;
            assert selected.getTestLocalTime2() == null : "testLocalTime2 should be null";
            commit();
        }
        else
        {
            log.info("skipping selectTest for java.sql.Time and java.time.LocalTime");
        }
    }
}
