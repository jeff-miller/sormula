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
package org.sormula.tests.operation.np;

import java.util.List;

import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.log.ClassLogger;
import org.sormula.operation.ArrayListSelectOperation;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests named parameter selects.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="cascade.select", dependsOnGroups="cascade.insert")
public class SelectTest extends DatabaseTest<SormulaTestNP1>
{
    private static final ClassLogger log = new ClassLogger();
    
    
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        createTable(SormulaTestNP1.class);
    }
    
    
    @AfterClass
    public void tearDown() throws Exception
    {
        closeDatabase();
    }
    
    
    @Test
    public void selectWithNamedParameter() throws SormulaException
    {
        int testId = 102;
        
        begin();
        @SuppressWarnings("resource") // selectAll method invokes close
        ArrayListSelectOperation<SormulaTestNP2> operation = new ArrayListSelectOperation<>(
                getDatabase().getTable(SormulaTestNP2.class), "byParent");
        operation.setParameter("parentId", testId);
        confirmByParent(operation.selectAll(/*no parameters here*/), testId);
        commit();
    }
    
    
    @Test
    public void selectWithPositionalParameter() throws SormulaException
    {
        int testId = 102;
        
        begin();
        @SuppressWarnings("resource") // selectAll method invokes close
        ArrayListSelectOperation<SormulaTestNP2> operation = new ArrayListSelectOperation<>(
                getDatabase().getTable(SormulaTestNP2.class), "byParent");
        operation.setParameter("parentId", 999); // confirm that this is NOT used
        confirmByParent(operation.selectAll(testId), testId);
        commit();
    }
    
    
    protected void confirmByParent(List<SormulaTestNP2> results, int testId) throws SormulaException
    {
        Table<SormulaTestNP2> sormulaTestNP2Table = getDatabase().getTable(SormulaTestNP2.class);
        assert results.size() == sormulaTestNP2Table.<Integer>selectCount("level2Id", "byParent", testId) 
                : "wrong number of rows selected";
        
        for (SormulaTestNP2 row : results)
        {
            assert row.getParentId() == testId : "wrong row selected";
        }
    }
    
    
    @Test
    public void selectCascade() throws SormulaException
    {
        int minLevel2Id = 222;
        
        begin();
        @SuppressWarnings("resource") // selectAll method invokes close
        ArrayListSelectOperation<SormulaTestNP1> operation = new ArrayListSelectOperation<>(
                getDatabase().getTable(SormulaTestNP1.class), "");
        operation.setParameter("minLevel2Id", minLevel2Id); // SormulaTestNP1 cascade uses $minLevel2Id
        List<SormulaTestNP1> results = operation.selectAll();
        //logGraph(results, "");
        for (SormulaTestNP1 r1 : results)
        {
            // confirm that all children have desired id
            for (SormulaTestNP2 r2 : r1.getChildList())
            {
                assert r2.getLevel2Id() >= minLevel2Id : "wrong level 2 child selected";
            }
        }
        commit();
    }
    
    
    protected void logGraph(List<SormulaTestNP1> level1Rows, String message)
    {
        log.info(message);
        for (SormulaTestNP1 row1 : level1Rows)
        {
            log.info("1 " + row1.getLevel1Id());
            
            for (SormulaTestNP2 row2 : row1.getChildList())
            {
                log.info("2   " + row2.getLevel2Id());    
            }
        }
    }
}
