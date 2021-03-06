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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sormula.Database;
import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.annotation.Column;
import org.sormula.annotation.Row;
import org.sormula.annotation.Where;
import org.sormula.annotation.WhereAnnotationReader;
import org.sormula.annotation.WhereField;
import org.sormula.annotation.cascade.Cascade;
import org.sormula.annotation.cascade.OneToManyCascade;
import org.sormula.annotation.cascade.OneToOneCascade;
import org.sormula.annotation.cascade.SelectCascade;
import org.sormula.cache.CacheException;
import org.sormula.cache.writable.WriteOperations;
import org.sormula.log.SormulaLogger;
import org.sormula.log.SormulaLoggerFactory;
import org.sormula.operation.cascade.CascadeOperation;
import org.sormula.operation.monitor.NoOperationTime;
import org.sormula.operation.monitor.OperationTime;
import org.sormula.reflect.DirectAccessField;
import org.sormula.reflect.FieldExtractor;
import org.sormula.reflect.MethodAccessField;
import org.sormula.reflect.ReflectException;
import org.sormula.reflect.RowField;
import org.sormula.translator.AbstractWhereTranslator;
import org.sormula.translator.ColumnTranslator;
import org.sormula.translator.RowTranslator;
import org.sormula.translator.TranslatorException;
import org.sormula.translator.TypeTranslator;
import org.sormula.translator.WhereTranslator;


/**
 * Base class for all database operations.
 * 
 * @since 1.0
 * @author Jeff Miller
 * @param <R> class type which contains members for columns of a row in a table
 */
public abstract class SqlOperation<R> implements AutoCloseable
{
    private static final SormulaLogger log = SormulaLoggerFactory.getClassLogger();
    private static NoOperationTime noOperationTime = new NoOperationTime();
    
    Table<R> table;
    String whereConditionName;
    AbstractWhereTranslator<R> whereTranslator;
    String baseSql;
    String customSql;
    PreparedStatement preparedStatement;
    int nextParameter;
    Object[] parameters;
    Map<String, Object> namedParameterMap;
    List<CascadeOperation<R, ?>> cascadeOperations;
    boolean includeIdentityColumns;
    boolean cascadesPrepared;
    String preparedSql;
    String timingId;
    OperationTime operationTime;
    boolean timings;
    boolean readOnly;
    int queryTimeout;
    boolean primaryKey;
    boolean cached;
    boolean cascade;
    Where whereAnnotation;
    String[] requiredCascades;
    int cascadeDepth;
    

    /**
     * Constructs for a table. 
     * <p>
     * The operation timings default to the current state of the database 
     * {@link Database#isTimings()}. If timings are enabled for database then
     * all operations will be timed unless explicitly disabled. Operation 
     * will be read only {@link #setReadOnly(boolean)} if either 
     * {@link Database#isReadOnly()} or {@link Table#isReadOnly()} are true.
     * 
     * @param table operations are performed on this table
     * @throws OperationException if error
     */
    public SqlOperation(Table<R> table) throws OperationException
    {
        this.table = table;
        setIncludeIdentityColumns(true);
        Database database = table.getDatabase();
        setTimings(database.isTimings()); // default to database setting
        setReadOnly(database.isReadOnly() || table.isReadOnly()); // default to database or table
        cached = table.isCached(); // default, change with setCached()
        cascade = true;
        requiredCascades = table.getRequiredCascades(); // default, change with setRequiredCascades()
    }

    
    /**
     * Sets a named parameter value. 
     * <p>
     * Named parameters are used when no positional parameters are set with {@link #setParameters(Object...)}.
     * When no positional parameters are available, {@link #getParameter(String)} is used to get parameter values
     * for all fields defined by {@link Where} using {@link Where#fieldNames()} or {@link WhereField#name()}
     * as named parameter key.
     * <p>
     * Named parameters are also looked up for cascades when 
     * {@link SelectCascade#sourceParameterFieldNames()} contains any field names that begin with dollar ($).
     * symbol. Named parameter map is supplied for all cascade levels so that any level can get value from map.
     * 
     * @param name name of parameter 
     * @param value value of named parameter
     * @since 3.1
     */
    public void setParameter(String name, Object value)
    {
        if (namedParameterMap == null) namedParameterMap = new HashMap<>();
        namedParameterMap.put(name, value);
    }

    
    /**
     * Gets value of named parameter that was set with {@link #setParameter(String, Object)} or 
     * {@link #setNamedParameterMap(Map)}.
     * 
     * @param name parameter name
     * @return parameter value or null if no value for name
     * @since 3.1
     */
    public Object getParameter(String name)
    {
        if (namedParameterMap != null) return namedParameterMap.get(name);
        else return null;
    }
    
    
    /**
     * Sets any parameters to be used by operation. Parameters must be in the same
     * order as defined by {@link Column#primaryKey()} or {@link Where} annotation
     * or {@link Row#primaryKeyFields()}.
     * 
     * @param parameters parameters as objects (not from row fields)
     */
    public void setParameters(Object... parameters)
    {
        this.parameters = parameters;
    }
    
    
    /**
     * Gets parameters that were set by {@link #setParameters(Object...)}.
     * 
     * @return parameters for this operation 
     */
    public Object[] getParameters()
    {
        return parameters;
    }
    

