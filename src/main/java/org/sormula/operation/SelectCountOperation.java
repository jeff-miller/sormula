/* sormula - Simple object relational mapping
 * Copyright (C) 2011 Jeff Miller
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
 * This class remains in this package for backward compatibility. org.sormula.operation.aggregate
 * contains a replacement for this class and it contains other aggregate operations.
 * 
 * @since 1.0
 * @author Jeff Miller
 * @see org.sormula.operation.aggregate.SelectCountOperation
 */
@Deprecated
public class SelectCountOperation<R> extends ScalarSelectOperation<R>
{
    public SelectCountOperation(Table<R> table) throws OperationException
    {
        super(table, "");
        initBaseSql();
    }


    protected void initBaseSql()
    {
        String tableName = getTable().getQualifiedTableName();
        StringBuilder sql = new StringBuilder(tableName.length() + 50);
        
        sql.append("SELECT COUNT(*) ");
        sql.append("FROM ");
        sql.append(tableName);
        
        setBaseSql(sql.toString());
    }


    @Override
    public R readNext() throws OperationException
    {
        throw new OperationException("not implemented");
    }
    
    
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
