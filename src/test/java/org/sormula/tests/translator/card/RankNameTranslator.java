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
package org.sormula.tests.translator.card;

import org.sormula.translator.DelimitedNameTranslator;

public class RankNameTranslator extends DelimitedNameTranslator
{
    @Override
    public String translate(String name, Class rowClass)
    {
        if (name.equalsIgnoreCase("rank"))
        {
            // rank is a reserved word in some databases
            return super.translate(name, rowClass);
        }
        
        return name;
    }
}
