

/**
 * Class that corresponds to a row in the INVENTORY table. No sormula annoations are needed
 * since Inventory class name and fields correspond directly with table name and columns.
 * 
 * @author Jeff Miller
 */
public class Inventory // table name defaults to "inventory", use @Row(tableName="...") to specify different name 
{
    int partNumber;         // first field is primary key, use @Column(primaryKey=true) to specify different key
    int quantity;           // corresponds to column quantity, use @Column(name="...") to specify different name
    String manufacturerId;  // corresponds to column manfId, use @Column(name="...") to specify different name
    
    
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
