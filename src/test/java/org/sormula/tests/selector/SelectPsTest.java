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
package org.sormula.tests.selector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sormula.SormulaException;
import org.sormula.log.ClassLogger;
import org.sormula.operation.ArrayListSelectOperation;
import org.sormula.selector.PaginatedSelector;
import org.sormula.selector.SelectorException;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests {@link PaginatedSelector}.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups={"operation.select"}, dependsOnGroups="operation.insert")
public class SelectPsTest extends DatabaseTest<SormulaPsTest>
{
	private static final ClassLogger log = new ClassLogger();
	int rowsPerPage;
	String whereConditionName;
	String orderByName;
	Object[] whereParameters;
	Map<Integer, List<Integer>> pageIdMap;
	PaginatedSelector<SormulaPsTest, List<SormulaPsTest>> selector;
	
	
    @BeforeClass
    public void setUp() throws Exception
    {
        openDatabase();
        createTable(SormulaPsTest.class);
    }
    
    
    @AfterClass
    public void tearDown() throws Exception
    {
        closeDatabase();
    }

    
    @Test
    public void selectPages() throws SormulaException
    {
        // TODO test only if db property set? to allow skipping for some db's
        int[] testRowsPerPage = {25, 31, 99};
        
        for (int rowsPerPage : testRowsPerPage)
        {
        	begin();
        	
        	initExpectedPages(rowsPerPage, ""/*all*/, "orderById");
        	testPages();
        	
            initExpectedPages(rowsPerPage, ""/*all*/, "orderByIdDescending");
            testPages();
        	
            initExpectedPages(rowsPerPage, "selectByType", "orderById", 1);
        	testPages();
        	
        	initExpectedPages(rowsPerPage, "selectByType", "orderById", 2);
        	testPages();
            
        	commit();
        }
    }
    
    
    protected void initExpectedPages(int rowsPerPage, String whereConditionName, String orderByName, Object...whereParameters) throws SormulaException
    {
        this.rowsPerPage = rowsPerPage;
        this.whereConditionName = whereConditionName;
        this.orderByName = orderByName;
        this.whereParameters = whereParameters;
        
        // build a map for page number to list of ids on that page
        List<SormulaPsTest> testRows = getTable().selectAllWhereOrdered(whereConditionName, orderByName, whereParameters);
        pageIdMap = new HashMap<>(testRows.size() / rowsPerPage * 2);
        int pageNumber = 1;
        int pageRow = 1;
        List<Integer> pageIdList = new ArrayList<>(rowsPerPage);
        pageIdMap.put(pageNumber, pageIdList);
        
        for (SormulaPsTest row : testRows)
        {
            if (pageRow > rowsPerPage)
            {
                // start new page
                ++pageNumber;
                pageRow = 1;
                pageIdList = new ArrayList<>(rowsPerPage);
                pageIdMap.put(pageNumber, pageIdList);
            }
            
            pageIdList.add(row.getId());
            ++pageRow;
        }
        
        if (log.isDebugEnabled())
        {
            for (pageNumber = 1; pageNumber <= pageIdMap.size(); ++pageNumber)
            {
                log.debug(pageNumber + " " + pageIdMap.get(pageNumber));
            }
        }
    }
    
    
    protected void testPages() throws SormulaException
    {
        ArrayListSelectOperation<SormulaPsTest> selectOperation = new ArrayListSelectOperation<>(getTable(), whereConditionName);
        selectOperation.setOrderBy(orderByName);
        selectOperation.setParameters(whereParameters);
        selector = new PaginatedSelector<>(selectOperation, rowsPerPage);
        int lastPage = pageIdMap.size() + 1; 

        int testPage;
        
        testPage = lastPage * 2 / 3;
        if (testPage >= 1)
        {
            selector.setPageNumber(testPage);
            testExpectedRows();
    
            if (testPage > 1)
            {
                selector.previousPage();
                testExpectedRows();
            }
        }
        
        testPage = lastPage / 3;
        if (testPage >= 1)
        {
            selector.setPageNumber(testPage);
            testExpectedRows();
            
            selector.nextPage();
            testExpectedRows();
        }
        
        // test outside of page boundaries
        try
        {
            selector.setPageNumber(-1);
            throw new SormulaException("negative page number");
        }
        catch (SelectorException e)
        {
            // expected
        }
        selector.setPageNumber(lastPage + 1);
        testExpectedRows();
    }
    
    
    protected void testExpectedRows() throws SormulaException
    {
        if (log.isDebugEnabled()) log.debug("page=" + selector.getPageNumber());
        
        // select rows for page
        List<SormulaPsTest> selectedPageRows = selector.selectPage();
        if (log.isDebugEnabled()) log.debug("selectedPageRows.size()="+selectedPageRows.size());
        
        // get ids of page 
        List<Integer> selectedPageIds = new ArrayList<>(selectedPageRows.size());
        for (SormulaPsTest selectedRow : selectedPageRows) selectedPageIds.add(selectedRow.getId());
        
        // actual and select ids must be the same
        List<Integer> actualPageIds = getExpectedRows(selector.getPageNumber());
        assert selectedPageIds.size() == actualPageIds.size() : 
            "selected size=" + selectedPageIds.size() + " is not the same as actual size=" + actualPageIds.size();
        
        int testPageRows = actualPageIds.size();
        for (int i = 0; i < testPageRows; ++i)
        {
            if (log.isDebugEnabled()) log.debug("row=" + (i + 1) + " " + selectedPageIds.get(i) + " " + actualPageIds.get(i));
            assert selectedPageIds.get(i).equals(actualPageIds.get(i)) : 
                "row id's are not the same for page=" + selector.getPageNumber() + " row=" + (i + 1);
        }
    }
    
    
    protected List<Integer> getExpectedRows(int pageNumber)
    {
        List<Integer> actualPageIds = pageIdMap.get(pageNumber);
        if (actualPageIds == null) return Collections.emptyList();
        return actualPageIds;
    }
}
