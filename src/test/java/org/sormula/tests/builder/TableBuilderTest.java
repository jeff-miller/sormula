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
package org.sormula.tests.builder;

import java.util.Arrays;

import org.sormula.Database;
import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.builder.TableBuilder;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.Test;


/**
 * Tests for {@link TableBuilder}.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="builder.table")
public class TableBuilderTest extends DatabaseTest<BuilderTestRow>
{
    @Override
    protected void open() throws Exception
    {
        super.open();
        
        createTable(BuilderTestRow.class, 
            "CREATE TABLE " + getSchemaPrefix() + BuilderTestRow.class.getSimpleName() + " (" +
            " id INTEGER NOT NULL PRIMARY KEY," +
            " type SMALLINT," +
            " description VARCHAR(30)" +
            ")"
        );
    }
    
    
    @Test
    public void builderAutoGeneratedKeys() throws SormulaException
    {
        Database database = getDatabase();
        Table<BuilderTestRow> table;
        
        table = Table.builder(database, BuilderTestRow.class).build();
        assert table.isAutoGeneratedKeys() == database.isAutoGeneratedKeys() : "autoGeneratedKeys should default to database";
        
        table = Table.builder(database, BuilderTestRow.class).autoGeneratedKeys(false).build();
        assert table.isAutoGeneratedKeys() == false : "autoGeneratedKeys should be false";
        
        table = Table.builder(database, BuilderTestRow.class).autoGeneratedKeys(true).build();
        assert table.isAutoGeneratedKeys() == true : "autoGeneratedKeys should be true";
    }
    
    
    @Test
    public void builderReadOnly() throws SormulaException
    {
        Database database = getDatabase();
        Table<BuilderTestRow> table;
        
        table = Table.builder(database, BuilderTestRow.class).readOnly(false).build();
        assert table.isReadOnly() == false : "readOnly should be false";
        
        table = Table.builder(database, BuilderTestRow.class).readOnly(true).build();
        assert table.isReadOnly() == true : "readOnly should be true";
    }
    
    
    @Test
    public void builderRequiredCascades() throws SormulaException
    {
        Database database = getDatabase();
        Table<BuilderTestRow> table;
        
        String[] required = {"abc", "x"};
        table = Table.builder(database, BuilderTestRow.class).requiredCascades(required[0], required[1]).build();
        assert Arrays.deepEquals(table.getRequiredCascades(), required) : "requiredCascades are not set properly";
    }
    
    
    @Test
    public void builderTableName() throws SormulaException
    {
        Database database = getDatabase();
        Table<BuilderTestRow> table;
        
        String overrideTableName = "qwerty";
        table = Table.builder(database, BuilderTestRow.class).tableName(overrideTableName).build();
        assert table.getTableName().equals(overrideTableName) : "table name not set properly";
    }
}
