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
package org.sormula.tests.cascade.multilevel;

import java.util.ArrayList;
import java.util.List;

import org.sormula.SormulaException;
import org.sormula.log.ClassLogger;
import org.sormula.operation.ArrayListSelectOperation;
import org.sormula.operation.filter.SelectCascadeFilter;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests {@link SelectOperationFilter}.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="cascade.select", dependsOnGroups="cascade.insert")
public class SelectTest extends DatabaseTest<SormulaTestLevel1>
{
    private static final ClassLogger log = new ClassLogger();
    
    
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        createTable(SormulaTestLevel1.class);
    }
    
    
    @AfterClass
    public void tearDown() throws Exception
    {
        closeDatabase();
    }

    
    @Test
    public void filter123() throws SormulaException
    {
        begin();
        filterTest(new Level1Filter(), new Level2Filter(), new Level3Filter());
        commit();
    }

    
    @Test
    public void filterA() throws SormulaException
    {
        begin();
        filterTest(new AllLevelsFilterA());
        commit();
    }

    
    @Test
    public void filterB() throws SormulaException
    {
        begin();
        filterTest(new AllLevelsFilterB());
        commit();
    }
    
    
    @SuppressWarnings("unchecked")
    protected void filterTest(SelectCascadeFilter<?>... selectCascadeFilters) throws SormulaException
    {
    	@SuppressWarnings("resource") // selectAll method invokes close
        ArrayListSelectOperation<SormulaTestLevel1> filteredSelectOperation = 
                new ArrayListSelectOperation<>(getTable(), "");
        filteredSelectOperation.setSelectCascadeFilters(selectCascadeFilters);
        List<SormulaTestLevel1> filteredSelectOperationResults = filteredSelectOperation.selectAll();
        //logGraph(filteredSelectOperationResults, "from db");
        
        // filter1,2,3 are used to verify results
        SelectCascadeFilter<SormulaTestLevel1> filter1; 
        SelectCascadeFilter<SormulaTestLevel2> filter2;
        SelectCascadeFilter<SormulaTestLevel3> filter3;
        if (selectCascadeFilters.length == 1)
        {
            // one filter for all row types
            filter1 = (SelectCascadeFilter<SormulaTestLevel1>)selectCascadeFilters[0];
            filter2 = (SelectCascadeFilter<SormulaTestLevel2>)selectCascadeFilters[0];
            filter3 = (SelectCascadeFilter<SormulaTestLevel3>)selectCascadeFilters[0];
        }
        else
        {
            // different filters for each row type (assume order)
            filter1 = (SelectCascadeFilter<SormulaTestLevel1>)selectCascadeFilters[0];
            filter2 = (SelectCascadeFilter<SormulaTestLevel2>)selectCascadeFilters[1];
            filter3 = (SelectCascadeFilter<SormulaTestLevel3>)selectCascadeFilters[2];
        }
        
        // all selected rows should pass filter 
        // confirms that all filtered rows are permitted
        for (SormulaTestLevel1 row1 : filteredSelectOperationResults)
        {
            assert filter1.accept(null, row1, false) && filter1.accept(null, row1, true) : 
                "level 1 row should not be selected id=" + row1.getLevel1Id();

            for (SormulaTestLevel2 row2 : row1.getChildList())
            {
                assert filter2.accept(null, row2, false) && filter2.accept(null, row2, true) : 
                    "level 2 row should not be selected id=" + row2.getLevel2Id();
                
                for (SormulaTestLevel3 row3 : row2.getChildList())
                {
                    assert filter3.accept(null, row3, false) && filter3.accept(null, row3, true) : 
                        "level 3 row should not be selected id=" + row3.getLevel3Id();
                }
            }
        }

        // filter graph of all based upon test filter
        // result should be filtered1 is parallel graph to filteredSelectOperationResults
        // important: must perform depth-first since some filter tests depend upon children
        
        // filter level 1 children
        List<SormulaTestLevel1> unfiltered1 = getTable().selectAll();
        List<SormulaTestLevel1> filtered1 = new ArrayList<>(unfiltered1.size());

        for (SormulaTestLevel1 row1 : unfiltered1)
        {
            // filter level 2 children
            List<SormulaTestLevel2> unfiltered2 = row1.getChildList();
            List<SormulaTestLevel2> filtered2 = new ArrayList<>(unfiltered2.size());
            row1.setChildList(filtered2);

            for (SormulaTestLevel2 row2 : unfiltered2)
            {
                // filter level 3 children
                List<SormulaTestLevel3> unfiltered3 = row2.getChildList();
                List<SormulaTestLevel3> filtered3 = new ArrayList<>(unfiltered3.size()); 
                row2.setChildList(filtered3);
            
                for (SormulaTestLevel3 row3 : unfiltered3)
                {
                    if (filter3.accept(null, row3, false) && filter3.accept(null, row3, true))
                    {
                        filtered3.add(row3);
                    }
                }
                
                if (filter2.accept(null, row2, false) && filter2.accept(null, row2, true))
                {
                    filtered2.add(row2);
                }
            }
            
            if (filter1.accept(null, row1, false) && filter1.accept(null, row1, true))
            {
                filtered1.add(row1);
            }
        }
        //logGraph(filtered1, "created by test method");
        
        // confirm that rows from filtered1 also exist in filteredSelectOperationResults from select operation
        for (SormulaTestLevel1 f1 : filtered1)
        {
            // linear search to find f1 in filteredSelectOperationResults
            SormulaTestLevel1 filteredSelect1 = null;
            for (SormulaTestLevel1 fso1 : filteredSelectOperationResults)
            {
                if (fso1.getLevel1Id() == f1.getLevel1Id())
                {
                    filteredSelect1 = fso1;
                    break;
                }
            }
            
            // f1 should be in filtered results 
            assert filteredSelect1 != null : "level 1 row=" + f1.getLevel1Id() + " should be in filtered results";
        
            // check level 2
            for (SormulaTestLevel2 f2 : f1.childList)
            {
                // linear search to find f2 in filteredSelectOperationResults
                SormulaTestLevel2 filteredSelect2 = null;
                for (SormulaTestLevel2 fso2 : filteredSelect1.getChildList())
                {
                    if (fso2.getLevel2Id() == f2.getLevel2Id())
                    {
                        filteredSelect2 = fso2;
                        break;
                    }
                }
                
                // f2 should be in filtered results 
                assert filteredSelect2 != null : "level 2 row=" + f2.getLevel2Id() + " should be in filtered results";

                // check level 3
                for (SormulaTestLevel3 f3 : f2.childList)
                {
                    // linear search to find f3 in filteredSelectOperationResults
                    SormulaTestLevel3 filteredSelect3 = null;
                    for (SormulaTestLevel3 fso3 : filteredSelect2.getChildList())
                    {
                        if (fso3.getLevel3Id() == f3.getLevel3Id())
                        {
                            filteredSelect3 = fso3;
                            break;
                        }
                    }
                    
                    // f3 should be in filtered results
                    assert filteredSelect3 != null : "level 3 row=" + f3.getLevel3Id() + " should be in filtered results";
                }
            }
        }
    }
    
    
    protected void logGraph(List<SormulaTestLevel1> level1Rows, String message)
    {
        log.info(message);
        for (SormulaTestLevel1 row1 : level1Rows)
        {
            log.info("1 " + row1.getLevel1Id());
            
            for (SormulaTestLevel2 row2 : row1.getChildList())
            {
                log.info("2   " + row2.getLevel2Id());    
                
                for (SormulaTestLevel3 row3 : row2.getChildList())
                {
                    log.info("3     " + row3.getLevel3Id());    
                }
            }
        }
    }
}
