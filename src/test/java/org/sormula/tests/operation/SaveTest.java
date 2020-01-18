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
package org.sormula.tests.operation;

import java.util.Set;

import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.Test;


/**
 * Tests all save operations.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="operation.update", dependsOnGroups="operation.insert")
public class SaveTest extends DatabaseTest<SormulaTest4>
{
    @Override
    protected void open() throws Exception
    {
        super.open();
        createTable(SormulaTest4.class);
    }
    
    
    @Test
    public void saveExisting1() throws SormulaException
    {
    	begin();
    	selectTestRows(); // must perform each time since other tests are destructive
    	
        // choose random row
        SormulaTest4 row = getRandom();

        // new values
        row.setType(555);
        row.setDescription("save existing record");
        
        assert getTable().save(row) == 1 : "save existing failed";
        
        // read row to confirm that saves applied
        SormulaTest4 row2 = getTable().select(row.getId());
        assert row2 != null && row2.getType() == row.getType() && row2.getDescription().equals(row.getDescription()) :
            " saved existing row not same";
        
        commit();
    }
    
    
    @Test
    public void saveNew1() throws SormulaException
    {
        begin();
        
        // choose random row
        SormulaTest4 row = new SormulaTest4();
        row.setId(5000);
        row.setType(555);
        row.setDescription("save new record");
        
        assert getTable().save(row) == 1 : "save new failed";
        
        // read row to confirm that saves applied
        SormulaTest4 row2 = getTable().select(row.getId());
        assert row2 != null && row2.getType() == row.getType() && row2.getDescription().equals(row.getDescription()) :
            " saved new row not same";
        
        commit();
    }
    

    @Test
    public void saveCollection() throws SormulaException
    {
    	begin();
    	selectTestRows(); // must perform each time since other tests are destructive
    	
    	// choose random set
        Set<SormulaTest4> set = getRandomSet();
        
        // modify to update
        for (SormulaTest4 row: set)
        {
            row.setType(5555);
        }
        
        // create new records
        for (int i = 5001; i < 5010; ++i)
        {
            set.add(new SormulaTest4(i, 5555, "save new collection " + i));
        }

        // save
        Table<SormulaTest4> table = getTable();
        assert table.saveAll(set) == set.size() : "save count not same as collection size";
        
        // confirm each row was saved
        for (SormulaTest4 r: set)
        {
            SormulaTest4 r2 = table.select(r.getId());
            assert r2 != null && r2.getType() == r.getType() : "save collection failed";
        }
        
        commit();
    }
}