    /**
     * Gets map of all named parameters.
     * 
     * @return map of named parameters (key is parameter name); null for no named parameters
     * @since 3.1
     */
    public Map<String, Object> getNamedParameterMap()
    {
        return namedParameterMap;
    }


    /**
     * Sets all named parameters replacing any previously set with {@link #setParameter(String, Object)}.
     * 
     * @param namedParameterMap map of named parameters (key is parameter name); null for no named parameters
     * parameters
     * @since 3.1
     */
    public void setNamedParameterMap(Map<String, Object> namedParameterMap)
    {
        this.namedParameterMap = namedParameterMap;
    }


    /**
     * Gets read-only indicator.
     * 
     * @return true if modify operations are not permitted
     * @since 1.6 and 2.0
     * @see Database#isReadOnly()
     */
    public boolean isReadOnly()
    {
        return readOnly;
    }


    /**
     * Sets read-only indicator. When true, this operation will
     * fail with a {@link ReadOnlyException} if it attempts to modify the database. 
     * By default read-only is set from the database associated with this
     * operation, {@link Database#isReadOnly()}. Set to true as a safe-guard
     * to prevent accidental modification of database.
     * 
     * @param readOnly true to prevent modify operations
     * @since 1.6 and 2.0
     * @see Database#setReadOnly(boolean)
     */
    public void setReadOnly(boolean readOnly)
    {
        this.readOnly = readOnly;
    }
    
    
    /**
     * Gets the number of seconds the driver will wait for a Statement object to execute.
     *   
     * @return the current query timeout limit in seconds; zero means there is no limit
     * @since 1.9 and 2.3
     * @see Statement#getQueryTimeout()
     */
    public int getQueryTimeout()
    {
        return queryTimeout;
    }


    /**
     * Sets the number of seconds the driver will wait for a Statement object to execute.
     * This value is set when statement is prepared in {@link #prepare()}.
     * 
     * @param queryTimeout the new query timeout limit in seconds; zero means there is no limit
     * @since 1.9 and 2.3
     * @see Statement#setQueryTimeout(int)
     */
    public void setQueryTimeout(int queryTimeout)
    {
        this.queryTimeout = queryTimeout;
    }
    

    /**
     * Gets the depth of the cascade graph relative to the root cascade operation. Root
     * cascade operation is depth 0.
     * 
     * @return cascade depth (0..n)
     * @since 4.1
     */
    public int getCascadeDepth() 
    {
        return cascadeDepth;
    }


    /**
     * Sets the cascade depth. Typically set by {@link CascadeOperation}
     * 
     * @param cascadeDepth 0..n
     * @since 4.1
     */
    public void setCascadeDepth(int cascadeDepth) 
    {
        this.cascadeDepth = cascadeDepth;
    }


    /**
     * Gets the caching mode.
     * 
     * @return true if caching is enabled; false if no caching is performed
     * @since 3.0
     */
    public boolean isCached()
    {
        return cached;
    }


    /**
     * Sets the cache mode for this operation. True by default. Set to false by {@link WriteOperations}
     * when writing cache changes to the database.
     * <p>
     * Setting to true enables caching only if Cached annotation is specified for row/table.
     * 
     * @param cached true to allow caching; false to prevent caching of rows for this operation
     * @since 3.0
     */
    public void setCached(boolean cached)
    {
        this.cached = cached;
    }


    /**
     * Gets cascade status. Default is true unless turned off by {@link #setCascade(boolean)}.
     * 
     * @return true if cascades are performed; false if cascades are ignored
     * @since 3.0
     */
    public boolean isCascade()
    {
        return cascade;
    }


