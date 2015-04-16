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
package org.sormula.examples.blob;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.sql.rowset.serial.SerialBlob;

import org.sormula.reflect.RowField;
import org.sormula.translator.AbstractBlobColumnTranslator;


/**
 * Translates a {@link Widget} field using {@link PreparedStatement#setBlob(int, Blob)} and 
 * {@link ResultSet#getBlob(int)}. {@link AbstractBlobColumnTranslator} is the base class.
 */
public class WidgetColumnTranslator2<R> extends AbstractBlobColumnTranslator<R, Widget>
{
    public WidgetColumnTranslator2(RowField<R, Widget> rowField, String columnName) throws Exception
    {
        super(rowField, columnName);
    }
    
    
    protected Blob fieldToBlob(Widget widget) throws Exception
    {
        // convert from domain object to bytes
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(1000))
        {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(widget);
            
            // convert bytes to jdbc blob
            return new SerialBlob(bos.toByteArray());
        }
    }
    
    
    protected Widget blobToField(Blob blob) throws Exception
    {
        // convert from jdbc blob to bytes to domain object
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(blob.getBytes(1, (int)blob.length()))))
        {
            return (Widget)ois.readObject();
        }
    }
}
