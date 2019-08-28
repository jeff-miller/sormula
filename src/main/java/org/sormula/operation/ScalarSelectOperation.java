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

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

import org.sormula.Table;
import org.sormula.annotation.Column;
import org.sormula.annotation.OrderBy;
import org.sormula.annotation.OrderByAnnotationReader;
import org.sormula.annotation.Row;
import org.sormula.annotation.Where;
import org.sormula.annotation.cascade.SelectCascade;
import org.sormula.annotation.cascade.SelectCascadeAnnotationReader;
import org.sormula.cache.Cache;
import org.sormula.cache.CacheException;
import org.sormula.log.SormulaLogger;
import org.sormula.log.SormulaLoggerFactory;
import org.sormula.operation.builder.ScalarSelectOperationBuilder;
import org.sormula.operation.cascade.CascadeOperation;
import org.sormula.operation.cascade.SelectCascadeOperation;
import org.sormula.operation.cascade.lazy.LazySelectable;
import org.sormula.operation.monitor.OperationTime;
import org.sormula.reflect.RowField;
import org.sormula.translator.OrderByTranslator;
import org.sormula.translator.RowTranslator;
import org.sormula.translator.TranslatorException;


/**
 * Select operation that reads one value at a time. It does not have any methods
 * that use {@link Collection} objects.
 * 
 * @since 1.0
 * @author Jeff Miller
 * @param <R> class type which contains members for columns of a row in a table
 */
public class ScalarSelectOperation<R> extends SqlOperation<R> implements Iterable<R>
{
    private static final SormulaLogger log = SormulaLoggerFactory.getClassLogger();
    
    ResultSet resultSet;
    R rowParameters;
    String orderByName;
    OrderByTranslator<R> orderByTranslator;
    RowTranslator<R> rowTranslator;
    int maximumRowsRead = Integer.MAX_VALUE;
    int rowsReadCount;
    boolean lazySelectsCascades;
    boolean notifyLazySelects;
    boolean cachePrimaryKeySelect; // set by execute() 
    boolean cacheContainsPrimaryKey; // set by execute() if cache hit
    boolean executed;
    
    Map<Class<?>, BiPredicate<?, Boolean>> filterPredicateMap;
    BiPredicate<R, Boolean> filterPredicate;
    
    
    /**
     * Creates a builder.
     * 
     * @param <R> type of row in table
     * @param table select from this table
     * @return builder
     * @since 4.4
     */
    public static <R> ScalarSelectOperationBuilder<R> builderScalar(Table<R> table)
    {
        return new ScalarSelectOperationBuilder<R>(table);  
    }

    
    /**
     * Constructs standard sql select by primary key as:<br>
     * SELECT c1, c2, c3, ... FROM table WHERE primary key clause
     * 
     * @param table select from this table
     * @throws OperationException if error
     */
    public ScalarSelectOperation(Table<R> table) throws OperationException
    {
        this(table, "primaryKey");
    }
    
    
    /**
     * Constructs standard sql select by primary key as:<br>
     * SELECT c1, c2, c3, ... FROM table WHERE ...
     * 
     * @param table select from this table
     * @param whereConditionName name of where condition to use ("primaryKey" to select
     * by primary key; empty string to select all rows in table)
     * @throws OperationException if error
     */
    public ScalarSelectOperation(Table<R> table, String whereConditionName) throws OperationException
    {
        super(table);
        rowTranslator = table.getRowTranslator();
        initBaseSql();
        setWhere(whereConditionName);
    }
    
    
    /**
     * Creates iterator for results of this operation.
     * 
     * @return {@link SelectIterator}
     * 
     * @since 3.0
     */
    @Override
    public Iterator<R> iterator()
    {
        return new SelectIterator<>(this);
    }
    
    
    /**
     * Gets the maximum number of rows to read from result set. The default
     * is {@link Integer#MAX_VALUE}.
     * 
     * @return 0..{@link Integer#MAX_VALUE}
     * @since 1.4
     */
    public int getMaximumRowsRead()
    {
        return maximumRowsRead;
    }


