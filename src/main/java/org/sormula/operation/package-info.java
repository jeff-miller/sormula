/**
 * Classes that perform SQL operations such as select, update, insert, delete.
 * <p> 
 * There are three classes of operations: select operations, modifiy operations, 
 * and the "Full" operations. All select operations are derived from 
 * {@link org.sormula.operation.SelectOperation}. Insert, update, and, delete 
 * operations are derived from {@link org.sormula.operation.ModifyOperation}. All
 * classes with names begining with "Full" encapsulate common pattern of prepare, execute,
 * and close. "Full" operations are optional and are provided to reduce programming
 * needed for typical scenarios. 
 */
package org.sormula.operation;