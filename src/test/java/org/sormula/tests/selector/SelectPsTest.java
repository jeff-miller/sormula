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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sormula.SormulaException;
import org.sormula.log.ClassLogger;
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
	String whereCondition;
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
    
    
    protected void initExpectedPages(int rowsPerPage, String whereConditionName, String orderByName, Object...whereParameters) throws SormulaException
    {
        this.rowsPerPage = rowsPerPage;
        this.whereCondition = whereConditionName;
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
        
        for (pageNumber = 1; pageNumber <= pageIdMap.size(); ++pageNumber)
        {
            log.info(pageNumber + " " + pageIdMap.get(pageNumber));
        }
    }

    
    @Test
    public void selectA() throws SormulaException
    {
    	begin();
    	initExpectedPages(25, ""/*all*/, "orderById");
        commit();
    }
}
