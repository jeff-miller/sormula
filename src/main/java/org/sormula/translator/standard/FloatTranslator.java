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
 * Translates using {@link PreparedStatement#setFloat(int, float)} and {@link ResultSet#getFloat(int)}.
 * This translator is available by default for all tables when {@link Database} is created.
 * 
 * @since 1.6 and 2.0
 * @author Jeff Miller
 */
public class FloatTranslator implements TypeTranslator<Float>
{
    /**
     * {@inheritDoc}
     */
    public void write(PreparedStatement preparedStatement, int parameterIndex, Float parameter) throws Exception
    {
        if (parameter == null) preparedStatement.setNull(parameterIndex, Types.FLOAT);
        else                   preparedStatement.setFloat(parameterIndex, parameter);
    }
    
    
    /**
     * {@inheritDoc}
     */
    public Float read(ResultSet resultSet, int columnIndex) throws Exception
    {
        Float f = resultSet.getFloat(columnIndex);
        if (resultSet.wasNull()) f = null;
        return f;
    }
}