    /**
     * Sets whether cascades are enabled for this operation. Default is true. If false, then
     * cascades will not occur if they are defined for the row.
     * <p>
     * Set to false by {@link WriteOperations} when writing cache changes to the database since
     * cascades for cached tables are performed at the time the row is put into cache.
     * 
     * @param cascade true to perform cascades; false to ignore cascades
     * @since 3.0
     */
    public void setCascade(boolean cascade)
    {
        this.cascade = cascade;
    }
    
    
    /**
     * Indicates that cascades will occur with this operation.
     * 
     * @return {@link #isCascade()} and at least one cascade operation is defined
     * @since 3.1
     */
    protected boolean isCascading()
    {
        return cascade && cascadeOperations != null;
    }
    
    
    /**
     * Prepares statement and then sets all parameters with {@link #writeParameter(int, Object)}.
     * {@link #prepareCheck()} or {@link #prepare()} must be invoked prior to using this method.
     * 
     * @throws OperationException if error
     */
    protected void writeParameters() throws OperationException
    {
        int index = 1;
        
        if (getParameters() != null)
        {
	        if (log.isDebugEnabled()) log.debug("writeParameters() parameters from objects");
	        AbstractWhereTranslator<R> wt = getWhereTranslator();
	        boolean inOperator = wt != null && wt.isCollectionOperand();
	        
	        try
	        {
	            for (Object p: parameters)
	            {
	                if (log.isDebugEnabled()) log.debug("writeParameters() index=" + index + " value='" + p + "'");
	                
	                if (inOperator && p instanceof Collection<?>)
	                {
	                    // assume parameter is for IN (?, ?,...), set each value within collection
	                    for (Object inParameter: (Collection<?>)p)
	                    {
	                        writeParameter(index, inParameter);
	                        ++index;
	                    }
	                }
	                else
	                {
	                    writeParameter(index, p);
	                    ++index;
	                }
	            }
	            
	            setNextParameter(index);
	        }
	        catch (Exception e)
	        {
	            throw new OperationException("writeParameters() error for parameter index=" +
	                    index, e);
	        }
        }
    }

    
    /**
     * Sets parameter on prepared statement using the appropriate {@link TypeTranslator}.
     * 
     * @param parameterIndex jdbc prepared statement parameter index
     * @param parameter parameter value
     * @throws Exception if no translator exists for parameter class or error writing parameter
     * @param <T> type of parameter
     */
    @SuppressWarnings("unchecked") // types are not known until runtime
    protected <T> void writeParameter(int parameterIndex, T parameter) throws Exception
    {
        if (parameter != null)
        {
            Class<T> parameterClass = (Class<T>)parameter.getClass(); 
            
            // look for translator in table
            TypeTranslator<T> typeTranslator = (TypeTranslator<T>)table.getTypeTranslator(parameterClass);
            
            if (typeTranslator != null)
            {
                if (log.isDebugEnabled())
                {
                    log.debug("writeParameter() parameter type="+parameterClass + " value="+parameter);
                }
                
                typeTranslator.write(preparedStatement, parameterIndex, parameter);
            }
            else
            {
                throw new OperationException("no translator for parameter type="+parameterClass + 
                        " index=" + parameterIndex + " value="+parameter);
            }
        }
        else
        {
            // null parameter
            preparedStatement.setObject(parameterIndex, null);
        }
    }
    
    
    /**
     * Invokes an execute method on a prepared statement. The method invoked must be implemented
     * by subclass based upon type of operation. {@link #prepareCheck()} should be invoked
     * prior to executing sql or use some other means of set up for prepared statement.
     * 
     * @throws OperationException if error
     */
    public abstract void execute() throws OperationException;
    
    
    /**
     * Requests cancel for currently executing statement. No synchronization is performed between the
     * thread that invokes this method and other threads that may be modifying this object.
     * 
     * @throws OperationException if error
     * @since 1.9 and 2.3
     * @see Statement#cancel()
     */
    public void cancel() throws OperationException
    {
        if (preparedStatement != null) 
        {
            try
            {
                preparedStatement.cancel();
            }
            catch (SQLException e)
            {
                throw new OperationException("cancel error", e);
            }
        }
    }
    
    
    /**
     * Cleans up after operation is no longer needed. The connection is not closed but all other
     * objects created by this operations are closed. Prepared statement is closed and
     * close method is invoked on all {@link CascadeOperation} objects. This method should
     * be invoked when operation is no longer needed for proper JDBC clean up.
     * 
     * @throws OperationException if error
     */
    public void close() throws OperationException
    {
        if (isCached() && table.isCached())
        {
            try
            {
                table.getCache().close(this);
            }
            catch (CacheException e)
            {
                throw new OperationException("close error", e);
            }
        }

        closeStatement();
        closeCascades();
    }


