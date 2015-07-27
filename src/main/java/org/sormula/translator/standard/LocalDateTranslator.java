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

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

import org.sormula.Database;
import org.sormula.translator.TypeTranslator;


/**
 * Translates {@link LocalDate} using {@link PreparedStatement#setDate(int, java.sql.Date)} 
 * and {@link ResultSet#getDate(int)}. This translator is available by default for
 * all tables when {@link Database} is created.
 * 
 * @since 4.0
 * @author Jeff Miller
 */
public class LocalDateTranslator implements TypeTranslator<LocalDate>
{
    /**
     * {@inheritDoc}
     */
    public void write(PreparedStatement preparedStatement, int parameterIndex, LocalDate parameter) throws Exception
    {
    	if (parameter == null) preparedStatement.setDate(parameterIndex, null);
    	else                   preparedStatement.setDate(parameterIndex, Date.valueOf(parameter));
    }
    
    
    /**
     * {@inheritDoc}
     */
    public LocalDate read(ResultSet resultSet, int columnIndex) throws Exception
    {
    	Date date = resultSet.getDate(columnIndex); 
        if (date == null) return null;
        else              return date.toLocalDate();
    }
}
