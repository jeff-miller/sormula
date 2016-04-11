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

import org.sormula.annotation.Column;
import org.sormula.annotation.ImplicitType;
import org.sormula.translator.standard.TrimTranslator;


/**
 * Row class for {@link TrimTranslatorTest}.
 * 
 * @author Jeff Miller
 */
public class SormulaTrimTest
{
    @Column(primaryKey=true)
    int id;
    
    @ImplicitType(translator=TrimTranslator.class)
    String charColumn;
    
    @ImplicitType(translator=TrimTranslator.class)
    String varcharColumn;
    
    
    public int getId() 
    {
        return id;
    }
    public void setId(int id) 
    {
        this.id = id;
    }
    
    
    public String getCharColumn() 
    {
        return charColumn;
    }
    public void setCharColumn(String charColumn) 
    {
        this.charColumn = charColumn;
    }
    
    
    public String getVarcharColumn() 
    {
        return varcharColumn;
    }
    public void setVarcharColumn(String varcharColumn) 
    {
        this.varcharColumn = varcharColumn;
    }
}