    /**
     * Sets the maximum number of rows to read from result set. This method 
     * does NOT alter SQL to contain anything to limit query but only 
     * limits the number of rows read by {@link #readNext()} and 
     * {@link SelectOperation#readAll()}. Limiting rows read is useful to avoid
     * reading too many rows and thus creating memory or display problems.
     * 
     * @param maximumRowsRead 0..{@link Integer#MAX_VALUE}
     * @since 1.4
     */
    public void setMaximumRowsRead(int maximumRowsRead)
    {
        this.maximumRowsRead = maximumRowsRead;
    }
    
    
    /**
     * Gets the count of rows that were read since the most recent {@link #execute()}.
     * 
     * @return number of rows read
     * @since 1.4
     */
    public int getRowsReadCount()
    {
        return rowsReadCount;
    }

    
    /**
     * Sets the number of rows read to zero. Use this method if {@link #setMaximumRowsRead(int)} has been
     * used to limit the number of rows, some rows have already been read, and you would like to reset
     * the counting of maximum number of rows. For example, after a page of rows is read.
     * 
     * @since 4.3
     */
    public void resetRowsReadCount()
    {
        rowsReadCount = 0;
    }
    

    /**
     * Set parameters using values from a row object. Use this instead of {@link #setParameters(Object...)}.
     * 
     * @param rowParameters where parameters are read from this object using fields defined by
     * {@link Where} or the primary key defined by {@link Column#primaryKey()}, 
     * {@link Column#identity()}, or {@link Row#primaryKeyFields()}.
     */
    public void setRowParameters(R rowParameters) 
    {
        this.rowParameters = rowParameters;
        
        // rowParameters and object parameters are mutually exclusive
        super.setParameters((Object)null); // note: must invoke super method
    }
    

    /**
     * {@inheritDoc}
     */
    @Override
    public void setParameters(Object... parameters)
    {
        super.setParameters(parameters);
        
        // rowParameters and object parameters are mutually exclusive
        rowParameters = null;
    }


