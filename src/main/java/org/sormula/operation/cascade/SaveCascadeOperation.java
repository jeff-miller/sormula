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
package org.sormula.operation.cascade;

import org.sormula.Table;
import org.sormula.annotation.cascade.SaveCascade;
import org.sormula.operation.OperationException;
import org.sormula.operation.SaveOperation;
import org.sormula.operation.SqlOperation;
import org.sormula.reflect.RowField;


/**
 * Cascade that saves rows in target table when source operation initiates a cascade.
 * 
 * @author Jeff Miller
 *
 * @param <S> row class of table that is source of cascade
 * @param <T> row class of table that is target of cascade
 * @since 1.9.2 and 2.3.2
 */
public class SaveCascadeOperation<S, T> extends ModifyCascadeOperation<S, T>
{
    /**
     * Constructor used by {@link SaveOperation}.
     * 
     * @param sourceOperation cascade originates on row from this table
     * @param targetField cascade save operation uses row(s) from this field
     * @param targetTable cascade save operation is performed on this table 
     * @param saveCascadeAnnotation cascade operation
     * @since 4.1
     */
    public SaveCascadeOperation(SaveOperation<S> sourceOperation, RowField<S, ?> targetField, Table<T> targetTable, SaveCascade saveCascadeAnnotation)
    {
        super(sourceOperation, targetField, targetTable, saveCascadeAnnotation.operation());
        setPost(saveCascadeAnnotation.post());
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    protected SqlOperation<?> createOperation() throws OperationException
    {
        SqlOperation<?> modifyOperation = super.createOperation();
        
        // updates default to primary key
        modifyOperation.setWhere("primaryKey");
        
        return modifyOperation;
    }
}
