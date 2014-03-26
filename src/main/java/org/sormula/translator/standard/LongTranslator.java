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

import org.sormula.Database;
import org.sormula.translator.TypeTranslator;


/**
 * Translates using {@link PreparedStatement#setLong(int, long)} and {@link ResultSet#getLong(int)}.
 * This translator is available by default for all tables when {@link Database} is created.
 * 
 * @since 1.6 and 2.0
 * @author Jeff Miller
 */
public class LongTranslator implements TypeTranslator<Long>
{
    /**
     * {@inheritDoc}
     */
    public void write(PreparedStatement preparedStatement, int parameterIndex, Long parameter) throws Exception
    {
        if (parameter == null) preparedStatement.setNull(parameterIndex, Types.BIGINT);
        else                   preparedStatement.setLong(parameterIndex, parameter);
    }
    
    
    /**
     * {@inheritDoc}
     */
    public Long read(ResultSet resultSet, int columnIndex) throws Exception
    {
        Long l = resultSet.getLong(columnIndex);
        if (resultSet.wasNull()) l = null;
        return l;
    }
}
