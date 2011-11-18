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
package org.sormula.tests.zeroannotation;

import java.util.Set;

import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests some delete operations for row with no annotations.
 * 
 * @author Jeff Miller
 */
@Test(groups="zeroannotation.delete", dependsOnGroups="zeroannotation.insert")
public class DeleteTest extends DatabaseTest<ZeroAnnotationTest>
{
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        createTable(ZeroAnnotationTest.class, null);
    }
    
    
    @AfterClass
    public void tearDown() throws Exception
    {
        closeDatabase();
    }
    
    
    @Test
    public void deleteOne() throws SormulaException
    {
    	begin();
        selectTestRows(); // must perform each time since other tests are destructive
        
        // choose random row
        ZeroAnnotationTest row = getRandom();

        assert getTable().delete(row) == 1 : "delete one row failed";
        
        // read row to confirm that delete applied
        assert getTable().select(row.getId()) == null : "row was not deleted";
        
        commit();
    }
    
    
    @Test
    public void deleteCollection() throws SormulaException
    {
    	begin();
        selectTestRows(); // must perform each time since other tests are destructive
        
        // choose random set
        Set<ZeroAnnotationTest> set = getRandomSet();
        
        // delete
        Table<ZeroAnnotationTest> table = getTable();
        assert table.deleteAll(set) == set.size() : "delete count not same as collection size";
        
        // confirm each row was deleted
        for (ZeroAnnotationTest r: set)
        {
            ZeroAnnotationTest r2 = table.select(r.getId());
            assert r2 == null : r.getId() + " was not deleted";
        }
        
        commit();
    }
}
