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
package org.sormula.tests.translator;

import java.math.BigDecimal;


/**
 * Row class for {@link BigDecimalTranslatorTest}.
 * @author Jeff Miller
 */
public class SormulaTestBD
{
    BigDecimal testBigDecimal1;
    BigDecimal testBigDecimal2;
    
    
    public BigDecimal getTestBigDecimal1()
    {
        return testBigDecimal1;
    }
    public void setTestBigDecimal1(BigDecimal testBigDecimal1)
    {
        this.testBigDecimal1 = testBigDecimal1;
    }
    public BigDecimal getTestBigDecimal2()
    {
        return testBigDecimal2;
    }
    public void setTestBigDecimal2(BigDecimal testBigDecimal2)
    {
        this.testBigDecimal2 = testBigDecimal2;
    }
}
