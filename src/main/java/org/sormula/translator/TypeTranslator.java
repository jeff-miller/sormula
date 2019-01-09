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
package org.sormula.translator;

import java.sql.PreparedStatement;
import java.sql.ResultSet;


/**
 * Interface for getting a value from a result set and setting a value
 * to a prepared statement.
 *
 * @since 1.6 and 2.0
 * @author Jeff Miller
 * @param <T> type of Java field or Java parameter (not column type)
 */
public interface TypeTranslator<T>
{
    /**
     * TODO
     * @param clazz
     * @since 4.31
     */
    default public void setClass(Class<T> clazz)
    {
    }

    
    /**
     * Reads value from result set.
     * 
     * @param resultSet read value from this result set
     * @param columnIndex read value at this column index from result set
     * @return result set value at parameter index 
     * @throws Exception if error
     */
    public T read(ResultSet resultSet, int columnIndex) throws Exception;
    
    
    /**
     * Sets parameter value in the prepared statement.
     * 
     * @param preparedStatement set column value as parameter in this statement
     * @param parameterIndex set parameter at this index
     * @param parameter value to set in prepared statement
     * @throws Exception if error
     */
    public void write(PreparedStatement preparedStatement, int parameterIndex, T parameter) throws Exception;
}
