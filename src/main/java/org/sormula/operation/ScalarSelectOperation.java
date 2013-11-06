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
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
import org.sormula.log.ClassLogger;
import org.sormula.operation.cascade.CascadeOperation;
import org.sormula.operation.cascade.SelectCascadeOperation;
import org.sormula.operation.cascade.lazy.LazySelectable;
import org.sormula.operation.filter.SelectCascadeFilter;
import org.sormula.operation.monitor.OperationTime;
import org.sormula.reflect.SormulaField;
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
public class ScalarSelectOperation<R> extends SqlOperation<R>
{
    private static final ClassLogger log = new ClassLogger();
    
    ResultSet resultSet;
    R rowParameters;
    String orderByName;
    OrderByTranslator<R> orderByTranslator;
    RowTranslator<R> rowTranslator;
    int maximumRowsRead = Integer.MAX_VALUE;
    int rowsReadCount;
    boolean lazySelectsCascades;
    boolean notifyLazySelects;
    boolean cacheContainsPrimaryKey; // set by execute() if cache hit
    boolean executed;
    SelectCascadeFilter<?>[] selectCascadeFilters;
    SelectCascadeFilter<R> filter;
    
    
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
     * {@link SelectOperation#readAll()}. Limiting rows read is usefull to avoid
     * reading too many rows and thus creating memory or display problems.
     * <P>
     * In the future, when more databases support a standard way to limit rows,
     * I will add support for SQL level limits through a method like 
     * "setMaximumRows(int)" and SQL "FETCH FIRST n ROWS ONLY".
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
        executed = true;
        cacheContainsPrimaryKey = false;
        initOperationTime();
        setNextParameter(1);
        rowsReadCount = 0;
        
        if (isCached())
        {
            try
            {
                // notify cache of start of execution
                getTable().getCache().execute(this);
            }
            catch (CacheException e)
            {
                throw new OperationException("error notifying cache", e);
            }

            if (isPrimaryKey() && rowParameters == null)
            {
                // table is cached and select is for primary key and primary is from parameters, look in cache
                try
                {
                    if (log.isDebugEnabled()) log.debug("execute() check cache " + table.getRowClass());
                    cacheContainsPrimaryKey = table.getCache().contains(parameters);
                }
                catch (CacheException e)
                {
                    throw new OperationException("error reading cache", e);
                }
            }
        }
        
        if (!cacheContainsPrimaryKey)
        {
            // query database
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
                log.debug("execute query");
                if (maximumRowsRead < Integer.MAX_VALUE) preparedStatement.setMaxRows(maximumRowsRead);
                operationTime.startExecuteTime();
                resultSet = preparedStatement.executeQuery();
                operationTime.stop();
            }
            catch (Exception e)
            {
                throw new OperationException("execute() error", e);
            }
        }
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
        
