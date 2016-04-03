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

import org.sormula.translator.standard.EnumToStringTranslator;


/**
 * For testing reading/writing enums as String values. The value written/read to/from database
 * is {@link #toString()} when using {@link EnumToStringTranslator}.
 * 
 * @author Jeff Miller
 */
public enum EnumFieldTS
{
    Hot("H"), Cold("C"), Warm("W");
    
    String value;

    
    private EnumFieldTS(String value) 
    {
        this.value = value;
    }

    
    /** 
     * Value of enum in database.
     */
    @Override
    public String toString() 
    {
        return value;
    }
}
