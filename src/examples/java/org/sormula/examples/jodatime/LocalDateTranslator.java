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
package org.sormula.examples.jodatime;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import org.joda.time.LocalDate;
import org.sormula.translator.TypeTranslator;


/**
 * Converts Joda Time {@link LocalDate} to/from database TIMESTAMP column.
 * @author Jeff Miller
 */
public class LocalDateTranslator implements TypeTranslator<LocalDate>
{
    public LocalDate read(ResultSet resultSet, int columnIndex) throws Exception
    {
        // convert from TIMESTAMP to LocalDate
        Timestamp timestamp = resultSet.getTimestamp(columnIndex);
        
        if (timestamp == null)
        {
            return null;
        }
        else
        {
            return new LocalDate(timestamp.getTime());
        }
    }

    public void write(PreparedStatement preparedStatement, int parameterIndex, LocalDate parameter) throws Exception
    {
        // convert from LocalDate to TIMESTAMP
        if (parameter == null)
        {
            preparedStatement.setTimestamp(parameterIndex, null);
        }
        else
        {
            preparedStatement.setTimestamp(parameterIndex, 
                new Timestamp(parameter.toDateMidnight().getMillis()));
        }
    }
}

