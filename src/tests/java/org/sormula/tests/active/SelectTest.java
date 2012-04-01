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
package org.sormula.tests.active;

import java.util.List;

import org.sormula.active.ActiveDatabase;
import org.sormula.active.ActiveTable;
import org.sormula.log.ClassLogger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests org.sormula.active select operations.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="active.select", dependsOnGroups="active.insert")
public class SelectTest extends ActiveDatabaseTest<SormulaTestAR>
{
    private static final ClassLogger log = new ClassLogger();
    
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        createTable(SormulaTestAR.class);
    }
    
    
    @AfterClass
    public void tearDown() throws Exception
    {
        closeDatabase();
    }

    
    @Test
    public void selectCountAR()
    {
        selectTestRows(); // must perform each time since other tests are destructive
        ActiveTable<SormulaTestAR> table = getActiveTable();
        assert getAll().size() == table.<Number>selectCount().intValue() : "AR select count failed";
        assert getAll().size() == table.<Integer>selectCount("id") : "AR select count failed";
        
        int type9Count = 0;
        for (SormulaTestAR r: getAll())
        {
            if (r.getType() == 9) ++type9Count;
        }
        assert type9Count == table.<Integer>selectCount("type", "byType", 9) : "AR select count type 9 failed";
    }

    
    @Test
    public void selectAggregateAR()
    {
        selectTestRows(); // must perform each time since other tests are destructive
        
        // sum with Java
        final int type = 99;
        int sum = 0;
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        int count = 0;
        for (SormulaTestAR s: getAll())
        {
            // limit to one type to test where condition
            if (s.getType() == type)
            {
                ++count;
                sum += s.getId();
                min = Math.min(min, s.getId());
                max = Math.max(max, s.getId());
            }
        }
        
        ActiveTable<SormulaTestAR> table = getActiveTable();
        assert sum == table.<Integer>selectSum("id", "byType", type) : "AR select sum failed";
        assert min == table.<Integer>selectMin("id", "byType", type) : "AR select min failed";
        assert max == table.<Integer>selectMax("id", "byType", type) : "AR select max failed";
        
        if (log.isDebugEnabled())
        {
            log.debug("J avg="+((float)sum/count));
            log.debug("T avg="+table.<Integer>selectAvg("id", "byType", type));
        }
        assert (Integer)sum/count == table.<Integer>selectAvg("id", "byType", type) : "AR select aggregate avg failed";
    }
    
    
    @Test
    public void customSqlAR()
    {
        selectTestRows(); // must perform each time since other tests are destructive

        // expected count 
        int expectedCount = 0;
        for (SormulaTestAR r : getAll())
        {
            if (r.getType() == 9 || r.getType() == 8) ++expectedCount;
        }
        
        assert expectedCount > 0 : "AR customSql no rows meet expected condition to test";

        // select many
        ActiveTable<SormulaTestAR> table = getActiveTable();
        List<SormulaTestAR> selectedList = table.selectAllCustom("where type in(?, ?)", 9, 8);
        
        // confirm
        assert expectedCount == selectedList.size() : "AR customSql operation wrong number of rows";
        for (SormulaTestAR r : selectedList)
        {
            assert r.getType() == 9 || r.getType() == 8 : r.getId() + " row is incorrect for where condition";
        }
        
        // select one
        SormulaTestAR rExpected = selectedList.get(selectedList.size() - 1); // use last as test
        SormulaTestAR rActual = table.selectCustom("where id = ?", rExpected.getId());
        assert rActual != null && rActual.getId() == rExpected.getId() : rActual.getId() + " row is incorrect for where condition";
    }
    
    
    @Test
    public void selectWhereAR()
    {
    	selectTestRows(); // must perform each time since other tests are destructive
    	SormulaTestAR rExpected = getRandom();
        
        // select one (more than one record is possible, test first one found)
    	ActiveTable<SormulaTestAR> table = getActiveTable();
        SormulaTestAR rActual = table.selectWhere("byType", rExpected.getType());
        assert rActual != null && rActual.getType() == rExpected.getType() : rActual.getId() + " row is incorrect type for where condition";
    }
    
    
    @Test
    public void selectWhereAR2()
    {
        selectTestRows(); // must perform each time since other tests are destructive
        SormulaTestAR rExpected = getRandom();

        // test default active data base
        ActiveDatabase.setDefault(getActiveDatabase());
        
        // select one (more than one record is possible, test first one found)
        SormulaTestAR rActual = SormulaTestAR.table.selectWhere("byType", rExpected.getType());
        assert rActual != null && rActual.getType() == rExpected.getType() : rActual.getId() + " row is incorrect type for where condition";
    }

    
    @Test
    public void selectAllWhereAR()
    {
        selectTestRows(); // must perform each time since other tests are destructive

        // expected count 
        int expectedCount = 0;
        for (SormulaTestAR r : getAll())
        {
            if (r.getType() == 8) ++expectedCount;
        }
        
        assert expectedCount > 0 : "selectAllWhereAR no rows meet expected condition to test";

        // select many
        ActiveTable<SormulaTestAR> table = getActiveTable();
        List<SormulaTestAR> selectedList = table.selectAllWhere("byType", 8);
        
        // confirm
        assert expectedCount == selectedList.size() : "selectAllWhereAR operation wrong number of rows";
        for (SormulaTestAR r : selectedList)
        {
            assert r.getType() == 8 : r.getId() + " row is incorrect for where condition";
        }
    }
    

    @Test
    public void selectAllWhereOrderedAR()
    {
        selectTestRows(); // must perform each time since other tests are destructive
        ActiveTable<SormulaTestAR> table = getActiveTable();
        List<SormulaTestAR> selectedList = table.selectAllWhereOrdered("byType", "obDesc", 99);
        
        assert selectedList.size() > 0 : "no rows selected";
        
        String previousDescription = "";
        for (SormulaTestAR r: selectedList)
        {
            assert r.getDescription().compareTo(previousDescription) >= 0 : 
                r.getId() + " row is not in ascending order by description";
        }
    }
}
