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
package org.sormula.operation;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.sormula.Table;


/**
 * SQL select count of records operation. By default selects count of all rows
 * in table. Set where condition with {@link #setWhere(String)} to count subset of all rows.
 * <p>
 * This class remains in this package for backward compatibility. org.sormula.operation.aggregate
 * contains a replacement for this class and it contains other aggregate operations.
 * <p>
 * Use {@link org.sormula.operation.aggregate.SelectCountOperation} as a replacement
 * with "*" for expression in constructor.
 * 
 * @since 1.0
 * @author Jeff Miller
 * @see org.sormula.operation.SelectCountOperation
 * @param <R> class type which contains members for columns of a row in a table
 */
@Deprecated // replaced by org.sormula.operation.aggregate.SelectCountOperation
public class SelectCountOperation<R> extends ScalarSelectOperation<R>
{
    /**
     * Constructs standard sql select by primary key as:<br>
     * SELECT COUNT(*), ... FROM table WHERE primary key clause
     * 
     * @param table insert into this table
     * @throws OperationException if error
     */
    public SelectCountOperation(Table<R> table) throws OperationException
    {
        this(table, "");
    }
    
    
    /**
     * Constructs standard sql select as:<br>
     * SELECT COUNT(*), ... FROM table WHERE ...
     * 
     * @param table insert into this table
     * @param whereConditionName name of where condition to use ("primaryKey" to select
     * by primary key; empty string to select all rows in table)
     * @throws OperationException if error
     */
    public SelectCountOperation(Table<R> table, String whereConditionName) throws OperationException
    {
        super(table, "");
        initBaseSql();
        setWhere(whereConditionName);
    }


    /**
     * Sets base sql with {@link #setBaseSql(String)}.
     */
    protected void initBaseSql()
    {
        String tableName = getTable().getQualifiedTableName();
        StringBuilder sql = new StringBuilder(tableName.length() + 50);
        
        sql.append("SELECT COUNT(*) ");
        sql.append("FROM ");
        sql.append(tableName);
        
        setBaseSql(sql.toString());
    }


    /**
     * Use {@link #readCount()} instead. R class rows are not returned from select count(*).
     * @throws OperationException for all invocations
     */
    @Override
    public R readNext() throws OperationException
    {
        throw new OperationException("not implemented");
    }
    
    
    /**
     * Reads the count of rows. Invoke {@link #execute()} prior to using this method.
     * 
     * @return value of count or -1 if count can't be read
     * @throws OperationException if error
     */
    public Integer readCount() throws OperationException
    {
        int count = -1;
        ResultSet rs = getResultSet();
        
        try
        {
            if (rs.next())
            {
                count = rs.getInt(1);
            }
        }
        catch (SQLException e)
        {
            throw new OperationException("error reading count", e);
        }
        
        return count;
    }
}
