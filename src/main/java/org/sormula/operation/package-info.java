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
 * Classes that perform SQL operations such as select, update, insert, save, and delete.
 * <p> 
 * There are two types of operations: select operations and modify operations. 
 * All select operations are derived from 
 * {@link org.sormula.operation.SelectOperation}. Insert, save, update, and, delete 
 * operations are derived from {@link org.sormula.operation.ModifyOperation}. 
 * <p>
 * Use operation classes instead of {@link org.sormula.Table} methods if you
 * need to perform the same operation many times but only want to prepare it once
 * for efficiency. 
 * <p>
 * For example, select all students by type 3 and then by type 4 with the same operation.
 * "byType" is name of Where annotation on Student:
 * <blockquote><pre>
 * Database database = ...
 * Table&lt;Student&gt; table = database.getTable(Student.class);
 * ArrayListSelectOperation&lt;Student&gt op = 
 *     new ArrayListSelectOperation&lt;&gt;(table, "byType");
 * op.setParameters(3);
 * op.execute(); // JDBC prepare occurs here
 * List&lt;Student&gt; selectedList3 = op.readAll();    
 * op.setParameters(4);
 * op.execute(); // no prepare needed
 * List&lt;Student&gt; selectedList4 = op.readAll();
 * op.close();
 * </pre></blockquote>
 * To update rows (insert, save, and delete are similar):
 * <blockquote><pre>
 * Database database = ...
 * Table&lt;Student&gt; table = database.getTable(Student.class);
 * UpdateOperation&lt;Student&gt op = new UpdateOperation&lt;&gt;(table);
 * List&lt;Student&gt; list1 = ...;    
 * op.setRows(list1);
 * op.execute(); // JDBC prepare occurs here
 * List&lt;Student&gt; list2 = ...; 
 * op.setRows(list2);
 * op.execute(); // no prepare needed
 * op.close();
 * </pre></blockquote> 
 */
package org.sormula.operation;

