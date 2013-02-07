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
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.sormula.Table;
import org.sormula.operation.ArrayListSelectOperation;
import org.sormula.operation.OperationException;
import org.sormula.operation.SqlOperation;


/**
 * Example of a custom select operation. Selects all rows with quantity between
 * minimum and maximum. Uses {@link PreparedStatement#setInt(int, int)} instead
 * of default behavior of {@link SqlOperation#writeParameters()}.
 * 
 * @author Jeff
 */
public class QuantityRangeSelect extends ArrayListSelectOperation<Inventory> 
{
	int minimumQuanity;
	int maximumQuantity;
	
	
	public QuantityRangeSelect(Table<Inventory> table) throws OperationException 
	{
		super(table, "");
		// custom sql appended to "select partnumber, quantity... from inventory" during prepare
		setCustomSql("where quantity between ? and ?");
	}

	
	public void setRange(int minimumQuanity, int maximumQuantity)
	{
		this.minimumQuanity = minimumQuanity;
		this.maximumQuantity = maximumQuantity;
	}
	
	
	@Override
	protected void writeParameters() throws OperationException 
	{
		try
		{
			// standard JDBC
			PreparedStatement ps = getPreparedStatement();
			ps.setInt(1, minimumQuanity);
			ps.setInt(2, maximumQuantity);
		}
		catch (SQLException e)
		{
			throw new OperationException("error preparing quantity range", e);
		}
	}
}