    /**
     * Performs query. Use {@link #readNext()} to get the next row selected.
     *  
     * @throws OperationException if error
     */
    @Override
    public void execute() throws OperationException
    {
        if (log.isDebugEnabled()) log.debug("execute()");
        
        cachePrimaryKeySelect = false;
        cacheContainsPrimaryKey = false;
        initOperationTime();
        setNextParameter(1);
        resetRowsReadCount();
        
        if (isCached())
        {
            // table is cached
        	Cache<R> cache = table.getCache();
        	
            try
            {
                // notify cache of start of execution
                cache.execute(this);
            }
            catch (CacheException e)
            {
                throw new OperationException("error notifying cache", e);
            }

            if (isPrimaryKey())
            {
                // select is for primary key
                if (rowParameters == null)
                {
                    // primary key is from parameters, use cache instead of preparing sql
                	cachePrimaryKeySelect = true;
                    try
                    {
                        if (log.isDebugEnabled()) log.debug("execute() check cache " + table.getRowClass().getCanonicalName());
                        cacheContainsPrimaryKey = cache.contains(parameters); 
                    }
                    catch (CacheException e)
                    {
                        throw new OperationException("error reading cache", e);
                    }
                }
                //else use AbstractCache.getPrimaryKeyValues() to get cache key? not likely selecting by primary key of row is already known
            }
            
            if (log.isDebugEnabled())
            {
                log.debug("cachePrimaryKeySelect=" + cachePrimaryKeySelect);
                log.debug("cacheContainsPrimaryKey=" + cacheContainsPrimaryKey);
            }
        }
        
        if (!cacheContainsPrimaryKey)
        {
            // not cached or read will be a cache miss so prepare sql to query database
            prepareCheck();
            OperationTime operationTime = getOperationTime();
            
            operationTime.startWriteTime();
            if (rowParameters != null)
            {
                // where values from row object
                writeWhere(rowParameters);
            }
            else 
            {
                // where values from objects
                writeParameters();
            }
            operationTime.stop();
            
            try
            {
                PreparedStatement ps = getPreparedStatement();
                operationTime.startExecuteTime();
                resultSet = ps.executeQuery();
                operationTime.stop();
            }
            catch (Exception e)
            {
                throw new OperationException("execute() error", e);
            }
        }
        
        executed = true;
    }
    
    
    /**
     * Indicates if {@link #execute()} has been invoked.
     * 
     * @return true if operation has been executed; false if not
     * @since 3.0
     */
    public boolean isExecuted()
    {
        return executed;
    }
    

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws OperationException
    {
        try
        {
            if (resultSet != null)
            {
                resultSet.close();
                resultSet = null;
            }
        }
        catch (Exception e)
        {
            throw new OperationException("close() error", e);
        }
        
        super.close();
    }
    
    
    /**
     * Positions the result set cursor to a specific row.
     * 
     * @param rowNumber the number of the row to which the cursor should move. A value of zero indicates that the cursor will be 
     * positioned before the first row; a positive number indicates the row number counting from the beginning of the result set; 
     * a negative number indicates the row number counting from the end of the result set
     * @throws OperationException if error
     * @since 4.3
     * @see ResultSet#absolute(int)
     */
    public void positionAbsolute(int rowNumber) throws OperationException
    {
        try
        {
            resultSet.absolute(rowNumber);
        }
        catch (SQLException e)
        {
            throw new OperationException("cursor position error", e);
        }
    }
    
    
    /**
     * Positions the result set cursor to a row relative to the current row.
     * 
     * @param rowOffset - the number of rows to move from the current row; a positive number moves the cursor forward; 
     * a negative number moves the cursor backward
     * @throws OperationException if error
     * @since 4.3
     * @see ResultSet#relative(int)
     */
    public void positionRelative(int rowOffset) throws OperationException
    {
        try
        {
            resultSet.relative(rowOffset);
        }
        catch (SQLException e)
        {
            throw new OperationException("cursor position error", e);
        }
    }
    
    
    /**
     * Reads next row from current result set. If table is cached, and cache indicates that the next row is deleted,
     * this method will continue to read until non deleted row is selected.
     * 
     * @return new instance of row or null if no more row in the current result set
     * @throws OperationException if error
     */
    public R readNext() throws OperationException
    {
        if (!executed) throw new OperationException("execute() method must be invoked prior to readNext()");
        
        R row = null;
        Cache<R> cache = table.getCache(); 
        
        try
        {
        	if (cachePrimaryKeySelect &&
        	    rowsReadCount == 0) // readNext() must return null after 1st invocation because only 1 row with primary key
        	{
        		// get from cache
        	    // must always try to select from cache so that cache miss status is correct
        	    if (log.isDebugEnabled()) log.debug("select from cache"); 
        	    row = cache.select(parameters);
        	}
    		
    		if (cacheContainsPrimaryKey)
            {
                // row from cache
    		    
    		    // row is null if row is in cache but row has deleted state
                if (filterPredicate != null && row != null)
                {
                    // test for filter pass 
                    if (!filterPredicate.test(row, false) ||
                        (isCascading() && !filterPredicate.test(row, true)) )
                    {
                        // don't use this row based upon filter response
                        row = null;
                    }
                }
            }
            else
            {
            	// select from result set
                if (log.isDebugEnabled()) log.debug("select from result set");
                
                // include resultSet.next() in read time but don't include cascade
                // times since that would cause cascade timings summed twice into total
                operationTime.startReadTime();
                
                if (rowsReadCount < maximumRowsRead && resultSet.next())
                {
                    //  within maximum and at least one more row
                    boolean complete = false;
                    row = table.newRow();
                    
                    while (!complete)
                    {
                        if (notifyLazySelects) 
                        {
                            // inform row of pending select cascades
                            LazySelectable lazySelectCascadeRow = (LazySelectable)row;
                            lazySelectCascadeRow.pendingLazySelects(table.getDatabase());
                        }
                        
                        operationTime.pause();
                        if (isCascading()) preReadCascade(row);
                        preRead(row);
                        
                        operationTime.resume();
                        rowTranslator.read(resultSet, 1, row);
                        operationTime.stop();
                        postRead(row);
                        
                        if (isCached())
                        {
                            // now that row has been selected, key is known, check if cache has newer
                            R cachedRow = cache.selected(row);
                            
                            if (cachedRow != null)
                            {
                                // assume cached row is authority
                                row = cachedRow;
                                if (log.isDebugEnabled()) log.debug("cache row is newer than selected row");
                            }   
                            else
                            {
                                // cache indicates row has been deleted, don't use it, try next row
                                if (log.isDebugEnabled()) log.debug("selected deleted row, select next");
                                row = null;
                            }
                        }

                        if (row != null)  
                        {
                            // row has not been deleted in cache
                            if (filterPredicate == null || filterPredicate.test(row, false))
                            {
                                // no filter or passes filter 
                                if (isCascading()) 
                                {
                                    postReadCascade(row);
                                    complete = filterPredicate == null || filterPredicate.test(row, true);
                                }
                                else
                                {
                                    // exit loop
                                    complete = true;
                                }
                            }
                            else
                            {
                                // did not pass filter
                                if (log.isDebugEnabled()) log.debug("row did not pass filter " + row);
                            }
                        }
                    
                        if (!complete)
                        {
                            // did not get desired row, try next row
                            operationTime.startReadTime(); // see comment above concerning startReadTime
                            
                            if (!resultSet.next()) // advance to next row (if any)
                            {
                                // no more rows
                                complete = true; // exit loop
                                row = null; // return null if no more rows
                                operationTime.cancel(); // see comment below concerning cancel                                
                            }
                        }
                    }
                }
                else
                {
                    // don't stop timer since count will be 1 more than rows read
                    // ignore time for ResultSet.next when no more rows
                    operationTime.cancel();
                }
            }
        }
        catch (Exception e)
        {
            throw new OperationException("readNext() error", e);
        }
        
        if (row != null)
        {
            ++rowsReadCount;
        }
        
        return row;
    }
    