    /**
     * Gets the timing id for this operation. If none is set then the default is the
     * hexadecimal string of the hash code of {@link #getSql()}.
     * 
     * @return timing id for this operation or null if {@link #isTimings()} is false
     * @since 1.5
     */
    public String getTimingId()
    {
        if (operationTime == null) return timingId;
        return operationTime.getTimingId();
    }

    
    /**
     * Sets the id for the operation times {@link #getOperationTime()}. Typically the default id is
     * sufficient. The id is a key into {@link Database#getOperationTimeMap()} which is used 
     * to sum multiple instances of timings for this operation. 
     * <p>
     * Use a custom id to force all operations that use the custom id to be summed into one 
     * instance of {@link OperationTime} in {@link Database#getOperationTimeMap()}. A good
     * custom id to use would be the class name of the class that is using the operation,
     * {@link Class#getName()}.
     * 
     * @param timingId unique id associated with an operation(s)
     * @since 1.5
     */
    public void setTimingId(String timingId)
    {
        this.timingId = timingId;
    }


    /**
     * Enables timings for this operation. When timings are enabled, prepare, write, execute,
     * and read times are recorded. See {@link #getOperationTime()}. Use {@link #logTimings()} to 
     * write timings to log for this operation. Use {@link Database#logTimings()} to write all operation
     * timings to log.
     * <p>
     * When on parameter is true, then instance of {@link OperationTime} is created 
     * at the start of execution for use by this operation. Use {@link #getOperationTime()} to 
     * change default values.
     * 
     * @param on true to recording execution times for this operation
     * @since 1.5
     */
    public void setTimings(boolean on)
    {
        timings = on;
    }
    
    
    /**
     * @return true if timings are enabled; false if not
     * @since 1.5
     */
    public boolean isTimings()
    {
        return timings;
    }

    
    /**
     * Logs current timings for this operation to log. Alias for {@link OperationTime#logTimings()}.
     * @since 1.5
     */
    public void logTimings()
    {
        operationTime.logTimings();
    }
    
    
    /**
     * Gets the timings for this operation that have accumulated since {@link #setTimings(boolean)}
     * have been enabled.
     * 
     * @return operation time object containing execution times or {@link NoOperationTime} if 
     * timings are not enabled
     * @since 1.5
     */
    public OperationTime getOperationTime()
    {
        return operationTime;
    }

    
    /**
     * Initializes {@link OperationTime} object that will record elapsed times for this operation.
     * This method can't be invoked until timing id and/or sql is known so it is invoked by
     * {@link #execute()}.
     */
    protected void initOperationTime()
    {
        if (timings)
        {
            // timings are enabled 
            if (operationTime == null || operationTime == noOperationTime)
            {
                // operationTime is not created, create it
                String sql = getSql(); // construct sql only once
                String id; // this operation timing id
                
                if (timingId == null)
                {
                    // no timing id specified, use sql hash
                    id = Integer.toHexString(sql.hashCode()).toUpperCase();
                }
                else
                {
                    id = timingId;
                }
    
                // get database sum for id
                Database database = table.getDatabase();
                OperationTime databaseTime = database.getOperationTime(id);
                
                if (databaseTime == null)
                {
                    // no sum yet, create one
                    databaseTime = database.createOperationTime(id, "All uses of " + sql);
                }
    
                // create timing for this operation
                operationTime = new OperationTime(id, databaseTime);
                operationTime.setDescription(sql);
            }
            
            // keep record of where and how often operation time was initiated
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            
            // search until sormula detected on stack 
            // skip top (likely java.lang.Thread.getStackTrace)
            int i = 0;
            for ( ; i < stackTrace.length; ++i)
            {
                if (stackTrace[i].getClassName().startsWith("org.sormula.")) break;
            }
            
            // search for class that used sormula
            for ( ; i < stackTrace.length; ++i)
            {
                String className = stackTrace[i].getClassName();
                
                if (!className.startsWith("org.sormula.") || className.startsWith("org.sormula.tests.") || className.startsWith("org.sormula.examples."))
                {
                    // found class that invoked sormula execute or select
                    operationTime.updateSource(stackTrace[i]);
                    break;
                }
            }
        }
        else
        {
            // no timings will be recorded
            operationTime = noOperationTime; 
        }
    }
    
    
    /**
     * Closes the prepared statement for this operation.
     * 
     * @throws OperationException if error
     */
    protected void closeStatement() throws OperationException
    {
        // close statements
        try
        {
            if (preparedStatement != null)
            {
                preparedStatement.close();
                preparedStatement = null;
            }
        }
        catch (Exception e)
        {
            throw new OperationException("close() error", e);
        }
    }
    
    
    /**
     * Closes the {@link CascadeOperation} objects for this operation.
     * 
     * @throws OperationException if error
     */
    protected void closeCascades() throws OperationException
    {
        // close cascades
        if (cascadeOperations != null)
        {
            for (CascadeOperation<R, ?> o: cascadeOperations)
            {
                if (log.isDebugEnabled()) log.debug("close cascade for " + o.getTargetField().getField());
                o.close();
            }
            
            cascadeOperations = null;
            cascadesPrepared = false;
        }
    }

    
    /**
     * Prepares statement with {@link #prepare()} if it is null. Invoke prior to using prepared statement to 
     * insure that prepared statement has been set up through {@link Connection#prepareStatement(String)}.
     * Usually invoked prior to invoking a method on {@link PreparedStatement}. 
     * 
     * @throws OperationException if error
     */
    protected void prepareCheck() throws OperationException
    {
        AbstractWhereTranslator<R> wt = getWhereTranslator();
        
        if (wt != null && wt.isCollectionOperand())
        {
            // IN used, force prepare again since number of parameters may be different
            closeStatement();
        }
        
        if (getPreparedStatement() == null)
        {
            // first time
            if (wt != null &&                                       // non primary key select
                (parameters == null || parameters.length == 0) &&   // no parameters
                namedParameterMap != null)                          // named parameters available
            {
                // init positional parameters from named parameter map
                // needs to occur here so that prepare knows number of ? placeholders in where w1=?, w2=?, ...
                List<ColumnTranslator<R>> whereColumnTranslators = wt.getColumnTranslatorList();
                
                if (whereColumnTranslators.size() > 0)
                {
                    // at least one where column
                    Object[] p = new Object[whereColumnTranslators.size()];
                    int index = 0;
                    for (ColumnTranslator<R> ct : whereColumnTranslators)
                    {
                        p[index] = getParameter(ct.getField().getName());
                        if (log.isDebugEnabled()) log.debug("named parameter index=" + index + 
                                " name=" + ct.getField().getName() + " value=" + p[index]);
                        ++index;
                    }
                    setParameters(p);
                }
            }
            
            // prepare sql
            prepare();
        }
    }
    
    
    /**
     * Creates prepared statement for this operation and invokes {@link #prepareCascades()}
     * to prepare statements for any cascade operations.
     * 
     * @throws OperationException if error
     */
    protected void prepare() throws OperationException
    {
        operationTime.startPrepareTime();
        setNextParameter(1);
        preparedSql = getSql();
        
        try
        {
            if (log.isDebugEnabled()) log.debug("prepare() " + preparedSql);
            preparedStatement = prepareStatement();
            preparedStatement.setQueryTimeout(queryTimeout);
        }
        catch (Exception e)
        {
            throw new OperationException("prepare() error: " + preparedSql, e);
        }
        
        if (!cascadesPrepared)
        {
            // prepares cascades only once when IN operator used
            prepareCascades();
        }
        
        operationTime.stop();
    }
    
    
    /**
     * Creates the prepared statement. Allows for subclasses to override.
     * 
     * @return prepared statement to use for this operation
     * @throws SQLException if error
     * @since 4.3
     */
    protected PreparedStatement prepareStatement() throws SQLException
    {
        return getConnection().prepareStatement(preparedSql);
    }
    
    
    /**
     * Prepares cascades for all cascade annotations on row class. Cascade annotations
     * are {@link OneToManyCascade}, {@link OneToOneCascade}, and {@link Cascade}.
     *  
     * @throws OperationException if error
     */
    protected void prepareCascades() throws OperationException
    {
        // for all fields
        for (Field f: table.getRowTranslator().getCascadeFieldList())
        {
            if (log.isDebugEnabled()) log.debug("prepare cascades for " + f);
            List<CascadeOperation<R, ?>> fieldCascades = prepareCascades(f);
            
            if (fieldCascades.size() > 0)
            {
                // at least one for the field
                if (cascadeOperations == null)
                {
                    // create only if needed so that no penalty for pre/postExecuteCascade and no memory usage
                    cascadeOperations = new ArrayList<>();
                }
            
                cascadeOperations.addAll(fieldCascades);
            }
        }

        cascadesPrepared = true;
    }


