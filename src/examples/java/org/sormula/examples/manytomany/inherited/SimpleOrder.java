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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.sormula.annotation.Column;
import org.sormula.annotation.Row;


/**
 * Order table row class with no cascade relationships.
 */
@Row(tableName="\"order\"")
public class SimpleOrder
{
    static SimpleDateFormat orderDateFormat = new SimpleDateFormat("yyyy-MMM-dd");
    
    @Column(primaryKey=true)
    int orderId;
    Date orderDate;
    
    
    public SimpleOrder()
    {
    }


    public SimpleOrder(int orderId)
    {
        this.orderId = orderId;
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
}
