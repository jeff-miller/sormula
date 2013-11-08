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
package org.sormula.active.operation;

import org.sormula.Table;
import org.sormula.active.ActiveRecord;
import org.sormula.active.ActiveTable;
import org.sormula.annotation.cascade.SelectCascade;
import org.sormula.annotation.cascade.SelectCascadeAnnotationReader;
import org.sormula.operation.cascade.SelectCascadeOperation;
import org.sormula.reflect.SormulaField;


/**
 * Performs lazy loading of active records. Used by {@link ActiveRecord#checkLazySelects(String)}.
 * 
 * @author Jeff Miller
 * @since 1.8 and 2.2
 * @param <R> record type
 */
public class ActiveLazySelector<R extends ActiveRecord<? super R>> extends ActiveOperation<R, Void>
{
    R sourceActiveRecord;    
    SelectCascadeAnnotationReader scar;
    
    
    /**
     * Constructs for the source and annotation reader.
     * 
     * @param sourceActiveTable parent table of 1-to-1 or 1-to-n relationship; active database is obtained from this table
     * @param sourceActiveRecord parent record that gets modified with selected child(ren)
     * @param scar annotation reader for field within parent that has lazy cascade defined
     */
    public ActiveLazySelector(ActiveTable<R> sourceActiveTable, R sourceActiveRecord, SelectCascadeAnnotationReader scar)
    {
        super(sourceActiveTable, "error performing select cascade for active record");
        this.sourceActiveRecord = sourceActiveRecord;
        this.scar = scar;
    }

    
    @Override
    public Void operate() throws Exception
    {
        // note: target table is table for field which is NOT same as getTable() which is for source record
        Table<?> targetTable = getOperationDatabase().getTable(scar.getTargetClass());
        SormulaField<R, ?> targetField = new SormulaField<>(scar.getSource());
        SelectCascade[] selectCascades = scar.getSelectCascades();
        
        // field has select cascade annotation(s)
        for (SelectCascade c: selectCascades)
        {
            if (c.lazy())
            {
                try (@SuppressWarnings("unchecked") // target field type is not known at compile time
                     SelectCascadeOperation<R, ?> operation = new SelectCascadeOperation(getTable(), targetField, targetTable, c))
                {
                    // does it make sense to allow filters for lazy selects since filter must be specified when ActiveRecord#checkLazySelects is invoked?
                    // operation.setSelectCascadeFilters(selectCascadeFilters);
                    
                    if (c.setForeignKeyValues()) operation.setForeignKeyFieldNames(scar.getForeignKeyValueFields());
                    if (c.setForeignKeyReference()) operation.setForeignKeyReferenceFieldName(scar.getForeignKeyReferenceField());
                    operation.prepare();
                    operation.cascade(sourceActiveRecord);
                }
            }
        }
        
        return null;
    }
}
