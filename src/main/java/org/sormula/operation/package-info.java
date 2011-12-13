/**
 * Classes that perform SQL operations such as select, update, insert, delete.
 * <p> 
 * There are three types of operations: select operations, modifiy operations, 
 * and the "Full" operations. All select operations are derived from 
 * {@link org.sormula.operation.SelectOperation}. Insert, update, and, delete 
 * operations are derived from {@link org.sormula.operation.ModifyOperation}. 
 * <p>
 * Classes with names begining with "Full" are deprecated. There are methods in
 * the other operations that are preferred alternatives to "Full" operations. 
 * 
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

