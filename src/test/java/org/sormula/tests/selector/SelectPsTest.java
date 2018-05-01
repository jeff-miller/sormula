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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sormula.SormulaException;
import org.sormula.log.ClassLogger;
import org.sormula.operation.ArrayListSelectOperation;
import org.sormula.selector.PaginatedSelector;
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
    public void selectA() throws SormulaException
    {
        // TODO test only if db property set? to allow skipping for some db's
        
    	begin();
    	//initExpectedPages(25, ""/*all*/, "orderById");
    	initExpectedPages(25, "selectByType", "orderById", 2);
    	testPages();
        commit();
    }
    
    
    protected void initExpectedPages(int rowsPerPage, String whereConditionName, String orderByName, Object...whereParameters) throws SormulaException
    {
        this.rowsPerPage = rowsPerPage;
        this.whereConditionName = whereConditionName;
        this.orderByName = orderByName;
        this.whereParameters = whereParameters;
        
        // build a map for page number to list of ids on that page
        List<SormulaPsTest> testRows = getTable().selectAllWhere(whereConditionName, whereParameters);
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
        
        // debug
        for (pageNumber = 1; pageNumber <= pageIdMap.size(); ++pageNumber)
        {
            log.info(pageNumber + " " + pageIdMap.get(pageNumber));
        }
    }
    
    
    protected void testPages() throws SormulaException
    {
        ArrayListSelectOperation<SormulaPsTest> selectOperation = new ArrayListSelectOperation<>(getTable(), whereConditionName); // TODO make class variable instead of parameter?
        selectOperation.setOrderBy(orderByName);
        selectOperation.setParameters(whereParameters);
        PaginatedSelector<SormulaPsTest, List<SormulaPsTest>> selector = new PaginatedSelector<>(selectOperation, rowsPerPage);
        int lastPage = pageIdMap.size() + 1; 

        selector.setPageNumber(1);
        testExpectedRows(selector);

        selector.setPageNumber(2);
        testExpectedRows(selector);

        selector.setPageNumber(3);
        testExpectedRows(selector);
    }
    
    
    protected <C extends Collection<SormulaPsTest>> void testExpectedRows(PaginatedSelector<SormulaPsTest, C> selector) throws SormulaException
    {
        // select rows for page
        C selectedPageRows = selector.selectPage();
        log.info("selectedPageRows.size()="+selectedPageRows.size());
        
        // get ids of page 
        List<Integer> selectedPageIds = new ArrayList<>(selectedPageRows.size());
        for (SormulaPsTest selectedRow : selectedPageRows) selectedPageIds.add(selectedRow.getId());
        
        // actual and select ids must be the same
        List<Integer> actualPageIds = pageIdMap.get(selector.getPageNumber());
        assert selectedPageIds.size() == actualPageIds.size() : 
            "selected size=" + selectedPageIds.size() + " is not the same as actual size=" + actualPageIds.size();
        
        int testPageRows = actualPageIds.size();
        for (int row = 0; row < testPageRows; ++row)
        {
            log.info((row+1) + " " + selectedPageIds.get(row) + " " + actualPageIds.get(row));
            assert selectedPageIds.get(row).equals(actualPageIds.get(row)) : 
                "row id's are not the same for page=" + selector.getPageNumber() + " row="+row;
        }
    }
}
