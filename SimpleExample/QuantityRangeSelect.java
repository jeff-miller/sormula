import org.sormula.Table;
import org.sormula.operation.ArrayListSelectOperation;
import org.sormula.operation.OperationException;


/**
 * Example of a custom select operation. Selects all rows with quantity between
 * minimum and maximum.
 * 
 * @author Jeff
 */
public class QuantityRangeSelect extends ArrayListSelectOperation<Inventory> 
{
	public QuantityRangeSelect(Table<Inventory> table) throws OperationException 
	{
		super(table);
	}

	
	public void setRange(int minimumQuanity, int maximumQuantity)
	{
		setParameters(minimumQuanity, maximumQuantity);
	}
	
	
	@Override
	protected String getSql() 
	{
		return super.getBaseSql() + " where quantity between ? and ?";
	}
}
