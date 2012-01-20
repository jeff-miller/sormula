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
package org.sormula.tests.annotation;

import java.sql.Connection;

import org.sormula.Database;
import org.sormula.SormulaException;
import org.sormula.annotation.ExplicitType;
import org.sormula.annotation.ExplicitTypes;

/**
 * Tests type annotations on database class.
 * 
 * @author Jeff Miller
 */
@ExplicitTypes({
    @ExplicitType(type=Test3.class, translator=Test3Translator.class)
})
public class TestDB extends Database
{
    public TestDB(Connection connection, String schema) throws SormulaException
    {
        super(connection, schema);
        
        // SormulaTestATable is used for tables usinng row SormulaTestA.class
        addTable(new SormulaTestATable(this, SormulaTestA.class));
    }
}