    /**
     * Sets order of results in list. Setting the order condition affects the
     * order of the rows are read from database but is only meaningful if the 
     * resulting collection type is also ordered.
     * 
     * @param orderByName name of order phrase to use as defined in {@link OrderBy#name()}
     * in {@link OrderBy} annotation on row R; use empty string for no ordering
     * @throws OperationException if error
     */
    public void setOrderBy(String orderByName) throws OperationException
    {
        this.orderByName = orderByName;
        
        if (orderByName.length() > 0)
        {
            try
            {
                // look for order annotation in operation, table class, row class (in that order)
                OrderBy orderByAnnotation = new OrderByAnnotationReader(
                        this.getClass(), table.getClass(), table.getRowClass()).getAnnotation(orderByName);
                if (orderByAnnotation != null)
                {
                    setOrderByTranslator(new OrderByTranslator<>(table.getRowTranslator(), orderByAnnotation));
                }
                else
                {
                    throw new OperationException("no OrderBy annotation named, " + orderByName);
                }
            }
            catch (TranslatorException e)
            {
                throw new OperationException("can't create OrderByTranslator for " + orderByName, e);
            }
        }
        else
        {
            // no order by
            setOrderByTranslator(null);
        }
    }
    
    
    /** 
     * Gets order by name that was set with {@link #setOrderBy(String)}.
     * 
     * @return order by name 
     */
    public String getOrderByName()
    {
        return orderByName;
    }


