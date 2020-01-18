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
package org.sormula.tests.column.roid;

import java.util.ArrayList;

import org.sormula.SormulaException;
import org.sormula.annotation.Column;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.Test;


/**
 * Tests inserts when row has  {@link Column#identity()} and {@link Column#readOnly()}. 
 * This test must run first so that test data is inserted for select, update, and delete tests.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="column.insert")
public class InsertTest extends DatabaseTest<ColumnTestRoid>
{
    @Override
    protected void open() throws Exception
    {
        if (isTestIdentity())
        {
            super.open();
            createTable(ColumnTestRoid.class, 
                "CREATE TABLE " + getSchemaPrefix() + ColumnTestRoid.class.getSimpleName() + " (" +
                " id " + getIdentityColumnDDL() + "," +
                " description VARCHAR(30)" +
                ")"
            );
        }
    }
    
    
    @Test
    public void insertRoid() throws SormulaException
    {
        if (isTestIdentity())
        {
            begin();
            ArrayList<ColumnTestRoid> list = new ArrayList<>();
            
            for (int i = 1; i < 10; ++i)
            {
                list.add(new ColumnTestRoid(-1, "Column test " + i));
            }
            
            getTable().insertAll(list);
            
            for (ColumnTestRoid row : getTable().selectAll())
            {
                assert row.getId() != -1 : "id was not generated as identity";
            }
            
            commit();
        }
    }
}
