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
package org.sormula.translator.standard;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.sormula.translator.BasicTranslator;


/**
 * Translates using a boolean using {@link PreparedStatement#setString(int, String)} and {@link ResultSet#getString(int)}.
 * "Y" is used for true and "N" is used for false.
 * 
 * @since 1.6
 * @author Jeff Miller
 */
public class BooleanYNTranslator implements BasicTranslator<Boolean>
{
    /**
     * {@inheritDoc}
     */
    public void write(PreparedStatement preparedStatement, int parameterIndex, Boolean parameter) throws Exception
    {
        preparedStatement.setString(parameterIndex, parameter ? "Y" : "N");
    }
    
    
    /**
     * {@inheritDoc}
     */
    public Boolean read(ResultSet resultSet, int columnIndex) throws Exception
    {
        String b = resultSet.getString(columnIndex);
        return b != null && b.equals("Y") ? true : false;
    }
}