    /**
     * Set parameters, executes, reads one row, closes.
     * <p>
     * Since this class implements {@link AutoCloseable}, you may see resource leak
     * warning when you use this method. You can ignore it, add a suppress annotation, 
     * explicitly close, or close with a try-with-resources statement. Closing an operation 
     * more than once will not cause problems since the close methods are idempotent. 
     * 
     * @param parameters query parameters as objects (see {@link #setParameters(Object...)})
     * @return {@link #readNext()}
     * @throws OperationException if error
     * @since 1.4
     */
    public R select(Object... parameters) throws OperationException
    {
        if (log.isDebugEnabled()) log.debug("select() read from database");
        setParameters(parameters);
        R row;
        try
        {
        	execute();
        	row = readNext();
        }
        finally
        {
        	close();
        }
        return row;
    }
    
    
    /**
     * Set parameters, executes, reads one row, closes.
     * <p>
     * Since this class implements {@link AutoCloseable}, you may see resource leak
     * warning when you use this method. You can ignore it, add a suppress annotation, 
     * explicitly close, or close with a try-with-resources statement. Closing an operation 
     * more than once will not cause problems since the close methods are idempotent. 
     * 
     * @param whereParameters query parameters are read from an existing row object 
     * (see {@link #setRowParameters(Object)})
     * @return {@link #readNext()}
     * @throws OperationException if error
     * @since 1.4
     */
    public R select(R whereParameters) throws OperationException
    {
        setRowParameters(whereParameters);
        R row;
        try
        {
        	execute();
        	row = readNext();
        }
        finally
        {
        	close();
        }
        return row;
    }
    
    
    /**
     * Reports that operation has at least one field with {@link SelectCascade#lazy()} true. This
     * status is set during {@link #prepare()}.
     * 
     * @return true if there are lazy select cascades annotated
     * @since 1.8 and 2.2
     */
    public boolean isLazySelectsCascades()
    {
        return lazySelectsCascades;
    }


    /**
     * Reports that {@link #isLazySelectsCascades()} is true and row is instance of {@link LazySelectable}. This is
     * set as an optimization so the {@link #readNext()} only tests a boolean to know when to invoke 
     * {@link LazySelectable#pendingLazySelects(org.sormula.Database)}.
     * 
     * @return true if {@link LazySelectable#pendingLazySelects(org.sormula.Database)} will be inovked for each row selected
     * @since 1.8 and 2.2
     */
    public boolean isNotifyLazySelects()
    {
        return notifyLazySelects;
    }
    

    /**
     * Adds a filter to use for a row type. Every row of type, rowClass, is 
     * is tested as the row is read from the database. 
     * <p>
     * It is tested up to two times. It is always tested immediately after it is read.
     * It is tested a second time if the row has cascades after all cascades have been performed
     * on the row.
     * <p>
     * An example use:
     * <blockquote><pre>
     * SelectOperation&lt;Order.class&gt; op = ...
     * op.addFilter(Order.class, (order, cascadesCompleted) -&gt;
     *   {
     *       if (!cascadesCompleted)
     *       {
     *           // tests prior to cascades
     *           return ...
     *       }
     *       else
     *       {
     *           // tests after cascades
     *           return ...
     *       }
     *   });
     * </pre></blockquote>
     * All filters that here are automatically added to lower level cascades when the
     * operation is prepared. The purpose of this is so that a top-level select operation 
     * can define filters for any/all lower level cascaded row types.
     * 
     * @param rowClass type of row to test
     * @param filterPredicate predicate to perform the test
     * @param <F> row type to filter
     * @since 4.0
     */
    public <F> void addFilter(Class<F> rowClass, BiPredicate<F, Boolean> filterPredicate)
    {
		if (filterPredicateMap == null) filterPredicateMap = new HashMap<>(); // first time  
		filterPredicateMap.put(rowClass, filterPredicate);
		updateActiveFilterPredicate(rowClass, filterPredicate);
    }
    
    
    /**
     * Removes a filter that was previously added with {@link #addFilter(Class, BiPredicate)}.
     * 
     * @param rowClass remove filter defined for this row class
     * @since 4.0
     * @param <F> row type to filter
     */
    public <F> void removeFilter(Class<F> rowClass)
	{
		if (filterPredicateMap != null)
		{
			filterPredicateMap.remove(rowClass);
			if (filterPredicateMap.size() == 0) filterPredicateMap = null; // null means none
			else updateActiveFilterPredicate(rowClass, null); // remove if active
		}
    }
    

