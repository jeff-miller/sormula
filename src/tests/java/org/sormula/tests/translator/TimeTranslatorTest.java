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

import java.sql.Time;
import java.util.List;

import org.sormula.SormulaException;
import org.sormula.log.ClassLogger;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests {@link TimeTranslator}. This test is separate from
 * {@link ColumnTranslatorTest} because some database JDBC drivers don't support 
 * {@link Time}.  This test is condition based upon testTime property
 * in jdbc.properties.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="translator")
public class TimeTranslatorTest extends DatabaseTest<SormulaTestTime>
{
    private static final ClassLogger log = new ClassLogger();
    SormulaTestTime inserted;
    
    
    @BeforeClass
    public void setUp() throws Exception
    {
        if (isTestTime())
        {
            openDatabase();
            createTable(SormulaTestTime.class, 
                "CREATE TABLE " + getSchemaPrefix() + SormulaTestTime.class.getSimpleName() + " (" +
                " testSqlTime TIME " +
                ")"
            );
        }
    }
    
    
    @AfterClass
    public void tearDown() throws Exception
    {
        if (isTestTime())
        {
            closeDatabase();
        }
    }
    
    
    @Test
    public void insertTest() throws SormulaException
    {
        if (isTestTime())
        {
            inserted = new SormulaTestTime();
            inserted.setTestSqlTime(new java.sql.Time(13*60*60*1000L + 25*60*1000L + 11*1000L));
            assert getTable().insert(inserted) == 1 : "1 row not inserted";
        }
        else
        {
            log.info("skipping insertTest for java.sql.Time");
        }
    }
    
    
    @Test(dependsOnMethods="insertTest")
    public void selectTest() throws SormulaException
    {
        if (isTestTime())
        {
            List<SormulaTestTime> list = getTable().selectAll();
            assert list.size() == 1 : "unexpected row count";
            SormulaTestTime selected = list.get(0);
            String message = " column inserted != selected";
            assert inserted.getTestSqlTime().equals(selected.getTestSqlTime()) : "testSqlTime" + message;
        }
        else
        {
            log.info("skipping selectTest for java.sql.Time");
        }
    }
}
