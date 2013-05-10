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
import org.sormula.annotation.Where;
import org.sormula.annotation.cascade.OneToOneCascade;
import org.sormula.annotation.cascade.SelectCascade;
import org.sormula.operation.ScalarSelectOperation;


/**
 * Order item table row class with cascade relationship to {@link SimpleProduct}.
 * Quantity is omitted for simplicity.
 */
@Where(name="whereProduct", fieldNames="productId")
@Row(inhertedFields=true)
public class OrderItem extends SimpleOrderItem
{
    @OneToOneCascade(selects=@SelectCascade(operation=ScalarSelectOperation.class, sourceParameterFieldNames="productId"),
            readOnly=true) // don't modify products when OrderItem is modified
    SimpleProduct product;
    
    
    public OrderItem()
    {
    }


    public OrderItem(int itemNumber, String productId)
    {
        super(itemNumber, productId);
    }


    public SimpleProduct getProduct()
    {
        return product;
    }
    public void setProduct(SimpleProduct product)
    {
        this.product = product;
    }
}
