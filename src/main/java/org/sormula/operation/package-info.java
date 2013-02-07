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


/**
 * Classes that perform SQL operations such as select, update, insert, delete.
 * <p> 
 * There are three types of operations: select operations, modifiy operations, 
 * and the "Full" operations. All select operations are derived from 
 * {@link org.sormula.operation.SelectOperation}. Insert, update, and, delete 
 * operations are derived from {@link org.sormula.operation.ModifyOperation}. 
 * <p>
 * For example:
 * <p>
 * Select all students by type 3 ("byType" is name of Where annotation on Student):
 * <blockquote><pre>
 * Database database = ...
 * Table&lt;Student&gt; table = database.getTable(Student.class);
 * List&lt;Student&gt; selectedList = new ArrayListSelect&lt;Student&gt;(table, "byType").selectAll(3);
 * </pre></blockquote>
 */
package org.sormula.operation;

