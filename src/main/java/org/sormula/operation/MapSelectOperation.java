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
package org.sormula.operation;

import java.lang.reflect.Method;
import java.util.Map;

import org.sormula.Table;


/**
 * {@link SelectOperation} that uses {@link Map} for {@link #readAll()} results. 
 * 
 * @since 1.0
 * @author Jeff Miller
 * @param <K> key class type for row
 * @param <R> Class associated with a row in table
 */
public abstract class MapSelectOperation<K, R> extends SelectOperation<R, Map<K, R>>
{
	Method getKeyMethod;
	
	
	/**
	 * Constructs for a table.
	 * 
	 * @param table select from this table
	 * @throws OperationException if error
	 */
	// TODO deprecate this method and subclass methods? primary key selects only 1 row
    public MapSelectOperation(Table<R> table) throws OperationException
    {
        super(table);
    }
    
    
    /**
     * Constructs for a table and where condition.
     * 
     * @param table select from this table
     * @param whereConditionName name of where condition to use ("primaryKey" to select
     * by primary key; empty string to select all rows in table)
     * @throws OperationException if error
     */
    public MapSelectOperation(Table<R> table, String whereConditionName) throws OperationException
    {
        super(table, whereConditionName);
    }


    /**
     * Gets the method of class R that obtains key of type K from a row. Used by
     * {@link #add(Object)} to add a row to the map.
     *  
     * @return method that supplies map key value for a row; default is {@link Object#hashCode()}
     */
    public Method getGetKeyMethod() 
    {
		return getKeyMethod;
	}


    /**
     * Sets the get key method. See {@link #getGetKeyMethod()} for details. The default
     * is {@link #hashCode()}. Use this method or {@link #setGetKeyMethodName(String)} to define 
     * the get key method  or override {@link #getKey(Object)}.
     * 
     * @param getKeyMethod row method that gets map key 
     */
	public void setGetKeyMethod(Method getKeyMethod) 
	{
		this.getKeyMethod = getKeyMethod;
	}

	
	/**
	 * Sets the get key method. See {@link #getGetKeyMethod()} for details. The default
     * is {@link #hashCode()}. Use this method or {@link #setGetKeyMethod(Method)}to define the 
     * get key method or override {@link #getKey(Object)}.
     * 
	 * @param getKeyMethodName name of row method to get map key 
	 * @throws OperationException if error
	 */
	public void setGetKeyMethodName(String getKeyMethodName) throws OperationException
	{
        try
        {
            setGetKeyMethod(getTable().getRowTranslator().getRowClass().getMethod(getKeyMethodName));
        }
        catch (NoSuchMethodException e)
        {
            throw new OperationException("error getting key method " + getKeyMethodName, e);
        }
	}
	
	
	/**
	 * Return the name of the get key method.
	 * 
	 * @return name of row method that gets map key
	 */
	public String getGetKeyMethodName()
	{
	    if (getKeyMethod != null)
	    {
	        return getKeyMethod.getName();
	    }
	    else
	    {
	        return null;
	    }
	}
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void prepare() throws OperationException 
	{
		super.prepare();
		
		if (getKeyMethod == null)
		{
			// default get key method is hashCode()
		    setGetKeyMethodName("hashCode");
		}
	}


	/**
     * {@inheritDoc}
     */
    @Override
    protected boolean add(R row) throws OperationException
    {
        getSelectedRows().put(getKey(row), row);
        return true;
    }
    
    
    /**
     * Gets key for row.
     * 
     * @param row row object
     * @return unique key for row to use as key for map
     * @throws OperationException if error
     */
	protected K getKey(R row) throws OperationException
    {
    	try
    	{
    		@SuppressWarnings("unchecked") // invoke returns Object
    		K key = (K)getKeyMethod.invoke(row);
    		return key;
    	}
    	catch (Exception e)
    	{
    		throw new OperationException("error getting key value", e);
    	}
    }
}
