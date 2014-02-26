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
import java.sql.Types;
import java.util.Date;

import org.sormula.Database;
import org.sormula.translator.TypeTranslator;


/**
 * Translates using {@link PreparedStatement#setTimestamp(int, java.sql.Timestamp)} and {@link ResultSet#getTimestamp(int)}
 * for java.util.Date objects. null's are preserved. This translator is available by default for 
 * all tables when {@link Database} is created.
 * 
 * @since 1.6 and 2.0
 * @author Jeff Miller
 */
public class DateTranslator implements TypeTranslator<Date>
{
    /**
     * {@inheritDoc}
     */
    public void write(PreparedStatement preparedStatement, int parameterIndex, Date parameter) throws Exception
    {
        if (parameter == null) preparedStatement.setNull(parameterIndex, Types.TIMESTAMP); 
        else                   preparedStatement.setTimestamp(parameterIndex, new java.sql.Timestamp(parameter.getTime()));
        
    }
    
    
    /**
     * {@inheritDoc}
     */
    public Date read(ResultSet resultSet, int columnIndex) throws Exception
    {
        java.sql.Timestamp sqlTimestamp = resultSet.getTimestamp(columnIndex);
        if (sqlTimestamp == null) return null;
        else                      return new java.util.Date(sqlTimestamp.getTime());
    }
}
