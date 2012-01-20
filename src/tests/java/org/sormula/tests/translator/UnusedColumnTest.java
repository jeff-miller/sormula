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

import org.sormula.SormulaException;
import org.sormula.annotation.UnusedColumn;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests unused column annotations.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="translator")
public class UnusedColumnTest extends DatabaseTest<SormulaTest3>
{
    private static final int primaryKey = 9999;
    
    
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        createTable(SormulaTest3.class, 
            "CREATE TABLE " + getSchemaPrefix() + SormulaTest3.class.getSimpleName() + " (" +
            " testInteger INTEGER NOT NULL," +
            " unusedInt INTEGER NOT NULL," +
            " unusedString VARCHAR(10) NOT NULL" +
            ")"
        );
    }
    
    
    @AfterClass
    public void tearDown() throws Exception
    {
        closeDatabase();
    }
    
    
    /**
     * Test that insert sets {@link UnusedColumn#value()} for unused columns.
     * Throws SQLException if unused columns do not contain a value since they are declared as NOT NULL.
     */
    @Test
    public void insertTest() throws SormulaException
    {
        begin();
        SormulaTest3 sormulaTest3 = new SormulaTest3();
        sormulaTest3.setTestInteger(primaryKey);
        assert getTable().insert(sormulaTest3) == 1 : "1 row not inserted";
        commit();
    }
    
    
    /**
     * Test that update sets {@link UnusedColumn#value()} for unused columns.
     * Throws SQLException if unused columns do not contain a value since they are declared as NOT NULL.
     */
    @Test(dependsOnMethods="insertTest")
    public void updateTest() throws SormulaException
    {
        begin();
        SormulaTest3 sormulaTest3 = getTable().select(primaryKey);
        assert sormulaTest3 != null : "no row for primary key";
        getTable().update(sormulaTest3);
        commit();
    }
}
