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

import java.util.Collection;
import java.util.Map;

import org.sormula.Table;
import org.sormula.log.ClassLogger;
import org.sormula.operation.ModifyOperation;
import org.sormula.operation.OperationException;
import org.sormula.reflect.ReflectException;
import org.sormula.reflect.RowField;
import org.sormula.reflect.SormulaField;


/**
 * Base class for cascades that modify database. 
 * 
 * @author Jeff Miller
 *
 * @param <S> row class of table that is source of cascade
 * @param <T> row class of table that is target of cascade
 */
public abstract class ModifyCascadeOperation<S, T> extends CascadeOperation<S, T>
{
    private static final ClassLogger log = new ClassLogger();
    ModifyOperation<T> modifyOperation;
    
    
    /**
     * Constructs.
     * 
     * @param sourceTable cascade originates on row from this table
     * @param targetField cascade operation uses row(s) from this field
     * @param targetTable cascade operation is performed on this table 
     * @param cascadeOperationClass class of cascade operation
     * false if cascade is performed before row execute (see {@link ModifyOperation#preExecute}
     * @since 3.0
     */
    @Deprecated
    public ModifyCascadeOperation(Table<S> sourceTable, SormulaField<S, ?> targetField, Table<T> targetTable, 
            Class <?> cascadeOperationClass)
    {
        super(sourceTable, targetField, targetTable, cascadeOperationClass);
    }
    
    
    /**
     * Constructs.
     * 
     * @param sourceTable cascade originates on row from this table
     * @param targetField cascade operation uses row(s) from this field
     * @param targetTable cascade operation is performed on this table 
     * @param cascadeOperationClass class of cascade operation
     * false if cascade is performed before row execute (see {@link ModifyOperation#preExecute}
     * @since 3.4
     */
    public ModifyCascadeOperation(Table<S> sourceTable, RowField<S, ?> targetField, Table<T> targetTable, 
            Class <?> cascadeOperationClass)
    {
        super(sourceTable, targetField, targetTable, cascadeOperationClass);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void cascade(S sourceRow) throws OperationException
    {
        super.cascade(sourceRow);
        RowField<S, ?> tf = (RowField<S, ?>)getTargetField();
        
        try
        {
            if (log.isDebugEnabled()) log.debug("cascade() " + tf.getField());
            Object value = tf.get(sourceRow);
            
            if (value != null)
            {
                if (tf.isScalar())
                {
                    // non collection/map type
                    @SuppressWarnings("unchecked") // target field type is not known at compile time
                    T row = (T)value;
                    modifyOperation.setRow(row);
                }
                else if (tf.isArray())
                {
                    // array
                    @SuppressWarnings("unchecked") // target field type is not known at compile time
                    T[] array = (T[])value;
                    modifyOperation.setRows(array);
                }
                else if (tf.isCollection())
                {
                    // collection
                    @SuppressWarnings("unchecked") // target field type is not known at compile time
                    Collection<T> collection = (Collection<T>)value;
                    modifyOperation.setRows(collection);
                }
                else if (tf.isMap())
                {
                    // collection
                    @SuppressWarnings("unchecked") // target field type is not known at compile time
                    Map<?, T> map = (Map<?, T>)value;
                    modifyOperation.setRows(map);
                }
                else
                {
                    throw new OperationException("unknown operation type for target field " + tf.getField().getType()); 
                }
            
                setForeignKeyValues(modifyOperation.getRows());
                setForeignKeyReference(modifyOperation.getRows());
                modifyOperation.execute();
            }
            else
            {
                if (log.isDebugEnabled()) log.debug("value is null, nothing to cascade");
            }
        }
        catch (ReflectException e)
        {
            throw new OperationException("error getting value from " + tf, e);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked") // type controlled by annotations 
    public void prepare() throws OperationException
    {
        super.prepare();
        modifyOperation = (ModifyOperation<T>)createOperation();
        modifyOperation.setNamedParameterMap(getNamedParameterMap()); // from source
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws OperationException
    {
        modifyOperation.close();
    }
}
