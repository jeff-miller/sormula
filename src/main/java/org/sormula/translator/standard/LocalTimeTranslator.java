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
package org.sormula.translator.standard;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Time;
import java.time.LocalTime;

import org.sormula.Database;
import org.sormula.translator.TypeTranslator;


/**
 * Translates {@link LocalTime} using {@link PreparedStatement#setTime(int, java.sql.Time)} 
 * and {@link ResultSet#getTime(int)}. This translator is available by default for
 * all tables when {@link Database} is created.
 * <p>
 * Note that standard SQL TIME data type does not store nanosecond.
 * 
 * @since 4.0
 * @author Jeff Miller
 */
public class LocalTimeTranslator implements TypeTranslator<LocalTime>
{
    /**
     * {@inheritDoc}
     */
    public void write(PreparedStatement preparedStatement, int parameterIndex, LocalTime parameter) throws Exception
    {
    	if (parameter == null) preparedStatement.setTime(parameterIndex, null);
    	else                   preparedStatement.setTime(parameterIndex, Time.valueOf(parameter));
    }
    
    
    /**
     * {@inheritDoc}
     */
    public LocalTime read(ResultSet resultSet, int columnIndex) throws Exception
    {
        Time time = resultSet.getTime(columnIndex);
        if (time == null) return null;
        else              return time.toLocalTime();
    }
}