        try
        {
            if (cacheContainsPrimaryKey)
            {
                // read from cache using primary key 
                if (rowsReadCount == 0) // readNext() returns null after 1st invocation because only 1 row with primary key
                {
                    // return value from most recent execute()
                    row = table.getCache().select(parameters);
                    
                    // test for filter pass 
                    if (filter != null)
                    {
                        if (!filter.accept(this, row, false) ||
                            (isCascading() && !filter.accept(this, row, true)) )
                        {
                            // don't use this row based upon filter response
                            row = null;
                        }
                    }
                }
            }
            else
            {
                // select from result set
                
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
                            R cachedRow = table.getCache().selected(row);
                            
                            if (cachedRow != null)
                            {
                                // assume cached row is authority
                                row = cachedRow;
                            }   
                            else
                            {
                                // cache indicates row has been deleted, dont use it, try next row
                                if (log.isDebugEnabled()) log.debug("selected deleted row, select next");
                                row = null;
                            }
                        }

                        if (row != null)  
                        {
                            // row has not been deleted in cache
                            
                            // test for filter pass
                            if (filter == null || filter.accept(this, row, false))
                            {
                                // no filter or passes filter 
                                if (isCascading()) 
                                {
                                    if (!isCached()) postReadCascade(row); // only cascade non-cached rows
                                    complete = filter == null || filter.accept(this, row, true);
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
                                if (log.isDebugEnabled()) log.info("row did not pass filter " + row);
                            }
                        }
                    
                        if (!complete)
                        {
                            // did not get desired row, try next row
                            operationTime.startReadTime(); // see comment above concerning startReadTime
                            
                            if (!resultSet.next())
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
        
        if (row != null) ++rowsReadCount;
        
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
                    setOrderByTranslator(new OrderByTranslator<R>(table.getRowTranslator(), orderByAnnotation));
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
     * Gets order by name set with {@link #setOrderBy(String)}.
     * 
     * @return order by name 
     */
    public String getOrderByName()
    {
        return orderByName;
    }


    /**
     * Set parameters, executes, reads one row, closes.
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
        execute();
        R row = readNext();
        close();
        return row;
    }
    
    
    /**
     * Set parameters, executes, reads one row, closes.
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
        execute();
        R row = readNext();
        close();
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
     * Reports that {@link #isLazySelectsCascades()} is true and row is instanceof {@link LazySelectable}. This is
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
     * Sets filter to be used for selected rows. Cascade filters allow you to write filter
     * algroithms in Java (instead of SQL). Sometimes it is easier and more powerful to write 
     * filtering in Java instead of SQL joins and SQL where conditions. For example, a filter 
     * allows a graph of objects to be read with selective pruning of nodes.
     * <p>
     * The filters set with this method will be used on all cascades that result from this
     * operation.
     * <p> 
     * Note: if table is cached, then cache will contain filtered rows. For subsequent selects, you 
     * may want to clear cache with {@link Table#getCache} and {@link Cache#evictAll()} or start with 
     * new instance of {@link Table} to avoid reading filtered rows.
     * 
     * @param selectCascadeFilters filter(s) to use or null for none
     * @since 3.1
     */
    @SuppressWarnings("unchecked")
    public void setSelectCascadeFilters(SelectCascadeFilter<?>... selectCascadeFilters)
    {
        this.selectCascadeFilters = selectCascadeFilters;
        
        if (selectCascadeFilters != null)
        {
            // find filter for this operation (linear search ok since small number?)
            String objectClassName = Object.class.getName(); // wildcard match
            String rowClassName = rowTranslator.getRowClass().getName();
            filter = null;
            for (SelectCascadeFilter<?> f : selectCascadeFilters)
            {
                String filterRowClassName = f.getRowClass().getName();
                
                if (filterRowClassName.equals(rowClassName)     // use filter for row class  
                 || filterRowClassName.equals(objectClassName)) // use filter for all classes
                {
                    // found
                    filter = (SelectCascadeFilter<R>)f;
                    break;
                }
            }
        }
    }
    

    /**
     * Gets the select filters used for this operation.
     * 
     * @return filters used; null for none
     * @since 3.1
     */
    public SelectCascadeFilter<?>[] getSelectCascadeFilters()
    {
        return selectCascadeFilters;
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
     * Default implementaion does nothing.
     * 
     * @param row row that will get column values from table
     */
    protected void preRead(R row)
    {
    }
    
    
    /**
     * Invoked after reading columns into row. Override to modify the row after to JDBC reads.
     * Default implementaion does nothing.
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
            if (log.isDebugEnabled()) log.debug("prepareCascades() for " + field.getName());
            Table<?> targetTable = getTargetTable(car.getTargetClass(), field);
            SormulaField<R, ?> targetField = createTargetField(field);
            co = new ArrayList<CascadeOperation<R, ?>>(selectCascades.length);
            
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
                    if (log.isDebugEnabled()) log.debug("prepare cascade " + c.operation());
                    @SuppressWarnings("unchecked") // target field type is not known at compile time
                    SelectCascadeOperation<R, ?> operation = new SelectCascadeOperation(getTable(), targetField, targetTable, c);
                    operation.setSelectCascadeFilters(selectCascadeFilters);
                    operation.setNamedParameterMap(getNamedParameterMap());
                    if (c.setForeignKeyValues()) operation.setForeignKeyFieldNames(car.getForeignKeyValueFields());
                    if (c.setForeignKeyReference()) operation.setForeignKeyReferenceFieldName(car.getForeignKeyReferenceField());

                    // cascade operation uses same required cascade names as this operation
                    operation.setRequiredCascades(getRequiredCascades());

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
