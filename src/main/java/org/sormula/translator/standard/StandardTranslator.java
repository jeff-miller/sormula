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
 * Marker so that it can be default for Column annotation. Means to get
 * BasicTranslator from table and construct DefaultColumnTranslator.
 * TODO
 * Delegates to translator based upon field type. This translator should work for all
 * standard Java data types. 
 * 
 * @since 1.6
 * @author Jeff Miller
 * @param <T> needed?
 */
public class StandardTranslator<T> implements BasicTranslator<T>
{
    public T read(ResultSet resultSet, int columnIndex) throws Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void write(PreparedStatement preparedStatement, int parameterIndex, T parameter) throws Exception
    {
        // TODO Auto-generated method stub
    }
}
