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
package org.sormula.tests.cascade.lazy;

import java.util.Map;

import org.sormula.Database;
import org.sormula.annotation.Row;
import org.sormula.annotation.Transient;
import org.sormula.annotation.cascade.Cascade;
import org.sormula.annotation.cascade.DeleteCascade;
import org.sormula.annotation.cascade.InsertCascade;
import org.sormula.annotation.cascade.SaveCascade;
import org.sormula.annotation.cascade.SelectCascade;
import org.sormula.annotation.cascade.UpdateCascade;
import org.sormula.operation.HashMapSelectOperation;
import org.sormula.operation.cascade.lazy.DurableLazySelector;
import org.sormula.operation.cascade.lazy.LazyCascadeException;
import org.sormula.operation.cascade.lazy.LazySelectable;


/**
 * Row class where child belongs to only one parent for testing lazy select cascades
 * using a delegate instead of inheritance. Lazy selects work any time (even when 
 * original connection has been closed). 
 * 
 * @author Jeff Miller
 */
@Row(tableName="SormulaTestParentLazy", primaryKeyFields="id")
public class SormulaTestParentLazy4 implements LazySelectable
{
    int id;
    String description;
    
    @Transient
    DurableLazySelector<SormulaTestParentLazy4> lazySelector = new DurableLazySelector<>(this);
    
    
    // tests general cascade and map type
    @Cascade(
    		selects=@SelectCascade(operation=HashMapSelectOperation.class, sourceParameterFieldNames="id", targetWhereName="byParent", targetKeyMethodName="getId", lazy=true),
			inserts=@InsertCascade(),
            updates=@UpdateCascade(),
    		saves=@SaveCascade(),
            deletes=@DeleteCascade()
	)
    Map<Integer, SormulaTestChildLazy> childMap;
    
    
    public SormulaTestParentLazy4()
    {
    }

    
    public SormulaTestParentLazy4(int id, String description)
    {
        this();
        this.id = id;
        this.description = description;
    }

    
    public void pendingLazySelects(Database database) throws LazyCascadeException
    {
        lazySelector.pendingLazySelects(database);
    }


    public void checkLazySelects(String fieldName) throws LazyCascadeException
    {
        lazySelector.checkLazySelects(fieldName);
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


    public Map<Integer, SormulaTestChildLazy> getChildMap()
    {
        checkLazySelects("childMap");
		return childMap;
	}
	public void setChildMap(Map<Integer, SormulaTestChildLazy> childMap) 
	{
		this.childMap = childMap;
	}
}