    /**
     * Creates and prepares cascade operations based upon cascade annotations for a field.
     * 
     * @param field annotation is for this field of row class R
     * @return list of cascade operations; empty list for none
     * @throws OperationException if error
     */
    protected abstract List<CascadeOperation<R, ?>> prepareCascades(Field field) throws OperationException;

    
    /**
     * Executes all cascade operations that were created by {@link #prepareCascades()}.
     * 
     * @param row instance of source row
     * @param post true if post cascades are to be executed; false if pre cascades are
     * to be executed
     * @throws OperationException if error
     */
    protected void cascade(R row, boolean post) throws OperationException
    {
        if (cascadeOperations != null && row != null)
        {
            for (CascadeOperation<R, ?> o: cascadeOperations)
            {
                if (post && o.isPost() || !post && !o.isPost())
                {
                    if (log.isDebugEnabled())
                    {
                        String primaryKeys;
                        try
                        {
                            primaryKeys = new FieldExtractor<>(
                                    table.getRowTranslator().getPrimaryKeyWhereTranslator()).toString(row);
                        }
                        catch (ReflectException e)
                        {
                            primaryKeys = "error " + e.getMessage();
                        }

                        Field targetField = o.getTargetField().getField();
                        log.debug("cascade depth=" + cascadeDepth + " isPost=" + o.isPost() +
                            " field=" + targetField.getName() +
                            " class=" + targetField.getDeclaringClass().getCanonicalName() +
                            " primary key(s): " + primaryKeys);
                    }
                    
                    o.cascade(row);
                }
            }
        }
    }
    
    
    /**
     * Gets a table object from database associated with this operation.
     * 
     * @param targetClass class that cascade is to affect
     * @return table for target class of annotation
     * @throws OperationException if error
     * @since 3.4
     */
    protected Table<?> getTargetTable(Class<?> targetClass) throws OperationException
    {
        Table<?> targetTable;
        
        try
        {
            targetTable = table.getDatabase().getTable(targetClass);
        }
        catch (SormulaException e)
        {
            throw new OperationException("error getting table object for targetClass=" + 
                    targetClass.getCanonicalName() + " in class=" + 
                    table.getRowTranslator().getRowClass().getCanonicalName(), e);
        }
        
        return targetTable;
    }
    
    
    /**
     * Creates a {@link RowField} from a {@link Field} and {@link Table}. Returned {@link RowField}
     * will be either {@link MethodAccessField} or {@link DirectAccessField} based upon values
     * used for {@link Column#fieldAccess()} and {@link Row#fieldAccess()}. 
     * <p>
     * Typically this method is used to create a field that will receive value from a cascade.
     * 
     * @param targetTable table that reads/writes rows that contain field
     * @param field create access to this field
     * @return {@link MethodAccessField} or {@link DirectAccessField}
     * annotation(s)
     * 
     * @throws OperationException if error
     * @since 3.4
     */
    protected RowField<R, ?> createRowField(Table<R> targetTable, Field field) throws OperationException
    {
        RowField<R, ?> rowField;
        
        try
        {
            rowField = targetTable.getRowTranslator().createRowField(field);
        }
        catch (TranslatorException e)
        {
            throw new OperationException("error creating field access for " + field, e);
        }
        
        return rowField;
    }

    
    /**
     * Gets full sql statement to prepare. Default implementation is to use base 
     * sql + custom sql + where sql. Subclasses may override to create more detailed sql.
     * 
     * @return sql to use in {@link #prepare()}
     */
    protected String getSql()
    {
        String sql = getBaseSql();
        
        if (customSql != null)
        {
        	sql += " " + customSql;
        }
        
        AbstractWhereTranslator<R> wt = getWhereTranslator(); 
        if (wt != null)
        {
            wt.setParameters(getParameters());
            sql += " " + wt.createSql();
        }
        
        return sql;
    }
    
    
    /**
     * Gets sql that was used in {@link #prepare()}.
     * 
     * @return sql that was used in prepared statement
     * @since 1.5
     */
    public String getPreparedSql()
    {
        return preparedSql;
    }
    
    
    /**
     * Sets sql to be appended to base sql in operation. Use this to add specialized
     * sql for operation.
     * 
     * @param customSql additional sql to be added to base sql or null for none
     */
    public void setCustomSql(String customSql)
    {
    	this.customSql = customSql;
    }
    
    
    /**
     * Gets custom sql set with {@link #setCustomSql(String)}.
     * 
     * @return custom sql or null if none
     */
    public String getCustomSql()
    {
    	return customSql;
    }
    
    
    /**
     * Gets the table provided in the constructor.
     * 
     * @return table to use in this operation
     */
    public Table<R> getTable()
    {
        return table;
    }


