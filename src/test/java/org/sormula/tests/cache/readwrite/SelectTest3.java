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
package org.sormula.tests.cache.readwrite;

import org.sormula.SormulaException;
import org.sormula.annotation.cache.Cached;
import org.sormula.tests.cache.CacheTest;
import org.testng.annotations.Test;


/**
 * Tests {@link Cached#evictOnTransactionEnd()} set true. 
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="cache.readwrite.select", dependsOnGroups="cache.readwrite.insert")
public class SelectTest3 extends CacheTest<SormulaCacheTestRW3>
{
    @Override
    protected void open() throws Exception
    {
        super.open();
        createTable(SormulaCacheTestRW3.class);
    }
    
    
    @Test
    public void selectBasic() throws SormulaException
    {
        begin();
        SormulaCacheTestRW3 test = getTable().select(101);
        assert test != null : "test row 101 is not in database";
        confirmCached(test);
        commit();
        
        // confirm that cache was evicted upon commit
        begin();
        confirmNotCached(test);
        commit();
    }
}
