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

import org.sormula.translator.TypeTranslator;


/**
 * {@link TypeTranslator} that translates a boolean field using 
 * {@link PreparedStatement#setString(int, String)} and {@link ResultSet#getString(int)}.
 * "Y" is used for true and "N" is used for false.
 * 
 * @since 1.9.2 and 2.3.2
 * @author Jeff Miller
 */
public class BooleanYNTranslator implements TypeTranslator<Boolean>
{
    /**
     * {@inheritDoc}
     */
    public void write(PreparedStatement preparedStatement, int parameterIndex, Boolean parameter) throws Exception
    {
        if (parameter == null) preparedStatement.setString(parameterIndex, null);
        else                   preparedStatement.setString(parameterIndex, parameter ? "Y" : "N");
    }
    
    
    /**
     * {@inheritDoc}
     */
    public Boolean read(ResultSet resultSet, int columnIndex) throws Exception
    {
        Boolean b;
        
        String yn = resultSet.getString(columnIndex);
        if (yn == null)          b = null;
        else if (yn.equals("Y")) b = true;
        else                     b = false;
        
        return b;
    }
}
