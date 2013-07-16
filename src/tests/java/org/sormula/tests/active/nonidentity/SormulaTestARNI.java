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
package org.sormula.tests.active.nonidentity;

import org.sormula.active.ActiveRecord;
import org.sormula.active.ActiveTable;
import org.sormula.annotation.Column;


/**
 * Active record for tests in org.sormula.tests.active.nonidentity package.
 * 
 * @author Jeff Miller
 */
public class SormulaTestARNI extends ActiveRecord<SormulaTestARNI>
{
    private static final long serialVersionUID = 1L;
    public static final ActiveTable<SormulaTestARNI> table = table(SormulaTestARNI.class);
    
    @Column(identity=true)
    int id;
    String description;
    
    
    public SormulaTestARNI()
    {
    }

    
    public SormulaTestARNI(int id, String description)
    {
        this.id = id;
        this.description = description;
    }
    
    
    public int getId()
    {
        return id;
    }
    public void setId(int id)
    {
        this.id = id;
    }
    
    
    public String getDescription()
    {
        return description;
    }
    public void setDescription(String description)
    {
        this.description = description;
    }
}
