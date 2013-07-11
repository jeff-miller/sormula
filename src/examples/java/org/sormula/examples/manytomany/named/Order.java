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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.sormula.annotation.Column;
import org.sormula.annotation.Row;
import org.sormula.annotation.cascade.OneToManyCascade;


/**
 * Order table row class with named cascade relationships.
 */
@Row(tableName="\"order\"")
public class Order
{
    static SimpleDateFormat orderDateFormat = new SimpleDateFormat("yyyy-MMM-dd");
    
    @Column(primaryKey=true)
    int orderId;
    Date orderDate;

    // select joins on orderId fields
    @OneToManyCascade(name="standard")
    List<OrderItem> orderItems;
    
    
    public Order()
    {
    }


    public Order(int orderId)
    {
        this.orderId = orderId;
        orderItems = new ArrayList<OrderItem>();
    }


    public int getOrderId()
    {
        return orderId;
    }
    public void setOrderId(int orderId)
    {
        this.orderId = orderId;
    }


    public Date getOrderDate()
    {
        return orderDate;
    }
    public void setOrderDate(Date orderdate)
    {
        this.orderDate = orderdate;
    }
    public String getFormattedOrderDate()
    {
        return orderDateFormat.format(orderDate);
    }


    public List<OrderItem> getOrderItems()
    {
        return orderItems;
    }
    public void setOrderItems(List<OrderItem> orderItems)
    {
        this.orderItems = orderItems;
    }
    public void add(OrderItem orderItem)
    {
        orderItem.setOrderId(orderId);
        orderItems.add(orderItem);
    }
}
