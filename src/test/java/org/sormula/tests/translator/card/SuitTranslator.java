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
package org.sormula.tests.translator.card;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.sormula.translator.TypeTranslator;

public class SuitTranslator implements TypeTranslator<Suit>
{
    public Suit read(ResultSet resultSet, int columnIndex) throws Exception
    {
        // convert int suit id from db to Suit object
        return new Suit(resultSet.getInt(columnIndex));
    }

    public void write(PreparedStatement preparedStatement, int parameterIndex, Suit parameter) throws Exception
    {
        // convert Suit object to int suit id for db
        preparedStatement.setInt(parameterIndex, parameter.getId());
    }
}

