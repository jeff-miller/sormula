/* sormula - Simple object relational mapping
 * Copyright (C) 2011 Jeff Miller
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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.sormula.Database;
import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.annotation.Column;
import org.sormula.annotation.Where;
import org.sormula.annotation.WhereAnnotationReader;
import org.sormula.annotation.cascade.Cascade;
import org.sormula.annotation.cascade.OneToManyCascade;
import org.sormula.annotation.cascade.OneToOneCascade;
import org.sormula.log.ClassLogger;
import org.sormula.operation.cascade.CascadeOperation;
import org.sormula.operation.monitor.NoOperationTime;
import org.sormula.operation.monitor.OperationTime;
import org.sormula.reflect.ReflectException;
import org.sormula.reflect.SormulaField;
import org.sormula.translator.AbstractWhereTranslator;
import org.sormula.translator.RowTranslator;
import org.sormula.translator.TranslatorException;
import org.sormula.translator.WhereTranslator;


/**
 * Base class for all database operations.
 * 
 * @since 1.0
 * @author Jeff Miller
 * @param <R> class type which contains members for columns of a row in a table
 */
public abstract class SqlOperation<R>
{
    private static final ClassLogger log = new ClassLogger();
    static NoOperationTime noOperationTime = new NoOperationTime(); // TODO?
    
    Table<R> table;
    Connection connection;
    String whereConditionName;
    AbstractWhereTranslator<R> whereTranslator;
    String baseSql;
    String customSql;
    PreparedStatement preparedStatement;
    int nextParameter;
    Object[] parameters;
    List<CascadeOperation<R, ?>> cascadeOperations;
    boolean includeIdentityColumns;
    boolean autoGeneratedKeys;
    boolean cascadesPrepared;
    String preparedSql;
    String timingId;
    OperationTime operationTime;
    boolean timings;
    