    /**
     * Gets the JDBC connection from the {@link Database} associated with this operation.
     *  
     * @return {@link Connection}
     */
    protected Connection getConnection()
    {
        return table.getDatabase().getConnection();
    }


    /**
     * Sets where condition from annotation name as defined in {@link Where#name()} for row.
     * 
     * @param whereConditionName name of where condition to use; 
     * "primaryKey" for key defined by {@link Column#primaryKey()}, {@link Column#identity()}, 
     * or {@link Row#primaryKeyFields()}; empty string for no where condition
     * @throws OperationException if error
     */
    public void setWhere(String whereConditionName) throws OperationException
    {
        this.whereConditionName = whereConditionName;
        
        if (whereConditionName.equals("primaryKey"))
        {
            primaryKey = true;
            setWhereTranslator(table.getRowTranslator().getPrimaryKeyWhereTranslator());
            whereAnnotation = null;
        }
        else if (whereConditionName.length() > 0)
        {
            primaryKey = false;
            
            try
            {
                // look for where annotation
                // in operation, row class, table class (in that order)
                whereAnnotation = new WhereAnnotationReader(
                    this.getClass(), table.getClass(), table.getRowClass()).getAnnotation(whereConditionName);
                
                if (whereAnnotation != null)
                {
                    setWhereTranslator(new WhereTranslator<>(table.getRowTranslator(), whereAnnotation));
                }
                else
                {
                    throw new OperationException("no Where annotation named, " + whereConditionName);
                }
            }
            catch (TranslatorException e)
            {
                throw new OperationException("can't create WhereTranslator for " + whereConditionName, e);
            }
        }
        else
        {
            // no where
            primaryKey = false;
            setWhereTranslator(null);
            whereAnnotation = null;
        }
    }
    
    
    /** 
     * Gets where condition name supplied in {@link #setWhere(String)}
     * 
     * @return where condition name
     */
    public String getWhereConditionName()
    {
        return whereConditionName;
    }
    
    
    /**
     * Gets the where annotation in use by this operation.
     * 
     * @return where annotation set by {@link #setWhere(String)} or null if none
     * @since 3.0
     */
    public Where getWhereAnnotation()
    {
        return whereAnnotation;
    }
    

