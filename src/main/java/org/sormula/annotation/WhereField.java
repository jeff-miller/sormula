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
package org.sormula.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;

import org.sormula.operation.ScalarSelectOperation;
import org.sormula.operation.SqlOperation;


/**
 * Defines a field within a {@linkplain Where} annotation. 
 * 
 * @since 1.0
 * @author Jeff Miller
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface WhereField
{
    /**
     * SQL boolean operator to use preceding this field. Possible operators are "AND", "OR", 
     * "AND NOT", "OR NOT", "NOT", etc. Operator has no affect for first field in where condition.
     *  
     * @return Boolean opearator to be used before "column-name booleanOperator ?"  
     */
    String booleanOperator() default "AND";

    
    /**
     * Name of row member involved in where condition.
     * 
     * @return field name to use
     */
    String name();
    
    
    /**
     * SQL comparison operator to use in condition "column-name comparisonOperator ?". Example
     * operators are: "=", "<", "<=", ">", ">=", "LIKE", "<>", "IN", "NOT IN". For "IS NULL" use
     * operator="IS" and operand="NULL".
     * <p>
     * When comparison operator is "IN" then the condition used is 
     * "column-name IN (?, ?, ...)" and the corresponding parameter supplied by
     * {@link SqlOperation#setParameters(Object...)} may be a {@link Collection}. The 
     * number of ? parameter placeholders within the parentheses will be equal
     * to the size of the collection.  
     * <p>
     * Any {@link SqlOperation} that uses a {@link WhereField} with a comparison operator of "IN"
     * will be prepared each time it is executed, {@link SqlOperation#execute()} since the
     * number of parameters within the IN phrase may be different from previous execution.
     * <p>
     * "IN" operator may be used with with other operators within the same {@link Where} 
     * annotation. Multiple "IN" operators may be used within same {@link Where} annotation.
     *  
     * @return sql comparison operator to use between column name and parameter
     */
    String comparisonOperator() default "=";

    
    /**
     * SQL to use as operand following comparison operator. Operand will be appended to SQL 
     * following the comparison operator. Operand will be used "as is" and must be valid SQL. An
     * operand of "?" is used to indicate that the parameter will be supplied at runtime.
     * <p>
     * Operand can be a constant, for example:<br>
     * {@code @Where(name="hasInventory", whereFields=@WhereField(name="quantity", comparisonOperator=">", operand="0")) }
     * <p>
     * If operand is "?", then the parameter for the field will be obtained 
     * from row object as set with {@link ScalarSelectOperation#setRowParameters(Object)} or
     * parameter set with {@link SqlOperation#setParameters(Object...)}.
     * 
     * @return SQL operand to use as where parameter 
     * @since 1.4
     */
    String operand() default "?";
}
