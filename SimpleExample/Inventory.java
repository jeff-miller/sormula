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
import org.sormula.annotation.Column;
import org.sormula.annotation.Where;
import org.sormula.annotation.WhereField;


/**
 * Class that corresponds to a row in the INVENTORY table.
 * 
 * @author Jeff Miller
 */
@Where(name="manf", fieldNames="manufacturerId") // where manf=?
@Where(name="partNumberIn", whereFields=@WhereField(name="partNumber", comparisonOperator="IN")) // where partnumber in (?, ?, ...)
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