    /**
     * Gets all filters defined for this operation. Typically this method is used internally by
     * cascade operations.
     * 
     * @return map of row class to filter
     * @since 4.0
     */
    public Map<Class<?>, BiPredicate<?, Boolean>> getFilterPredicateMap() 
    {
		return filterPredicateMap;
	}


    /**
     * Sets all filters defined for this operation and lower level cascades. Typically this method is
     * used internally by when preparing cascade operations.
     * 
     * @param filterPredicateMap map of row class to filter
     * @since 4.0
     */
	public void setFilterPredicateMap(Map<Class<?>, BiPredicate<?, Boolean>> filterPredicateMap) 
	{
		this.filterPredicateMap = filterPredicateMap;
		
		// reset active filterPredicate
		this.filterPredicate = null; // assume none
		
		if (filterPredicateMap != null)  // filterPredicateMap will be null if no lambda filters
		{
			for (Map.Entry<Class<?>, BiPredicate<?, Boolean>> me : filterPredicateMap.entrySet())
			{
				if (updateActiveFilterPredicate(me.getKey(), me.getValue()))
				{
					// found active filter
					break;
				}
			}
		}
	}

	
	/**
	 * Tests if filterPredicate parameter is to be used for this operation. If true is returned
	 * then #filterPredicate member is set to filterPredicate parameter.
	 * 
	 * @param rowClass row class that is to use filterPredicate
	 * @param filterPredicate filter 
	 * @return true if rowClass parameter is same as the row class of row translator ({@link #rowTranslator})
	 * or if rowClass is Object.class (indicates that filter is to be used for all row types)
	 * 
	 * @since 4.0
	 */
	@SuppressWarnings("unchecked")
	protected boolean updateActiveFilterPredicate(Class<?> rowClass, BiPredicate<?, Boolean> filterPredicate)
	{
		String rowClassName = rowTranslator.getRowClass().getName();
        String filterRowClassName = rowClass.getName();
        if (filterRowClassName.equals(rowClassName)     // use filter for this row class only 
         || filterRowClassName.equals(Object.class.getName())) // use filter for all classes (wild card)
        {
        	this.filterPredicate = (BiPredicate<R, Boolean>)filterPredicate;
        	return true;
        }
        
        return false;
	}
	

	/**
     * Gets the {@link OrderByTranslator}. See {@link #setOrderByTranslator(OrderByTranslator)} for details.
     * 
     * @return order translator or null if no ordering desired
     */
    protected OrderByTranslator<R> getOrderByTranslator()
    {
        return orderByTranslator;
    }


    /**
     * Sets the {@link OrderByTranslator} that creates the sql "order by" phrase based upon
     * the {@link OrderBy} annotations. Default is null. Set by {@link #setOrderBy(String)}.
     * 
     * @param orderByTranslator order translator or null if no ordering desired
     */
    protected void setOrderByTranslator(OrderByTranslator<R> orderByTranslator)
    {
        this.orderByTranslator = orderByTranslator;
    }


    /**
     * {@inheritDoc}
     * Order by clause is appended to super{@link #getSql()}.
     */
    @Override
    protected String getSql()
    {
        String sql = super.getSql();
        
        if (orderByTranslator != null)
        {
            // order by specified
            sql += " " + orderByTranslator.createSql();
        }
        
        return sql;
    }
    
    
    /**
     * Gets the result set of the most recent select.
     * 
     * @return JDBC result set
     */
    protected ResultSet getResultSet()
    {
        return resultSet;
    }


