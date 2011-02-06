import org.sormula.annotation.Column;
import org.sormula.annotation.Where;


/**
 * Class that corresponds to a row in the INVENTORY table.
 * 
 * @author Jeff Miller
 */
@Where(name="manf", fieldNames="manufacturerId")
public class Inventory
{
    @Column(primaryKey=true)
    int partNumber;
    
    int quantity;
    
    @Column(name="manfid")
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
