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
import org.sormula.Table;
import org.sormula.tests.DatabaseTest;
import org.sormula.translator.standard.TrimTranslator;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests {@link TrimTranslator}.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="translator")
public class TrimTranslatorTest extends DatabaseTest<SormulaTrimTest>
{
    static final String nonPaddedTestString   = "abcd";
    static final String rightPaddedTestString = "abcd      "; // length of 10 since CHAR(10)
    
    
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        createTable(SormulaTrimTest.class, 
            "CREATE TABLE " + getSchemaPrefix() + SormulaTrimTest.class.getSimpleName() + " (" +
            " id INTEGER primary key," +
            " charcolumn CHAR(10)," +
            " varcharcolumn VARCHAR(10)" +
            ")"
        );
    }
    
    
    @AfterClass
    public void tearDown() throws Exception
    {
        closeDatabase();
    }
    
    
    /**
     * Tests that Strings are trimmed when inserted or selected.
     */
    @Test
    public void trim1Test() throws SormulaException
    {
        begin();
        
        Table<SormulaTrimTest> table = getTable();
        Table<SormulaNoTrimTest> unTable = getDatabase().getTable(SormulaNoTrimTest.class);
        
        SormulaTrimTest inserted = new SormulaTrimTest();
        inserted.setId(1);
        inserted.setCharColumn("  " + nonPaddedTestString + "  ");
        inserted.setVarcharColumn("  " + nonPaddedTestString + "  ");
        assert table.insert(inserted) == 1 : "1 row not inserted";
        
        // tests that char column is trimmed when written to db
        SormulaNoTrimTest selected = unTable.select(inserted.getId());
        assert selected.getCharColumn().equals(rightPaddedTestString) : "char column was not trimmed";
        
        // tests that varchar column is trimmed when written to db
        // note: some databases always trim varchar columns
        assert selected.getVarcharColumn().equals(nonPaddedTestString) : "varchar column was not trimmed";
        
        commit();
    }
    
    
    /**
     * Tests that Strings are trimmed when inserted or selected.
     */
    @Test
    public void trim2Test() throws SormulaException
    {
        begin();
        
        Table<SormulaTrimTest> table = getTable();
        Table<SormulaNoTrimTest> unTable = getDatabase().getTable(SormulaNoTrimTest.class);
        
        SormulaNoTrimTest inserted = new SormulaNoTrimTest();
        inserted.setId(2);
        inserted.setCharColumn("  " + nonPaddedTestString + "  ");
        inserted.setVarcharColumn("  " + nonPaddedTestString + "  ");
        assert unTable.insert(inserted) == 1 : "1 row not inserted";
        
        // tests that char column is trimmed when read from db
        SormulaTrimTest selected = table.select(inserted.getId());
        assert selected.getCharColumn().equals(nonPaddedTestString) : "char column was not trimmed";
        
        // tests that varchar column is trimmed when read from db
        // note: some databases always trim varchar columns
        assert selected.getVarcharColumn().equals(nonPaddedTestString) : "varchar column was not trimmed";
        
        commit();
    }
    
    
    /**
     * Tests null Strings don't create NPE.
     */
    @Test
    public void trimNullTest() throws SormulaException
    {
        begin();
        
        SormulaTrimTest inserted = new SormulaTrimTest();
        inserted.setId(11);
        inserted.setCharColumn(null);
        inserted.setVarcharColumn(null);
        assert getTable().insert(inserted) == 1 : "1 row not inserted";
        
        SormulaTrimTest selected = getTable().select(inserted.getId());
        assert selected.getCharColumn() == null    : "char column is supposed to be null";
        assert selected.getVarcharColumn() == null : "varchar column is supposed to be null";
        
        commit();
    }
}