    /**
     * Sets the base sql with {@link #setBaseSql(String)}.
     */
    protected void initBaseSql()
    {
        String tableName = getTable().getQualifiedTableName();
        RowTranslator<R> rowTranslator = getTable().getRowTranslator();
        rowTranslator.setIncludeIdentityColumns(isIncludeIdentityColumns()); // usually true for selects
        rowTranslator.setIncludeReadOnlyColumns(true);
        String selectColumnPhrase = rowTranslator.createColumnPhrase(); 
        StringBuilder sql = new StringBuilder(selectColumnPhrase.length() + tableName.length() + 50);
        
        sql.append("SELECT ");
        sql.append(selectColumnPhrase);
        sql.append(" FROM ");
        sql.append(tableName);
        
        setBaseSql(sql.toString());
    }

    
    /**
     * Invoked prior to reading columns into row. Override to modify the row prior to JDBC reads.
     * Default implementation does nothing.
     * 
     * @param row row that will get column values from table
     */
    protected void preRead(R row)
    {
    }
    
    
    /**
     * Invoked after reading columns into row. Override to modify the row after to JDBC reads.
     * Default implementation does nothing.
     * 
     * @param row row that got column values from table
     */
    protected void postRead(R row)
    {
    }

    
    /**
     * Invoked prior to reading row. Performs all select cascade operations 
     * where {@link SelectCascade#post()} is false.
     * 
     * @param row row that will get column values from table 
     * @throws OperationException if error
     */
    protected void preReadCascade(R row) throws OperationException
    {
        cascade(row, false);
    }


    /**
     * Invoked after reading row. Performs all select cascade operations 
     * where {@link SelectCascade#post()} is true.
     * 
     * @param row row that got column values from table
     * @throws OperationException if error 
     */
    protected void postReadCascade(R row) throws OperationException
    {
        cascade(row, true);
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected List<CascadeOperation<R, ?>> prepareCascades(Field field) throws OperationException
    {
        List<CascadeOperation<R, ?>> co = null;
        SelectCascadeAnnotationReader car = new SelectCascadeAnnotationReader(field);
        SelectCascade[] selectCascades = car.getSelectCascades();
        
        if (selectCascades.length > 0 && isRequiredCascade(car.getName()))
        {
            // at least one select cascade and (unnamed or is required)
            if (log.isDebugEnabled()) log.debug("prepareCascades() for " + field);
            @SuppressWarnings("unchecked") // target field type is not known at compile time
            Table<R> targetTable = (Table<R>)getTargetTable(car.getTargetClass());
            RowField<R, ?> targetField = createRowField(targetTable, field);
            co = new ArrayList<>(selectCascades.length);
            
            // for each cascade operation
            for (SelectCascade c: selectCascades)
            {
                if (c.lazy())
                {
                    lazySelectsCascades = true;
                }
                else
                {
                    // prepare non lazy cascade for execution
                    if (log.isDebugEnabled()) log.debug("prepare cascade " + c.operation().getCanonicalName() + 
                            " for target field " + targetField.getField());
                    @SuppressWarnings("unchecked") // target field type is not known at compile time
                    SelectCascadeOperation<R, ?> operation = new SelectCascadeOperation(this, targetField, targetTable, c);
                    if (c.setForeignKeyValues()) operation.setForeignKeyFieldNames(car.getForeignKeyValueFields());
                    if (c.setForeignKeyReference()) operation.setForeignKeyReferenceFieldName(car.getForeignKeyReferenceField());

                    operation.prepare();
                    co.add(operation);
                }
            }
            
            if (lazySelectsCascades)
            {
                // test if row class instance of LazySelectable
                notifyLazySelects = LazySelectable.class.isAssignableFrom(getTable().getRowClass());
                
                if (!notifyLazySelects)
                {
                    throw new OperationException(getTable().getRowClass() + " must implement " + LazySelectable.class + 
                            " when SelectCascade#lazy() is true");
                }
            }
        }
        else
        {
            // no cascades
            co = Collections.emptyList();
        }
        
        return co;
    }
}