    /**
     * Gets primary key status. 
     * 
     * @return true if where condition for this operation is for primary key
     * @since 3.0
     */
    public boolean isPrimaryKey()
    {
        return primaryKey;
    }


    /**
     * Tests if identity columns are used in this operation. Default is true. 
     * Typically false for insert and update operations. 
     * 
     * @return true to include identity columns in sql
     * @see Column#identity()
     */
    public boolean isIncludeIdentityColumns()
    {
        return includeIdentityColumns;
    }


    /**
     * Sets when to generate identity columns. Most operations required that the base
     * base sql be recreated after this method has been used. Typically base sql is
     * recreated with a method like initBaseSql().
     * 
     * @param includeIdentityColumns true to include identity columns in sql
     */
    public void setIncludeIdentityColumns(boolean includeIdentityColumns)
    {
        this.includeIdentityColumns = includeIdentityColumns;
    }
    
    
    /**
     * Sets the name(s) of cascades that should occur with this operation. Cascades with names that equal
     * any of the names specified in cascadeNames parameter will be executed. The default value for 
     * required cascade names is {@link Table#getRequiredCascades()}. 
     * <p>
     * For all cascades that are executed, cascadeNames is passed on to the cascade operation 
     * so that all cascades for all levels use the same required cascade names. 
     * <p> 
     * The wildcard "*" parameter will result in {@link StackOverflowError} if cascade relationships form 
     * a cyclic graph and no termination condition exists to end the recursion.
     * 
     * @param cascadeNames name(s) of cascades to use; "*" for wildcard to use all cascades
     * @since 3.0
     */
    public void setRequiredCascades(String... cascadeNames)
    {
        requiredCascades = cascadeNames;
    }
    
    
    /**
     * Gets the required cascade names.
     * 
     * @return names of cascades to use 
     * @since 3.0
     */
    public String[] getRequiredCascades()
    {
        return requiredCascades;
    }
    
    
    /**
     * Tests if a cascade name equals current required cascade names.
     * 
     * @param cascadeName check this name
     * @return true if required cascades is a "*" or if cascadeName equals any of the
     * required cascade names
     * @since 3.0
     */
    public boolean isRequiredCascade(String cascadeName) 
    {
        boolean required;
        assert (requiredCascades != null) : "requiredCascades is null?!";
        if (cascadeName.equals("*"))
        {
            // wildcard (cascade always)
            required = true;
        }
        else if (requiredCascades.length == 1 && requiredCascades[0].equals("*"))
        {
            // wildcard (cascade any name)
            required = true;
        }
        else if (requiredCascades.length == 0)
        {
            // no required cascades requested
            // required only if requested cascade is unnamed (default)
            required = (cascadeName.length() == 0);
        }
        else
        {
            // search for name among required
            required = false; // assume
            
            // linear search should be ok since list should be small
            for (String name : requiredCascades)
            {
                if (name.equals(cascadeName))
                {
                    // found 
                    required = true;
                    break;
                }
            }
        }
        
        return required;
    }
    
    
    /**
     * Sets all column parameters from a row using the table's {@link RowTranslator}.
     * 
     * @param row get column values from this row
     * @throws OperationException if error
     */
    protected void writeColumns(R row) throws OperationException
    {
        try
        {
            RowTranslator<R> rowTranslator = table.getRowTranslator();
            rowTranslator.setIncludeIdentityColumns(includeIdentityColumns);
            rowTranslator.setIncludeReadOnlyColumns(false);
            setNextParameter(rowTranslator.write(preparedStatement, getNextParameter(), row));
        }
        catch (Exception e)
        {
            throw new OperationException("error setting parameters for columns", e);
        }
    }
    
    
    /**
     * Sets all where parameters from a row using the table's {@link WhereTranslator}.
     * 
     * @param row get where parameters from this row
     * @throws OperationException if error
     */
    protected void writeWhere(R row) throws OperationException
    {
    	if (log.isDebugEnabled()) log.debug("writeWhere() parameters from row");
    	
        if (getWhereTranslator() != null)
        {
            try
            {
                // don't use AbstractWhereTranslator.setIncludeIdentityColumns since
                // where translators default to true and usually always should be true for where conditions
                setNextParameter(getWhereTranslator().write(preparedStatement, getNextParameter(), row));
            }
            catch (Exception e)
            {
                throw new OperationException("error setting parameters for where clause", e);
            }
        }
    }
    
    
    /**
     * Gets the sql used by this operation. Typically includes command like select, update, insert, delete,
     * and columns. Does not include where, order by, or other additional sql.
     * 
     * @return base sql used by this operation
     */
    protected String getBaseSql()
    {
        return baseSql;
    }
    
    
    /**
     * Sets the base sql used by this operation. See {@link #getBaseSql()} for details.
     * 
     * @param sql base sql used by this operation
     */
    protected void setBaseSql(String sql)
    {
        this.baseSql = sql;
    }


