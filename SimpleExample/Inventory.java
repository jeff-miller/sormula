import org.sormula.annotation.Column;
import org.sormula.annotation.Where;
import org.sormula.annotation.WhereField;
import org.sormula.annotation.Wheres;


/**
 * Class that corresponds to a row in the INVENTORY table.
 * 
 * @author Jeff Miller
 */
@Wheres({
    @Where(name="manf", fieldNames="manufacturerId"),
    @Where(name="partNumberIn", whereFields=@WhereField(name="partNumber", comparisonOperator="IN"))
})
public class Inventory // table name defaults to "inventory", use @Row(tableName="...") to override 
{
    @Column(primaryKey=true) // table column name defaults to "partnumber", column is primary key 
    int partNumber;
    
    int quantity; // no annotation is needed when field name is same as column name
    
    @Column(name="manfid") // table column name is "manfid"
    String manufacturerId;
    
    
    public int getPartNumber()
    {
        return partNumber;
    }
    public void setPartNumber(int partNumber)
    {
        this.partNumber = partNumber;
    }
    
    
    public int getQuantity()
    {
        return quantity;
    }
    public void setQuantity(int quantity)
    {
        this.quantity = quantity;
    }
    
    
    public String getManufacturerId()
    {
        return manufacturerId;
    }
    public void setManufacturerId(String manufacturerId)
    {
        this.manufacturerId = manufacturerId;
    }
}
