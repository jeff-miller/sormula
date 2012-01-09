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
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


/**
 * Base class for creating custom column translators for fields stored as SQL BLOB.
 * Translates a field using {@link PreparedStatement#setBlob(int, Blob)} and 
 * {@link ResultSet#getBlob(int)}. Implement {@link #blobToField(Blob)} and {@link #fieldToBlob(Object)}
 * to create a blob column translator for type T in row type R. {@link AbstractColumnTranslator} is 
 * the base class. See BlobExample and WidgetColumnTranslator2 for example of how
 * to use this class.
 * 
 * @since 1.0
 * @author Jeff Miller
 * @param <R> row class
 * @param <T> type of field that is stored as SQL BLOB type
 */
public abstract class AbstractBlobColumnTranslator<R, T> extends AbstractColumnTranslator<R, T>
{
    /**
     * See super class constructor for description.
     */
    public AbstractBlobColumnTranslator(Field field, String columnName) throws Exception
    {
        super(field, columnName);
    }
    
    
    /**
     * {@inheritDoc}
     */
    public void write(PreparedStatement preparedStatement, int parameterIndex, R row) throws Exception
    {
        preparedStatement.setBlob(parameterIndex, fieldToBlob(getSormulaField().invokeGetMethod(row)));
    }
    
    
    /**
     * {@inheritDoc}
     */
    public void read(ResultSet resultSet, int columnIndex, R row) throws Exception
    {
        getSormulaField().invokeSetMethod(row, blobToField(resultSet.getBlob(columnIndex)));
    }
    

    /**
     * Converts a domain object to a {@link Blob} for use by {@link #write(PreparedStatement, int, Object)}.
     * Implement this method in a subclass to write type T that to database as a BLOB column type.
     *  
     * @param field domain object that is stored in database as SQL BLOB type
     * @return JDBC blob
     * @throws Exception if error
     */
    protected abstract Blob fieldToBlob(T field) throws Exception;
    
    
    /**
     * Converts a {@link Blob} object to a domain object for use by {@link #read(ResultSet, int, Object)}.
     * Implement this method in a subclass to read type T from database as a BLOB column type. 
     * 
     * @param blob JDBC blob
     * @return domain object that is stored in database as SQL BLOB type
     * @throws Exception
     */
    protected abstract T blobToField(Blob blob) throws Exception;
}