    /**
     * Gets the prepared statement used by this operation.
     * 
     * @return prepared statement or null if statement has not been prepared
     */
    protected PreparedStatement getPreparedStatement()
    {
        return preparedStatement;
    }

    
    /**
     * Gets the translator to map row object values into where condition.
     * 
     * @return where translator or null if none
     */
    public AbstractWhereTranslator<R> getWhereTranslator()
    {
        return whereTranslator;
    }
    
    
    /**
     * Sets the translator to map row object values into where condition.
     * 
     * @param whereTranslator where translator or null if none
     */
    public void setWhereTranslator(AbstractWhereTranslator<R> whereTranslator)
    {
        this.whereTranslator = whereTranslator;
    }


    /**
     * Gets the next JDBC parameter number used by {@link PreparedStatement} to set parameters.
     * Parameter number changes as column and where conditions are prepared. Parameter numbers
     * start at 1 and occur for every "?" in the SQL statement. The parameter numbers are used
     * as the first parameter in the various {@link PreparedStatement} set methods.
     *  
     * @return the next {@link PreparedStatement} parameter to use  
     */
    protected int getNextParameter()
    {
        return nextParameter;
    }
    
    
    /**
     * Sets the next column index to use in {@link PreparedStatement}. See {@link #getNextParameter()}
     * for details.
     * 
     * @param nextParameter the next {@link PreparedStatement} parameter to use
     */
    protected void setNextParameter(int nextParameter)
    {
        this.nextParameter = nextParameter;
    }
}
