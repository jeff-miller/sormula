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
package org.sormula.operation.cascade;

import org.sormula.Table;
import org.sormula.annotation.cascade.InsertCascade;
import org.sormula.operation.InsertOperation;
import org.sormula.reflect.SormulaField;


/**
 * Cascade that inserts rows into target table when source operation initiates a cascade.
 * 
 * @author Jeff Miller
 *
 * @param <S> row class of table that is source of cascade
 * @param <T> row class of table that is target of cascade (typically not known at compile time)
 */
public class InsertCascadeOperation<S, T> extends ModifyCascadeOperation<S, T>
{
    /**
     * Constructor used by {@linkplain InsertOperation}.
     *  
     * @param targetField cascade insert operation uses row(s) from this field
     * @param targetTable cascade insert operation is performed on this table 
     * @param insertCascadeAnnoation cascade operation
     */
    public InsertCascadeOperation(SormulaField<S, ?> targetField, Table<T> targetTable, InsertCascade insertCascadeAnnoation)
    {
        super(targetField, targetTable, insertCascadeAnnoation.operation(), insertCascadeAnnoation.post());
    }
}
