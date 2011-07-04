/**
 * Classes that perform SQL operations such as select, update, insert, delete.
 * <p> 
 * There are three classes of operations: select operations, modifiy operations, 
 * and the "Full" operations. All select operations are derived from 
 * {@link org.sormula.operation.SelectOperation}. Insert, update, and, delete 
 * operations are derived from {@link org.sormula.operation.ModifyOperation}. 
 * <p>
 * Classes with names begining with "Full" encapsulate common pattern of prepare, execute,
 * and close. "Full" operations are optional and are provided to reduce programming
 * needed for typical scenarios. The most useful are {@link org.sormula.operation.FullListSelect},
 * {@link org.sormula.operation.FullScalarSelect}. 
 * For example:
 * <p>
 * Select all students by type 3 ("byType" is name of Where annotation on Student):
 * <blockquote><pre>
 * Database database = ...
 * Table&lt;Student&gt; table = database.getTable(Student.class);
 * List&lt;Student&gt; selectedList = new FullListSelect&lt;Student&gt;(table, "byType").executeAll(3);
 * </pre></blockquote>
 */
package org.sormula.operation;

