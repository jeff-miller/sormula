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
package org.sormula.tests.translator;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;

import org.sormula.translator.standard.EnumTranslator;


/**
 * Simplistic translator that stores ordinal instead of Enum names 
 * 
 * @author Jeff Miller
 */
public class CustomEnumTranslator extends EnumTranslator<EnumField>
{
    @Override
    public void write(PreparedStatement preparedStatement, int parameterIndex, EnumField parameter) throws Exception
    {
        if (parameter != null) preparedStatement.setShort(parameterIndex, (short)parameter.ordinal());
        else                   preparedStatement.setNull(parameterIndex, Types.SMALLINT);
    }

    @Override
    public EnumField read(ResultSet resultSet, int columnIndex) throws Exception
    {
        EnumField result = null;
        short ordinal = resultSet.getShort(columnIndex);
        
        if (!resultSet.wasNull())
        {
            EnumField[] enums = getEnumClass().getEnumConstants();

            if (ordinal >= 0 && ordinal < enums.length)
            {
                // valid ordinal
                result = enums[ordinal];
            }
            else
            {
                // ordinal was not valid for enum
                result = getDefaultEnum();
            }
        }
        
        return result; 
    }
}
