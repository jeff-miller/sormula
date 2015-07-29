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
import java.util.function.Function;

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
	Function<R, K> keyFunction;
	
	
	/**
     * Constructs for a table to use primary key where condition. This is the standard 
     * constructor for all {@link SqlOperation} classes.
     * <p>
     * It is unlikely that you will want to use this constructor without also
     * changing the where condition since at most one row will be selected. Use 
     * {@link #setWhere(String)} or {@link #setWhereTranslator(org.sormula.translator.AbstractWhereTranslator)} 
     * to change the default primary key where condition. 
	 * 
	 * @param table select from this table
	 * @throws OperationException if error
	 */
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
	 * Sets the method name of row class R that gets key of type K to use in Map.
	 * Used by {@link #add(Object)} to add a row to the map. 
	 * The default is "hashCode".
	 * <p>
	 * Override {@link #getKey(Object)} to implement alternative ways to form key for Map.
     * 
	 * @param getKeyMethodName name of row method to get key for Map 
	 * @throws OperationException if error
	 * @see #setKeyFunction(Function)
	 */
	public void setGetKeyMethodName(String getKeyMethodName) throws OperationException
	{
        try
        {
            getKeyMethod = getTable().getRowTranslator().getRowClass().getMethod(getKeyMethodName);
        }
        catch (NoSuchMethodException e)
        {
            throw new OperationException("error getting key method " + getKeyMethodName, e);
        }
	}


	/**
	 * Return the name of the method for the row R that returns the Map key K. The default is "hashCode".
	 * 
	 * @return name of row method that gets row key for Map or null if none
	 * @see #getKeyFunction()
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
	 * Gets the function used to obtain the map key from a row.
	 * 
	 * @return function set by {@link #setKeyFunction(Function)} or null if none
	 * @since 4.0
	 */
	public Function<R, K> getKeyFunction() 
	{
		return keyFunction;
	}


	/**
	 * Sets the function used to obtain the map key from a row. If no key function is set
	 * and no method was set with {@link #setGetKeyMethodName(String)}, then the default
	 * is to use {@link Object#hashCode()}. 
	 * <p>
	 * Examples of use:
	 * <blockquote><pre>
	 * MapSelectOperation&lt;SomeKey, SomeRow&gt; operation = ...
	 * operation.setKeyFunction(SomeRow::getId);
	 * operation.setKeyFunction(r -&gt; r.getId());
	 * </pre></blockquote>
	 * 
	 * @param keyFunction lambda expression that will return map key, K, from a row, R
	 * @since 4.0
	 */
	public void setKeyFunction(Function<R, K> keyFunction) 
	{
		this.keyFunction = keyFunction;
	}
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void prepare() throws OperationException 
	{
		super.prepare();
		
		if (keyFunction == null && getKeyMethod == null)
		{
			// default key method is hashCode()
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
		if (keyFunction != null)
		{
			// function specified, use it
			return keyFunction.apply(row);
		}
		else
		{
			// assume method name specified, get key with method
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
}
