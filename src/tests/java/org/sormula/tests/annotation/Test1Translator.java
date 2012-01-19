/* sormula - Simple object relational mapping
 * Copyright (C) 2011 Jeff Miller
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

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.sormula.translator.TypeTranslator;

/**
 * Converts {@link Test1} to/from database.
 * @author Jeff Miller
 */
public class Test1Translator implements TypeTranslator<Test1>
{
    public Test1 read(ResultSet resultSet, int columnIndex) throws Exception
    {
        // convert from db int to Test1
        return new Test1(resultSet.getInt(columnIndex));
    }

    public void write(PreparedStatement preparedStatement, int parameterIndex, Test1 parameter) throws Exception
    {
        // convert from Test1 to int for db
        if (parameter == null) preparedStatement.setInt(parameterIndex, 0);
        else preparedStatement.setInt(parameterIndex, parameter.intValue());
    }
}