    /**
     * Constructs for a table.
     * 
     * @param table operations are performed on this table
     * @throws OperationException if error
     */
    public SqlOperation(Table<R> table) throws OperationException
    {
        this.table = table;
        setIncludeIdentityColumns(true);
        setAutoGeneratedKeys(false);
        Database database = table.getDatabase();
        connection = database.getConnection();
        setTimings(database.isTimings()); // default to database setting
    }
    
    
    /**
     * Sets any parameters to be used by operation. Parameters must be in the same
     * order as defined by {@link Column#primaryKey()} or {@link Where} annotation.
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
     * Replaced with {@link #writeParameters()}.
     */
    @Deprecated
    protected void prepareParameters() throws OperationException
    {
        writeParameters();
    }
    
    
    /**
     * Prepares statement and then sets all parameters with {@link PreparedStatement#setObject(int, Object)}.
     * {@link #prepareCheck()} or {@link #prepare()} must be invoked prior to using this method.
     * 
     * @throws OperationException if error
     */
    protected void writeParameters() throws OperationException
    {
        int parameterIndex = 1;
        
        if (getParameters() != null)
        {
	        if (log.isDebugEnabled()) log.debug("write parameters from objects");
	        AbstractWhereTranslator<R> wt = getWhereTranslator();
	        boolean inOperator = wt != null && wt.isCollectionOperand();
	        
	        try
	        {
	            for (Object p: parameters)
	            {
	                if (log.isDebugEnabled()) log.debug("setParameters parameterIndex=" + parameterIndex + " value='" + p + "'");
	                
	                if (inOperator && p instanceof Collection<?>)
	                {
	                    // assume parameter is for IN (?, ?,...), set each value within collection
	                    for (Object inParameter: (Collection<?>)p)
	                    {
	                        preparedStatement.setObject(parameterIndex, inParameter);
	                        ++parameterIndex;
	                    }
	                }
	                else
	                {
	                    preparedStatement.setObject(parameterIndex, p);
	                    ++parameterIndex;
	                }
	            }
	            
	            setNextParameter(parameterIndex);
	        }
	        catch (Exception e)
	        {
	            throw new OperationException("setParameters(Object... parameters) error for parameter index=" +
	                    parameterIndex, e);
	        }
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
     * Cleans up after operation is no longer needed. The connection is not closed but all other
     * objects created by this operations are closed. Prepared statement is closed and
     * close method is invoked on all {@link CascadeOperation} objects. This method should
     * be invoked when operation is no longer needed for proper JDBC clean up.
     * 
     * @throws OperationException if error
     */
    public void close() throws OperationException
    {
        closeStatement();
        closeCascades();
    }

    
    public String getTimingId()
    {
        return operationTime.getId();
    }

    
    public void setTimingId(String timingId)
    {
        this.timingId = timingId;
    }


    // TODO
    public void setTimings(boolean on)
    {
        this.timings = on;
    }
    
    
    public boolean getTimings()
    {
        return timings;
    }

    
    public void logTimings()
    {
        operationTime.logTimings();
    }
    
    
    public OperationTime getOperationTime()
    {
        return operationTime;
    }

    
    protected void zzz()
    {
        if (timings)
        {
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
            Database database = getTable().getDatabase();
            OperationTime databaseTime = database.getOperationTime(id);
            
            if (databaseTime == null)
            {
                // no sum yet, create one
                databaseTime = database.createOperationTime(id, "totals for all uses of " + sql);
            }

            // create timing for this operation
            operationTime = new OperationTime(id, databaseTime);
            operationTime.setDescription(sql);
        }
        else
        {
            // no timings will be recorded
            operationTime = noOperationTime; 
        }
    }
    
    
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
    
    
    protected void closeCascades() throws OperationException
    {
        // close cascades
        if (cascadeOperations != null)
        {
            for (CascadeOperation<R, ?> o: cascadeOperations)
            {
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
            // first time, prepare sql
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
            if (log.isDebugEnabled()) log.debug("prepare " + preparedSql);
            
            if (isAutoGeneratedKeys())
            {
                preparedStatement = connection.prepareStatement(preparedSql, Statement.RETURN_GENERATED_KEYS);
            }
            else
            {
                // use prepareStatement(String) method for capatibility with 
                // jdbc drivers that don't support identity columns
                preparedStatement = connection.prepareStatement(preparedSql);
            }
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
     * Prepares cascades for all cascade annotations on row class. Cascade annotations
     * are {@linkplain OneToManyCascade}, {@linkplain OneToOneCascade}, and {linkplain Cascade}.
     *  
     * @throws OperationException if error
     */
    protected void prepareCascades() throws OperationException
    {
        // for all fields
        for (Field f: getTable().getRowTranslator().getRowClass().getDeclaredFields())
        {
            if (f.isAnnotationPresent(OneToManyCascade.class) ||
                f.isAnnotationPresent(OneToOneCascade.class) ||
                f.isAnnotationPresent(Cascade.class))
            {
                // prepare cascades
                if (log.isDebugEnabled()) log.debug("prepareCascades() for " + f.getName());
                
                if (cascadeOperations == null)
                {
                    // create only if needed so that no penalty for pre/postExecuteCascade and no memory usage
                    cascadeOperations = new ArrayList<CascadeOperation<R,?>>();
                }
                
                cascadeOperations.addAll(prepareCascades(f));
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
                    o.cascade(row);
                }
            }
        }
    }
    
    
    /**
     * Gets a table object from database associated with this operation.
     * 
     * @param targetClass class that cascade is to affect
     * @param targetField target of cascade (obtain target field type if not specified by annotation)
     * @return table for target class of annotation
     * @throws OperationException if error
     */
    protected Table<?> getTargetTable(Class<?> targetClass, Field targetField) throws OperationException
    {
        Table<?> targetTable;
        
        try
        {
            if (targetClass.getName().equals("java.lang.Object"))
            {
                // get target class based upon field type
                // if field is parameterized, then getTable will not obtain correct table
                targetClass = targetField.getType();
            }
            
            targetTable = getTable().getDatabase().getTable(targetClass);
        }
        catch (SormulaException e)
        {
            throw new OperationException("error getting table object for targetClass=" + 
                    targetClass.getCanonicalName() + " in class=" + 
                    getTable().getRowTranslator().getRowClass().getCanonicalName(), e);
        }
        
        return targetTable;
    }
    
    
    /**
     * Creates a {@linkplain SormulaField} from {@linkplain Field}.
     * 
     * @param field creates for this field
     * @return sormula field based upon field parameter
     * @throws OperationException if error
     */
    protected SormulaField<R, ?> createTargetField(Field field) throws OperationException
    {
        SormulaField<R, ?> targetField;
        
        try
        {
            targetField = new SormulaField<R, Object>(field);
        }
        catch (ReflectException e)
        {
            throw new OperationException("error constructing SormulaField for " + field, e);
        }
        
        return targetField;
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
     * Gets the JDBC connection used in this operation.
     *  
     * @return {@link Connection}
     */
    protected Connection getConnection()
    {
        return connection;
    }


    /**
     * Sets where condition from annotation name as defined in {@linkplain Where#name()} for row.
     * 
     * @param whereConditionName name of where condition to use; 
     * "primaryKey" for key defined by {@linkplain Column#primaryKey()}; 
     * empty string for no where condition
     */
    public void setWhere(String whereConditionName) throws OperationException
    {
        this.whereConditionName = whereConditionName;
        
        if (whereConditionName.equals("primaryKey"))
        {
            setWhereTranslator(table.getRowTranslator().getPrimaryKeyWhereTranslator());
        }
        else if (whereConditionName.length() > 0)
        {
            try
            {
                // look in operation class first
                Where whereAnnotation = new WhereAnnotationReader(this.getClass()).getAnnotation(whereConditionName);
                if (whereAnnotation != null)
                {
                    if (log.isDebugEnabled()) log.debug(whereConditionName + 
                            " Where annotation from operation class, " + this.getClass()); 
                    setWhereTranslator(new WhereTranslator<R>(table.getRowTranslator(), whereAnnotation));
                }
                else
                {
                    // look in row class if not found in operation class
                    if (log.isDebugEnabled()) log.debug(whereConditionName + 
                            " where annotation from row class," + table.getRowTranslator().getRowClass());
                    whereAnnotation = new WhereAnnotationReader(table.getRowTranslator().getRowClass()).getAnnotation(whereConditionName);
                    if (whereAnnotation != null)
                    {
                        setWhereTranslator(new WhereTranslator<R>(table.getRowTranslator(), whereAnnotation));
                    }
                    else
                    {
                        throw new OperationException("no Where annotation named, " + whereConditionName);
                    }
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
            setWhereTranslator(null);
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
     * Sets when to generate identity columns. 
     * 
     * @param includeIdentityColumns true to include identity columns in sql
     */
    public void setIncludeIdentityColumns(boolean includeIdentityColumns)
    {
        this.includeIdentityColumns = includeIdentityColumns;
    }


    /**
     * If true, {@link Statement#getGeneratedKeys()} is used to read column defined as 
     * identity and then sets the generated key value in the row. Default is false. Typically
     * true for insert operations.
     * 
     * @return true if identity column is generated by database
     * @see InsertOperation#processIdentityColumn(Object)
     */
    public boolean isAutoGeneratedKeys()
    {
        return autoGeneratedKeys;
    }


    /**
     * Sets when to get generated keys from database.
     * 
     * @param autoGeneratedKeys true if identity column is generated by database
     */
    public void setAutoGeneratedKeys(boolean autoGeneratedKeys)
    {
        this.autoGeneratedKeys = autoGeneratedKeys;
    }


    /**
     * Replaced with {@link #writeColumns(Object)}.
     */
    @Deprecated
    protected void prepareColumns(R row) throws OperationException
    {
        writeColumns(row);
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
            RowTranslator<R> rowTranslator = getTable().getRowTranslator();
            rowTranslator.setIncludeIdentityColumns(includeIdentityColumns);
            setNextParameter(rowTranslator.write(preparedStatement, getNextParameter(), row));
        }
        catch (Exception e)
        {
            throw new OperationException("error setting parameters for columns", e);
        }
    }


    /**
     * Replaced with {@link #writeWhere(Object)}.
     */
    @Deprecated
    protected void prepareWhere(R row) throws OperationException
    {
        writeWhere(row);
    }
    
    
    /**
     * Sets all where parameters from a row using the table's {@link WhereTranslator}.
     * 
     * @param row get where parameters from this row
     * @throws OperationException if error
     */
    protected void writeWhere(R row) throws OperationException
    {
    	if (log.isDebugEnabled()) log.debug("write parameters from row");
    	
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
    protected void setBaseSql(String sql)
    {
        this.baseSql = sql;
    }


    /**
     * Gets the prepared statement used by this operation.
     * 
     * @return prepared statement or null if statement has not been prepapred
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
    protected AbstractWhereTranslator<R> getWhereTranslator()
    {
        return whereTranslator;
    }
    protected void setWhereTranslator(AbstractWhereTranslator<R> whereTranslator)
    {
        this.whereTranslator = whereTranslator;
    }


    /**
     * Gets the next JDBC parameter number used by {@link PreparedStatement} to set parameters.
     * Parameter number changes as column and where conditions are prepared.
     *  
     * @return the next {@link PreparedStatement} parameter to use 
     */
    protected int getNextParameter()
    {
        return nextParameter;
    }
    protected void setNextParameter(int nextParameter)
    {
        this.nextParameter = nextParameter;
    }
}
