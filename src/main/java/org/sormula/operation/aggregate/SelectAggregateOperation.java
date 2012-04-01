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
package org.sormula.operation.aggregate;

import java.sql.ResultSet;

import org.sormula.Table;
import org.sormula.log.ClassLogger;
import org.sormula.operation.OperationException;
import org.sormula.operation.ScalarSelectOperation;
import org.sormula.reflect.ReflectException;
import org.sormula.reflect.SormulaField;
import org.sormula.translator.ColumnTranslator;


/**
 * Operation to select a value using an aggregate sql function like MIN, MAX, SUM, AVG, etc. By default,
 * operates on all rows in table. Set where condition with {@link #setWhere(String)} to limit 
 * to a subset of rows.  To get the result, use {@link #readAggregate()}.
 * <p>
 * If expression is a column, then T should be same type as column type in row class R. Column translator 
 * associated with column in row R will be used.
 * <p>
 * If expression is not a column, then T should match the expression type. Override {@link #readAggregate()}
 * to provide a customized read for expression. By default where expression is not a column name, then
 * {@link ResultSet#getObject(int)} is used. 
 * 
 * @since 1.1
 * @author Jeff Miller
 * @param <R> class type which contains members for columns of a row in a table
 * @param <T> class type of aggregate result
 */
public class SelectAggregateOperation<R, T> extends ScalarSelectOperation<R>
{
    private static final ClassLogger log = new ClassLogger(); 
    String function;
    String expression;
    ColumnTranslator<R> columnTranslator;
    SormulaField<R, T> sormulaField;
    
    
    /**
     * Constructs for standard sql select statement as:<br>
     * SELECT f(e), ... FROM table<br>
     * where f is an aggregate SQL function and e is a SQL expression (typically a column name).
     * <p>
     * Example:
     * <blockquote><pre>
     * Database database = ...
     * Table&lt;Order&gt; table = database.getTable(Order.class);
     * // find largest order
     * SelectAggregateOperation&ltOrder&gt; maxOrderOperation = 
     *     new SelectAggregateOperation&ltOrder, Double&gt;(table, "MAX", "amount");
     * maxOrderOperation.execute();    
     * double maxOrder = maxOrderOperation.readAggregate();
     * </pre></blockquote>
     * @param table select from this table
     * @param function aggregate function name like AVG, SUM, MIN, etc.
     * @param expression expression to use as parameter to function; typically it is the
     * name of a column to that aggregate function operates upon (example: SUM(amount) SUM is function and
     * amount is expression)  
     * @throws OperationException if error
     */
    public SelectAggregateOperation(Table<R> table, String function, String expression) throws OperationException
    {
        super(table, "");
        this.function = function;
        this.expression = expression;
        initBaseSql();
        
        columnTranslator = getTable().getRowTranslator().getColumnTranslator(expression);
        if (columnTranslator != null)
        {
            if (log.isDebugEnabled()) log.debug("columnTranslator.getColumnName()=" + columnTranslator.getColumnName());
            try
            {
                sormulaField = new SormulaField<>(columnTranslator.getField());
            }
            catch (ReflectException e)
            {
                throw new OperationException("error getting column translator", e);
            }
        }
    }


    /**
     * Sets base sql with {@link #setBaseSql(String)}.
     */
    protected void initBaseSql()
    {
        String tableName = getTable().getQualifiedTableName();
        StringBuilder sql = new StringBuilder(tableName.length() + 50);
        
        sql.append("SELECT ");
        sql.append(function);
        sql.append("(");
        sql.append(expression);
        sql.append(") ");
        sql.append("FROM ");
        sql.append(tableName);
        
        setBaseSql(sql.toString());
    }


    /**
     * Reads the aggregate value from the current result set.
     * 
     * @return aggregate value
     * @throws OperationException if error
     */
    public T readAggregate() throws OperationException
    {
        T result = null;
        ResultSet rs = getResultSet();
        
        try
        {
            if (rs.next())
            {
                if (columnTranslator != null)
                {
                    // read result into a row using column translator of column for row
                    R row = getTable().newRow();
                    columnTranslator.read(rs, 1, row);
                    
                    // get result from row
                    result = sormulaField.invokeGetMethod(row);
                }
                else
                {
                    // no column translator, read as object and cast to T
                    @SuppressWarnings("unchecked")
                    T temp = (T)rs.getObject(1);
                    result = temp;
                }
            }
        }
        catch (Exception e)
        {
            throw new OperationException("error reading result", e);
        }
        
        return result;
    }
}
