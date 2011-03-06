import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.sormula.Table;
import org.sormula.operation.ArrayListSelectOperation;
import org.sormula.operation.OperationException;
import org.sormula.operation.SqlOperation;


/**
 * Example of a custom select operation. Selects all rows with quantity between
 * minimum and maximum. Uses {@link PreparedStatement#setInt(int, int)} instead
 * of default behavior of {@link SqlOperation#prepareParameters()}.
 * 
 * @author Jeff
 */
public class QuantityRangeSelect extends ArrayListSelectOperation<Inventory> 
{
	int minimumQuanity;
	int maximumQuantity;
	
	
	public QuantityRangeSelect(Table<Inventory> table) throws OperationException 
	{
		super(table);
		// custom sql appended to "select partnumber, quantity... from inventory" during prepare
		setCustomSql("where quantity between ? and ?");
	}

	
	public void setRange(int minimumQuanity, int maximumQuantity)
	{
		this.minimumQuanity = minimumQuanity;
		this.maximumQuantity = maximumQuantity;
	}
	
	
	@Override
	protected void prepareParameters() throws OperationException 
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
