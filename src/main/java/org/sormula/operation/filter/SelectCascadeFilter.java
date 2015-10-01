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
package org.sormula.operation.filter;

import org.sormula.operation.ScalarSelectOperation;
import org.sormula.operation.SelectOperation;


/**
 * Filter to use on selected rows.
 * 
 * {@link #accept(ScalarSelectOperation, Object, boolean)} will be invoked when row is read
 * from database or cache prior to any cascades (cascaded parameter will be false). If the first invocation returns
 * true and row has cascades defined, then accept will be invoked a second time after all cascades
 * have been completed (cascaded parameter will be true).
 * <p>
 * The first invocation of accept method (cascade parameter of false), allows you to filter based upon
 * row values and parent values. Returning false allows you to short-circuit cascading and eliminate
 * and sub nodes that are not desired.
 * <p>
 * The second invocation of accept method (cascade parameter of true), allows you to filter based upon
 * child node values.
 * 
 * @deprecated Use {@link SelectOperation#addFilter(Class, java.util.function.BiPredicate)}
 * @since 3.1
 * @author Jeff Miller
 * @param <R> class of row to be filtered
 */
@Deprecated
public interface SelectCascadeFilter<R>
{
   /**
    * Tests if row is to be used in results.
    * 
    * @param source operation or subclass that read row
    * @param row row to be tested
    * @param cascadesCompleted true if all row cascades have completed
    * @return true if row is to be included in results; false to ignore row
    */
   public boolean accept(ScalarSelectOperation<R> source, R row, boolean cascadesCompleted);
   
   
   /**
    * Return the runtime type of row that should be filtered by this filter.
    * 
    * @return row class that should be filtered by this filter; Object.class to use this filter for all row types
    */
   public Class<R> getRowClass();
}
