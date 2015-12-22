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
package org.sormula.tests;

import java.sql.Connection;

import javax.sql.DataSource;

import org.sormula.Database;
import org.sormula.SormulaException;


/**
 * A sormula {@link Database}. Allows for common attributes and behavior for most tests.
 * 
 * @author Jeff Miller
 */
//@Cached(type=ReadWriteCache.class, size=99) // use to test ReadWriteCache on all tests
//@Cached(type=ReadOnlyCache.class)  // use to test ReadOnlyCache on all tests
public class TestDatabase extends Database
{
    public TestDatabase(Connection connection, String schema)
    {
        super(connection, schema);
    }


    public TestDatabase(DataSource dataSource, String schema) throws SormulaException
    {
        super(dataSource, schema);
    }


    public TestDatabase(String dataSourceName, String schema) throws SormulaException
    {
        super(dataSourceName, schema);
    }
}

