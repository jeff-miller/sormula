/* sormula - Simple object relational mapping
 * Copyright (C) 2011-2013 Jeff Miller
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
package org.sormula.examples.filter;

import org.sormula.operation.ScalarSelectOperation;
import org.sormula.operation.filter.AbstractSelectCascadeFilter;


/**
 * Simple filter to keep orders that contain a specific product ID.
 */
public class ProductFilter extends AbstractSelectCascadeFilter
{
    String productId;
    
    
    public ProductFilter(String productId) 
    {
        this.productId = productId;
    }


    public boolean accept(ScalarSelectOperation<Object> source, Order order, boolean cascadesCompleted)
    {
        if (!cascadesCompleted)
        {
            // assume order will be kept, test after cascades occur
            return true;
        }
        else
        {
            // keep order if one order item contains desired product
            for (OrderItem oi : order.getOrderItems())
            {
                if (oi.getProductId().equals(productId)) return true;
            }
            
            // assume product id was not in order
            return false;
        }
    }
}
