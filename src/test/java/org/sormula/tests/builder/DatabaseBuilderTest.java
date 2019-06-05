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

import java.sql.Connection;

import javax.sql.DataSource;

import org.sormula.Database;
import org.sormula.tests.DatabaseTest;
import org.testng.annotations.Test;


/**
 * Tests for {@link Database}.
 * 
 * @author Jeff Miller
 */
@Test(singleThreaded=true, groups="builder.database")
public class DatabaseBuilderTest extends DatabaseTest<Object>
{
    String schema = "SomeSchema";
    String dataSourceName = "SomeDataSource";
    

    @Test
    public void builderDataSourceName() throws Exception
    {
        openDatabase(dataSourceName); // creates data source with name
        
        try (Database database = Database.builder(dataSourceName).build())
        {
            assert database.getDataSourceName().equals(dataSourceName) : "data source name is incorrect";
        }
        
        try (Database database = Database.builder(dataSourceName).schema(schema).build())
        {
            assert database.getSchema().equals(schema) : "schema name is incorrect";
        }
        
        closeDatabase();
    }
    
    
    @Test
    public void builderDataSource() throws Exception
    {
        openDatabase(true); // creates test data source
        DataSource dataSource = getDataSource();
        
        try (Database database = Database.builder(dataSource).build())
        {
            assert database.getDataSource() == dataSource : "data source is incorrect";
        }
        
        try (Database database = Database.builder(dataSource).schema(schema).build())
        {
            assert database.getSchema().equals(schema) : "schema name is incorrect";
        }
        
        closeDatabase();
    }
    
    
    @Test
    public void builderConnection() throws Exception
    {
        openDatabase(); // creates connection
        Connection connection = getConnection();
        
        try (Database database = Database.builder(connection).build())
        {
            assert database.getConnection() == connection : "connection is incorrect";
        }
        
        try (Database database = Database.builder(connection).schema(schema).build())
        {
            assert database.getSchema().equals(schema) : "schema name is incorrect";
        }
        
        closeDatabase();
    }

    
    @Test
    public void builder() throws Exception
    {
        openDatabase(); // creates connection
        Connection connection = getConnection();
        
        try (Database database = Database.builder(connection).build())
        {
            assert database.isAutoGeneratedKeys() : "autoGeneratedKeys should be true by default";
        }
        
        try (Database database = Database.builder(connection).autoGeneratedKeys(false).build())
        {
            assert !database.isAutoGeneratedKeys() : "autoGeneratedKeys should be false";
        }
        
        try (Database database = Database.builder(connection).build())
        {
            assert !database.isReadOnly() : "readOnly should be false by default";
        }
        
        try (Database database = Database.builder(connection).readOnly(true).build())
        {
            assert database.isReadOnly() : "readOnly should be true";
        }
        
        try (Database database = Database.builder(connection).build())
        {
            assert !database.isTimings() : "timings should be false by default";
        }
        
        try (Database database = Database.builder(connection).timings(true).build())
        {
            assert database.isTimings() : "timings should be true";
        }
        
        closeDatabase();
    }
}
