import org.sormula.annotation.Column;
import org.sormula.annotation.Row;

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


/**
 * Class that corresponds to a row in the INVENTORY table. No sormula annoations are needed
 * since Inventory class name and fields correspond directly with table name and columns.
 * <p>
 * Note: some JVM's do not reflect fields in the order that they are declared. For the JVM's that
 * do not, {@link Column#primaryKey()} or {@link Row#primaryKeyFields()} may be required.
 * 
 * @author Jeff Miller
 */
public class Inventory // table name defaults to "inventory", use @Row(tableName="...") to specify different name 
{
    int partNumber;         // first field is primary key, use @Column(primaryKey=true) to specify different key(s)
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
