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
package org.sormula.examples.manytomany.inherited;

import org.sormula.annotation.Column;
import org.sormula.annotation.Row;
import org.sormula.annotation.Where;


/**
 * Order item table row class with no cascade relationships.
 * Quantity is omitted for simplicity.
 */
@Where(name="whereProduct", fieldNames="productId")
@Row(tableName="orderitem")
public class SimpleOrderItem
{
    @Column(primaryKey=true)
    int orderId;
    int itemNumber;
    String productId;
    
    
    public SimpleOrderItem()
    {
    }


    public SimpleOrderItem(int itemNumber, String productId)
    {
        this.itemNumber = itemNumber;
        this.productId = productId;
    }


    public int getOrderId()
    {
        return orderId;
    }
    public void setOrderId(int orderId)
    {
        this.orderId = orderId;
    }


    public int getItemNumber()
    {
        return itemNumber;
    }
    public void setItemNumber(int itemNumber)
    {
        this.itemNumber = itemNumber;
    }


    public String getProductId()
    {
        return productId;
    }
    public void setProductId(String productId)
    {
        this.productId = productId;
    }
}
