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
package org.sormula.translator;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


/**
 * Interface of minimal behavior needed for a column translator. Interface provides
 * methods for reading/writing values from/to table column.
 *
 * @since 1.0
 * @author Jeff Miller
 * @param <R> class type for a row object
 */
public interface ColumnTranslator<R>
{
    /**
     * @return field within row class that corresponds to column
     */
    public Field getField();
    
    
    /**
     * @return name of table column to be translated
     */
    public String getColumnName();
    
    
    /**
     * Reads column value from result set and sets in row object.
     * 
     * @param resultSet read column from this result set
     * @param parameterIndex read value at this index from result set 
     * @param row set value in this row
     * @throws Exception if error
     */
    public void read(ResultSet resultSet, int parameterIndex, R row) throws Exception;
    
    
    /**
     * Gets column value from row and sets as parameter in prepared statement.
     * 
     * @param preparedStatement set column value as parameter in this statement
     * @param parameterIndex set parameter at this index
     * @param row get value from this row
     * @throws Exception if error
     */
    public void write(PreparedStatement preparedStatement, int parameterIndex, R row) throws Exception;
}
