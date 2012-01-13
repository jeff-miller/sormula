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

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.sormula.translator.AbstractColumnTranslator;


/**
 * No longer used by {@link StandardColumnTranslator}.
 * Translates a boolean field using {@link PreparedStatement#setBigDecimal(int, BigDecimal)} and {@link ResultSet#getBigDecimal(int)}.
 * 
 * @since 1.0
 * @author Jeff Miller
 */
@Deprecated
public class BigDecimalColumnTranslator<R> extends AbstractColumnTranslator<R, BigDecimal>
{
	/**
	 * See super class constructor for description.
	 */
    public BigDecimalColumnTranslator(Field field, String columnName) throws Exception
    {
        super(field, columnName);
    }
    
    
    /**
     * {@inheritDoc}
     */
    public void write(PreparedStatement preparedStatement, int parameterIndex, R row) throws Exception
    {
        preparedStatement.setBigDecimal(parameterIndex, getSormulaField().invokeGetMethod(row));
    }
    
    
    /**
     * {@inheritDoc}
     */
    public void read(ResultSet resultSet, int columnIndex, R row) throws Exception
    {
        getSormulaField().invokeSetMethod(row, resultSet.getBigDecimal(columnIndex));
    }
}
