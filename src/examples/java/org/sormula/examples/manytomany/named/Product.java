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
package org.sormula.examples.manytomany.named;

import java.util.List;

import org.sormula.annotation.Column;
import org.sormula.annotation.Row;
import org.sormula.annotation.cascade.OneToManyCascade;


/**
 * Product table row class with named cascade relationships.
 */
@Row(tableName="product")
public class Product
{
    @Column(primaryKey=true)
    String productId;
    String description;

    @OneToManyCascade(name="product-orders",
            readOnly=true) // don't modify orders when product is modfied
    List<OrderItem> orderItems;
    
    
    public Product()
    {
    }
    
    
    public Product(String productId, String description)
    {
        this.productId = productId;
        this.description = description;
    }
    
    
    public String getProductId()
    {
        return productId;
    }
    public void setProductId(String productId)
    {
        this.productId = productId;
    }
    
    
    public String getDescription()
    {
        return description;
    }
    public void setDescription(String description)
    {
        this.description = description;
    }
    
    
    public List<OrderItem> getOrderItems()
    {
        return orderItems;
    }
    public void setOrderItems(List<OrderItem> orderItems)
    {
        this.orderItems = orderItems;
    }
}
