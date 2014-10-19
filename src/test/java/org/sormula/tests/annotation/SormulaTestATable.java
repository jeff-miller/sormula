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
package org.sormula.tests.annotation;

import org.sormula.Database;
import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.annotation.ExplicitType;
import org.sormula.annotation.ExplicitTypes;
import org.sormula.annotation.OrderBy;
import org.sormula.annotation.OrderBys;
import org.sormula.annotation.Row;
import org.sormula.annotation.UnusedColumn;
import org.sormula.annotation.UnusedColumns;
import org.sormula.annotation.Where;
import org.sormula.annotation.WhereField;
import org.sormula.annotation.Wheres;


/**
 * Used by tests in this package in place of Table<SormulaTestA>. All annotations are
 * added to this class as alternative to row or operation class.
 * 
 * @author Jeff Miller
 */
@Row(tableName="STA")
@Where(name="byType", fieldNames="type")
@Wheres({
    @Where(name="idIn",  whereFields=@WhereField(name="id", comparisonOperator="in")),
    @Where(name="idIn2", whereFields=@WhereField(name="id", comparisonOperator="in", operand="(6001, 6002)"))
})
@OrderBy(name="ob1", ascending="type")
@OrderBys({
    @OrderBy(name="ob2", ascending="description")
})
@UnusedColumns({ 
    @UnusedColumn(name="unusedInt", value="123")
})
@ExplicitType(type=Test1.class, translator=Test1Translator.class)
@ExplicitTypes({
    @ExplicitType(type=Test2.class, translator=Test2Translator.class)
})
public class SormulaTestATable extends Table<SormulaTestA>
{
    public SormulaTestATable(Database database, Class<SormulaTestA> rowClass) throws SormulaException
    {
        super(database, rowClass);
    }
}
