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

import java.util.List;

import org.sormula.annotation.Row;
import org.sormula.annotation.cascade.OneToManyCascade;
import org.sormula.annotation.cascade.SelectCascade;
import org.sormula.operation.ArrayListSelectOperation;


/**
 * Product table row class with cascade relationship to {@link OrderItemOrder}.
 */
@Row(tableName="Product", inhertedFields=true)
public class ProductOrderItems extends SimpleProduct
{
    @OneToManyCascade(selects=@SelectCascade(operation=ArrayListSelectOperation.class, 
            targetWhereName="whereProduct", sourceParameterFieldNames="productId"),
            readOnly=true) // don't modify orders when product is modfied
    List<OrderItemOrder> orderItems;
    
    
    public List<OrderItemOrder> getOrderItems()
    {
        return orderItems;
    }
    public void setOrderItems(List<OrderItemOrder> orderItems)
    {
        this.orderItems = orderItems;
    }
}
