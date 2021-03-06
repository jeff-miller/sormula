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

import org.sormula.annotation.Row;
import org.sormula.annotation.cascade.OneToOneCascade;


/**
 * Order item table row class with cascade relationship to {@link SimpleOrder}.
 */
@Row(tableName="orderitem", inhertedFields=true)
public class OrderItemOrder extends SimpleOrderItem
{
    @OneToOneCascade(readOnly=true) // don't modify order when OrderItemOrder is modified
    SimpleOrder order;
    
    
    public OrderItemOrder()
    {
    }


    public OrderItemOrder(int itemNumber, String productId)
    {
        super(itemNumber, productId);
    }


    public SimpleOrder getOrder()
    {
        return order;
    }
    public void setOrder(SimpleOrder order)
    {
        this.order = order;
    }
}
