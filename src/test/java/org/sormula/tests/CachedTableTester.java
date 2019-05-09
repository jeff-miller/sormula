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
package org.sormula.tests;

import org.sormula.CachedTable;
import org.sormula.SormulaException;
import org.sormula.builder.CachedTableBuilder;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests for {@link CachedTableBuilder}.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="fundamental.table")
public class CachedTableTester extends DatabaseTest<TableTesterRow>
{
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
    }
    
    
    @AfterClass
    public void tearDown() throws Exception
    {
        closeDatabase();
    }
    
    
    @Test
    public void builderCached() throws SormulaException
    {
        CachedTable<TableTesterRow> cachedTable = 
                CachedTable.builderCached(getDatabase(), TableTesterRow.class).build();
        assert cachedTable != null : "cached table builder fail";
    }
}
