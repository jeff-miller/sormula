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

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.sormula.annotation.Column;
import org.sormula.reflect.RowField;
import org.sormula.translator.AbstractColumnTranslator;


/**
 * Translates a boolean field using {@link PreparedStatement#setString(int, String)} and {@link ResultSet#getString(int)}.
 * "Y" is used for true and "N" is used for false.
 * <p>
 * Use this to override the standard translator attribute, translator in {@link Column} annotation:
 * <blockquote><pre>
 * &#64;Column(translator=BooleanYNColumnTranslator.class) 
 * boolean someBoolean;
 * </pre></blockquote>
 * 
 * An alternative is to use {@link BooleanYNTranslator}:
 * <blockquote><pre>
 * &#64;ImplicitType(translator=BooleanYNTranslator.class) 
 * boolean someBoolean;
 * </pre></blockquote>
 * 
 * @since 1.0
 * @author Jeff Miller
 */
public class BooleanYNColumnTranslator<R> extends AbstractColumnTranslator<R, Boolean>
{
	/**
	 * {@inheritDoc}
	 */
    @Deprecated
    public BooleanYNColumnTranslator(Field field, String columnName) throws Exception
    {
        super(field, columnName);
    }
    
    
    /**
     * {@inheritDoc}
     */
    public BooleanYNColumnTranslator(RowField<R, Boolean> rowField, String columnName) throws Exception
    {
        super(rowField, columnName);
    }
    
    
    /**
     * {@inheritDoc}
     */
    public void write(PreparedStatement preparedStatement, int parameterIndex, R row) throws Exception
    {
        Boolean b = getSormulaField().invokeGetMethod(row);
        if (b == null) preparedStatement.setString(parameterIndex, null);
        else           preparedStatement.setString(parameterIndex, b ? "Y" : "N");
    }
    
    
    /**
     * {@inheritDoc}
     */
    public void read(ResultSet resultSet, int columnIndex, R row) throws Exception
    {
        Boolean b;
        
        String yn = resultSet.getString(columnIndex);
        if (yn == null)          b = null;
        else if (yn.equals("Y")) b = true;
        else                     b = false;
        
        getSormulaField().invokeSetMethod(row, b);
    }
}
