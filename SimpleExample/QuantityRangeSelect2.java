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
public class QuantityRangeSelect2 extends ArrayListSelectOperation<Inventory> 
{
	int minimumQuanity;
	int maximumQuantity;
	
	
	public QuantityRangeSelect2(Table<Inventory> table) throws OperationException 
	{
		super(table);
	}

	
	public void setRange(int minimumQuanity, int maximumQuantity)
	{
		this.minimumQuanity = minimumQuanity;
		this.maximumQuantity = maximumQuantity;
	}
	
	
	@Override
	protected String getSql() 
	{
		return super.getBaseSql() + " where quantity between ? and ?";
	}


	@Override
	protected void prepareParameters() throws OperationException 
	{
		try
		{
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
