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
package org.sormula.tests.cascade.nofk;

import org.sormula.SormulaException;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.Test;


/**
 * Tests cascade inserts for {@link SormulaParentNFK}.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="cascade.insert")
public class InsertTest extends DatabaseTest<SormulaParentNFK>
{
    @Override
    protected void open() throws Exception
    {
        super.open();
        createTable(SormulaParentNFK.class, 
            "CREATE TABLE " + getSchemaPrefix() + SormulaParentNFK.class.getSimpleName() + " (" +
            " parentid INTEGER PRIMARY KEY," +
            " description VARCHAR(60)" +
            ")"
        );
        
        // create child table for 1 to n relationship
        DatabaseTest<SormulaChildNFK> childN = new DatabaseTest<>();
        childN.openDatabase();
        childN.createTable(SormulaChildNFK.class, 
                "CREATE TABLE " + getSchemaPrefix() + SormulaChildNFK.class.getSimpleName() + " (" +
                " childid INTEGER PRIMARY KEY," +
                " parentid INTEGER NOT NULL," +
                " description VARCHAR(60)" +
                ")"
            );
        childN.closeDatabase();
    }
    
    
    @Test
    public void insertOne() throws SormulaException
    {
        begin();
        SormulaParentNFK parent = new SormulaParentNFK(1, "Insert parent");
        SormulaChildNFK child = new SormulaChildNFK(12, "insert child");
        parent.add(child);
        
        assert getTable().insert(parent) == 1 : "insert parent failed";
        assert child.getParentId() != parent.getParentId() : "child parent id was set but setForeignKeyValues=false";
        
        commit();
    }
}
