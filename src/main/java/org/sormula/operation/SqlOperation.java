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
import java.util.ArrayList;
import java.util.List;

import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.annotation.Column;
import org.sormula.annotation.Where;
import org.sormula.annotation.cascade.Cascade;
import org.sormula.log.ClassLogger;
import org.sormula.operation.cascade.CascadeOperation;
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
    
    Table<R> table;
    Connection connection;
    String whereConditionName;
    AbstractWhereTranslator<R> whereTranslator;
    String baseSql;
    PreparedStatement preparedStatement;
    int nextParameter;
    Object[] parameters;
    List<CascadeOperation<R, ?>> cascadeOperations;
    
    
    /**
     * Constructs for a table.
     * 
     * @param table operations are performed on this table
     * @throws OperationException if error
     */
    public SqlOperation(Table<R> table) throws OperationException
    {
        this.table = table;
        connection = table.getDatabase().getConnection();
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
     * Prepares statement and then sets all parameters with {@link PreparedStatement#setObject(int, Object)}.
     * 
     * @throws OperationException if error
     */
    protected void prepareParameters() throws OperationException
    {
        prepareCheck();
        int parameterIndex = 1;
        
        if (getParameters() != null)
        {
	        if (log.isDebugEnabled()) log.debug("prepare parameters from objects");
	        try
	        {
	            for (Object p: parameters)
	            {
	                if (log.isDebugEnabled()) log.debug("setParameters parameterIndex=" + parameterIndex + " value='" + p + "'");
	                preparedStatement.setObject(parameterIndex++, p);
	            }
	            
	            setNextParameter(parameterIndex);
	        }
	        catch (Exception e)
	        {
	            throw new OperationException("setParameters(Object... parameters) error", e);
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
     * close method is invoked on all {@link CascadeOperation} objects.
     * 
     * @throws OperationException if error
     */
    public void close() throws OperationException
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

        // close cascades
        if (cascadeOperations != null)
        {
            for (CascadeOperation<R, ?> o: cascadeOperations)
            {
                o.close();
            }
            
            cascadeOperations = null;
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
        setNextParameter(1);
        String sql = getSql();
        
        try
        {
            if (log.isDebugEnabled()) log.debug("prepare " + sql);
            preparedStatement = connection.prepareStatement(sql);
        }
        catch (Exception e)
        {
            throw new OperationException("prepare() error", e);
        }
        
        prepareCascades();
    }
    
    
    /**
     * Prepares cascades for all {@linkplain Cascade} annotations on row class.
     *  
     * @throws OperationException if error
     */
    protected void prepareCascades() throws OperationException
    {
        // for all fields
        for (Field f: getTable().getRowTranslator().getRowClass().getDeclaredFields())
        {
            if (f.isAnnotationPresent(Cascade.class))
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
    }


    /**
     * Creates and prepares cascade operations based upon annotation for a field.
     * 
     * @param field annotation is for this field of row class R
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
     * @param cascadesAnnotation gets table for {@linkplain Cascade#targetClass()}
     * @param targetField target of cascade (obtain target field type if not specified by annotation)
     * @return table for target class of annotation
     * @throws OperationException if error
     */
    protected Table<?> getTargetTable(Cascade cascadesAnnotation, Field targetField) throws OperationException
    {
        Table<?> targetTable;
        Class<?> targetClass = null;
        
        try
        {
            targetClass = cascadesAnnotation.targetClass();
            
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
            throw new OperationException("error constructing SormulaField", e);
        }
        
        return targetField;
    }
    

    /**
     * Gets full sql statement to prepare. Default implementation is to include base + where.
     * Subclasses may override to create more detailed sql.
     * 
     * @return sql to use in {@link #prepare()}
     */
    protected String getSql()
    {
        String sql = getBaseSql();
        
        if (getWhereTranslator() != null)
        {
            sql += " " + getWhereTranslator().createSql();
        }
        
        return sql;
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
     * Gets the jdbc connection used in this operation.
     *  
     * @return {@link Connection}
     */
    protected Connection getConnection()
    {
        return connection;
    }


    /**
     * Sets where condition from annoation name as defined in {@linkplain Where#name()} for row.
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
                setWhereTranslator(new WhereTranslator<R>(table.getRowTranslator(), whereConditionName));
            }
            catch (TranslatorException e)
            {
                throw new OperationException("can't create WhereTranslator for " + whereConditionName, e);
            }
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
     * Sets all column parameters from a row using the table's {@link RowTranslator}.
     * 
     * @param row get column values from this row
     * @throws OperationException if error
     */
    protected void prepareColumns(R row) throws OperationException
    {
        try
        {
            setNextParameter(getTable().getRowTranslator().write(preparedStatement, getNextParameter(), row));
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
    protected void prepareWhere(R row) throws OperationException
    {
    	if (log.isDebugEnabled()) log.debug("prepare parameters from row");
    	
        if (getWhereTranslator() != null)
        {
            try
            {
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
     * Gets the next jdbc parameter number used by {@link PreparedStatement} to set parameters.
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
