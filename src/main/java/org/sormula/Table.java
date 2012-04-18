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
package org.sormula;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sormula.annotation.Column;
import org.sormula.annotation.ExplicitTypeAnnotationReader;
import org.sormula.annotation.OrderBy;
import org.sormula.annotation.Row;
import org.sormula.log.ClassLogger;
import org.sormula.operation.ArrayListSelectOperation;
import org.sormula.operation.DeleteOperation;
import org.sormula.operation.InsertOperation;
import org.sormula.operation.SaveOperation;
import org.sormula.operation.ScalarSelectOperation;
import org.sormula.operation.SelectCountOperation;
import org.sormula.operation.UpdateOperation;
import org.sormula.operation.aggregate.SelectAggregateOperation;
import org.sormula.operation.aggregate.SelectAvgOperation;
import org.sormula.operation.aggregate.SelectMaxOperation;
import org.sormula.operation.aggregate.SelectMinOperation;
import org.sormula.operation.aggregate.SelectSumOperation;
import org.sormula.translator.NameTranslator;
import org.sormula.translator.NoNameTranslator;
import org.sormula.translator.RowTranslator;
import org.sormula.translator.TranslatorException;
import org.sormula.translator.TypeTranslator;
import org.sormula.translator.TypeTranslatorMap;


/**
 * A table within a sql database. Contains a {@link RowTranslator} for reading/writing
 * data form/to database and contains methods for common input/output operations. The RowTranslator
 * is created based upon fields within the row class of type R and from annotations for the
 * class. All common input/output methods use the primary key defined by {@link Column#primaryKey()} 
 * annotation on class R.
 * <p>
 * Example 1 - Get table from database:
 * <blockquote><pre>
 * Connection connection = ... // jdbc connection
 * Database database = new Database(connection);
 * Table&lt;MyRow&gt; table = database.getTable(MyRow.class);
 * table.selectAll();
 * </pre></blockquote>
 * <p>
 * Example 2 - Instantiate table:
 * <blockquote><pre>
 * Connection connection = ... // jdbc connection
 * Database database = new Database(connection);
 * Table&lt;MyRow&gt; table = new Table&lt;MyRow&gt;(database, MyRow.class);
 * table.selectAll();
 * </pre></blockquote>
 * <p>
 * Example 3 - Instantiate a table subclass:
 * <blockquote><pre>
 * public class MyCustomTable extends Table&lt;MyRow&gt;
 * {
 *     public MyCustomTable(Connection connection)
 *     {
 *         super(connection, MyRow.class);
 *     }
 * 
 *     ...
 * }
 * 
 * Connection connection = ... // jdbc connection
 * Database database = new Database(connection);
 * MyCustomTable table = new MyCustomTable(database);
 * table.selectAll();
 * </pre></blockquote>
 * 
 * @since 1.0
 * @author Jeff Miller
 * @param <R> type of row objects
 */
public class Table<R> implements TypeTranslatorMap
{
    private static final ClassLogger log = new ClassLogger();
    
    Database database;
    Class<R> rowClass;
    String tableName;
    RowTranslator<R> rowTranslator;
    NameTranslator nameTranslator;
    Map<String, TypeTranslator<?>> typeTranslatorMap; // key is row class canonical name
    
    
    /**
     * Constructs for a database and the class that used for row objects. Creates
     * a {@link RowTranslator} for mapping row objects to/from database.
     * 
     * @param database database for this table
     * @param rowClass row objects are of this type
     * @throws SormulaException if error
     */
    public Table(Database database, Class<R> rowClass) throws SormulaException
    {
        this.database = database;
        this.rowClass = rowClass;
        typeTranslatorMap = new HashMap<String, TypeTranslator<?>>();
        
        // process any type annotations
        try
        {
            new ExplicitTypeAnnotationReader(this, this.getClass(), rowClass).install();
        }
        catch (Exception e)
        {
            throw new SormulaException("error getting ExplicitType from table " + 
                    getClass().getCanonicalName(), e);
        }
        
        // process row annotation
        Row rowAnnotation = getClass().getAnnotation(Row.class); // look in table subclass first
        if (rowAnnotation == null) rowAnnotation = rowClass.getAnnotation(Row.class); // look in row class if none for table subclass
        
        Class<? extends NameTranslator> nameTranslatorClass = null;
        
        if (rowAnnotation != null)
        {
            // row annotation was available
            nameTranslatorClass = rowAnnotation.nameTranslator();
            tableName = rowAnnotation.tableName();
        }
        else
        {
        	// no row annotation
        	nameTranslatorClass = NoNameTranslator.class;
        }
        
        // default name translator check
        if (nameTranslatorClass.getName().equals(NoNameTranslator.class.getName()))
        {
        	// name translator is NoNameTranslator which does nothing
        	Class<? extends NameTranslator> defaultNameTranslatorClass = database.getNameTranslatorClass();
        	
        	if (defaultNameTranslatorClass != null)
        	{
        		// default is available, use it
        		nameTranslatorClass = defaultNameTranslatorClass;
        	}
        }
        
        // instantiate name translator
        try
        {
            nameTranslator = nameTranslatorClass.newInstance();
        }
        catch (Exception e)
        {
            log.error("error creating name translator", e);
            nameTranslator = new NoNameTranslator();
        }
        
        if (tableName == null || tableName.length() == 0) 
        {
            // no table name is provided, get table name from class name
            tableName = nameTranslator.translate(rowClass.getSimpleName(), rowClass);
        }

        rowTranslator = initRowTranslator();
        
        if (log.isDebugEnabled())
        {
            log.debug("nameTranslator=" + nameTranslator.getClass().getCanonicalName());
            log.debug("table name = " + tableName);
            log.debug("number of columns=" + rowTranslator.getColumnTranslatorList().size());
        }
    }

    
    protected RowTranslator<R> initRowTranslator() throws TranslatorException
    {
        // default
        return new RowTranslator<R>(this);
    }
    

    /**
     * Gets the database supplied in constructor.
     * 
     * @return database for this table
     */
    public Database getDatabase()
    {
        return database;
    }


    /**
     * @return row class supplied in constructor
     * @since 1.6
     */
    public Class<R> getRowClass()
    {
        return rowClass;
    }


    /**
     * Gets the table name used in sql statements.
     * 
     * @return table name (without schema prefix)
     */
    public String getTableName()
    {
        return tableName;
    }


    /**
     * Sets the table name to use in sql statements. The default is
     * set in the constructor and based upon the row class name and 
     * {@link NameTranslator} if specified. Use this to override the default.
     * 
     * @param tableName table name (without schema prefix)
     */
    public void setTableName(String tableName)
    {
        this.tableName = tableName;
    }


    /**
     * Gets table name used in sql statements with optional schema prefix if necessary.
     *  
     * @return schema.tablename if schema is not empty string; otherwise tablename
     */
    public String getQualifiedTableName()
    {
        String schema = database.getSchema();
        if (schema.length() > 0) return schema + "." + getTableName();
        else return getTableName();
    }

    
    /**
     * Gets translator defined by {@link Row#nameTranslator()}.
     * 
     * @return translator for converting java names to sql table and column names
     */
    public NameTranslator getNameTranslator()
    {
        return nameTranslator;
    }
    

    /**
     * Gets the row translator for converting row values to/from sql parameters.
     * 
     * @return row translator for this table
     */
    public RowTranslator<R> getRowTranslator()
    {
        return rowTranslator;
    }
    
    
    /**
     * Creates new instance of row. Typically used by select operations for
     * each row that is read from result set.
     * 
     * @return new instance of row created with zero-arg constructor
     * @throws SormulaException if error
     * @since 1.7
     */
    public R newRow() throws SormulaException
    {
        R row;
        
        try
        {
            row = rowClass.newInstance();
        }
        catch (Exception e)
        {
            throw new SormulaException("error creating row instance for " + rowClass.getName() +
                    "; make sure row has public zero-arg constructor", e);
        }
        
        return row;
    }


    /**
     * Overrides translator defined in {@link Database} for all operations on this table. See 
     * {@link Database#putTypeTranslator(Class, TypeTranslator)} for an explanation
     * of translators.
     * 
     * @param typeClass class that translator operates upon
     * @param typeTranslator to use for typeClass
     * @since 1.6
     */
    public void putTypeTranslator(Class<?> typeClass, TypeTranslator<?> typeTranslator)
    {
        typeTranslatorMap.put(typeClass.getCanonicalName(), typeTranslator);
    }
    
    
    /**
     * Gets the translator to use to convert a value to a prepared statement or to 
     * convert from a result set. If none are set for this table, then
     * translator is obtained from {@link Database#getTypeTranslator(Class)}. See
     * {@link Database#getTypeTranslator(Class)} for more details.
     * 
     * @param typeClass class that translator operates upon
     * @return translator to use for typeClass
     * @since 1.6
     */
    public TypeTranslator<?> getTypeTranslator(Class<?> typeClass)
    {
        String typeClassName = typeClass.getCanonicalName();
        
        // get table-specific translator
        TypeTranslator<?> typeTranslator = (TypeTranslator<?>)typeTranslatorMap.get(typeClassName);
        
        if (typeTranslator == null)
        {
            // get default translator defined by database
            typeTranslator = getDatabase().getTypeTranslator(typeClassName);
        }
        
        return typeTranslator;
    }
    
    
    /**
     * Selects all rows in table.
     * <p>
     * Example:
     * <blockquote><pre>
     * Database database = ...
     * Table&lt;Order&gt; table = database.getTable(Order.class);
     * List&ltOrder&gt; orders = table.selectAll();
     * </pre></blockquote>
     * @return list of all rows; empty list if none found
     * @throws SormulaException if error
     */
    public List<R> selectAll() throws SormulaException
    {
        return new ArrayListSelectOperation<R>(this, "").selectAll();
    }
    
    
    /**
     * Selects one row in table using primary key. Primary key must be defined by one
     * or more {@link Column#primaryKey()} annotations.
     * <p>
     * Example:
     * <blockquote><pre>
     * Database database = ...
     * Table&lt;Order&gt; table = database.getTable(Order.class);
     * int orderNumber = 123456; // primary key
     * Order order = table.select(orderNumber);
     * </pre></blockquote>
     * @param primaryKeys primary key values to use for select (must be in same order as
     * primary key columns appear with row class)
     * @return row or null if none found
     * @throws SormulaException if error
     */
    public R select(Object... primaryKeys) throws SormulaException
    {
        return new ScalarSelectOperation<R>(this).select(primaryKeys);
    }
    
    
    /**
     * Selects one row for where condition and parameters.
     * 
     * @param whereConditionName name of where condition to use; empty string to select arbitrary row in table
     * @param parameters parameter values for where condition
     * @return row for where condition and parameters; null if none found
     * @throws SormulaException if error
     * @since 1.7
     */
    public R selectWhere(String whereConditionName, Object...parameters) throws SormulaException
    {
        return new ScalarSelectOperation<R>(this, whereConditionName).select(parameters);
    }
    
    
    /**
     * Selects list of rows for where condition and parameters.
     * 
     * @param whereConditionName name of where condition to use; empty string to select all rows in table
     * @param parameters parameter values for where condition
     * @return rows for where condition and parameters; empty list if none found
     * @throws SormulaException if error
     * @since 1.7
     */
    public List<R> selectAllWhere(String whereConditionName, Object...parameters) throws SormulaException
    {
        ArrayListSelectOperation<R> operation = new ArrayListSelectOperation<>(this, whereConditionName);
        return operation.selectAll(parameters);
    }
    
    
    /**
     * Selects list of rows for where condition and parameters.
     * 
     * @param whereConditionName name of where condition to use; empty string to select all rows in table
     * @param orderByName name of order phrase to use as defined in {@link OrderBy#name()}
     * @param parameters parameter values for where condition
     * @return rows for where condition and parameters; empty list if none found
     * @throws SormulaException if error
     * @since 1.7
     */
    public List<R> selectAllWhereOrdered(String whereConditionName, String orderByName, Object...parameters) throws SormulaException
    {
        ArrayListSelectOperation<R> operation = new ArrayListSelectOperation<>(this, whereConditionName);
        operation.setOrderBy(orderByName);
        return operation.selectAll(parameters);
    }
    
    
    /**
     * Select list of rows using custom sql.
     * <p>
     * Example:
     * <blockquote><pre>
     * Database database = ...
     * Table&lt;Order&gt; table = database.getTable(Order.class);
     * List&ltOrder&gt; orders = table.selectAllCustom("where orderdate >= '2011-01-01'");
     * </pre></blockquote>
     * @param customSql custom sql to be appended to base sql (for example, "where somecolumn=?")
     * @param parameters parameter value to be set in customSql
     * @return list of rows selected; empty list if none found
     * @throws SormulaException if error
     */
    public List<R> selectAllCustom(String customSql, Object... parameters) throws SormulaException
    {
    	ArrayListSelectOperation<R> operation = new ArrayListSelectOperation<>(this, "");
    	operation.setCustomSql(customSql);
    	return operation.selectAll(parameters);
    }
    
    
    /**
     * Selects one row using custom sql.
     * <p>
     * Example:
     * <blockquote><pre>
     * Database database = ...
     * Table&lt;Order&gt; table = database.getTable(Order.class);
     * Order order = table.selectCustom("where orderdate >= '2011-01-01'");
     * </pre></blockquote>
     * @param customSql custom sql to be appended to base sql (for example, "where somecolumn=?")
     * @param parameters parameter value to be set in customSql
     * @return first row found or null if no rows selected
     * @throws SormulaException if error
     */
    public R selectCustom(String customSql, Object... parameters) throws SormulaException
    {
    	ScalarSelectOperation<R> operation = new ScalarSelectOperation<>(this, "");
    	operation.setCustomSql(customSql);
    	return operation.select(parameters);
    }
    
    
    /**
     * Gets count of all rows in table. This method is from older versions and is kept for 
     * backward compatiblity. It is equivalent to <Integer>selectCount("*", "").
     * 
     * @return count of all rows in table
     * @throws SormulaException if error
     */
    public int selectCount() throws SormulaException
    {
        return selectCount("", new Object[0]); // new Object[0] parameter disambiguates the method to use
    }
    
    
    /**
     * Selects count for a subset of rows. This method is from older versions and is kept for 
     * backward compatiblity. It is equivalent to <Integer>selectCount("*", whereConditionName, parameters).
     * <p>
     * Example:
     * <blockquote><pre>
     * Database database = ...
     * Table&lt;Order&gt; table = database.getTable(Order.class);
     *  
     * // quantityExceeds is the name of a Where annotation on Order that filters quantity >= ?
     * int bigOrderCount = table.selectCount("quanityExceeds", 100);
     * </pre></blockquote>
     * @param whereConditionName name of where condition to use; empty string to count all rows in table
     * @param parameters parameter values for where condition
     * @return count of all rows in table
     * @throws SormulaException if error
     */
    public int selectCount(String whereConditionName, Object...parameters) throws SormulaException
    {
        org.sormula.operation.SelectCountOperation<R> selectCountOperation = new SelectCountOperation<>(this);
        selectCountOperation.setWhere(whereConditionName);
        selectCountOperation.setParameters(parameters);
        selectCountOperation.execute();
        int count = selectCountOperation.readCount();
        selectCountOperation.close();
        return count;
    }
    

    /**
     * Selects count of rows.
     * <p>
     * The data type returned from database is the same type as the expression. For example,
     * if expression is a column, then the returned type is the same type as column. If
     * expression is "*", then return types are database dependent.
     * 
     * @param <T> aggregate result type
     * @param expression expression to use as parameter to function; typically it is the name of a column
     * @return count of rows for expression 
     * @throws SormulaException if error
     */
    public <T> T selectCount(String expression) throws SormulaException
    {
        return this.<T>selectCount(expression, "");
    }
    
    
    /**
     * Selects count of rows.
     * <p>
     * The data type returned from database is the same type as the expression. For example,
     * if expression is a column, then the returned type is the same type as column. If
     * expression is "*", then return types are database dependent.
     * 
     * @param <T> aggregate result type
     * @param expression expression to use as parameter to function; typically it is the name of a column
     * @param whereConditionName name of where condition to use; empty string to count all rows in table
     * @param parameters parameter values for where condition
     * @return count of rows for expression and where condition
     * @throws SormulaException if error
     */
    public <T> T selectCount(String expression, String whereConditionName, Object...parameters) throws SormulaException
    {
        org.sormula.operation.aggregate.SelectCountOperation<R, T> selectOperation = 
            new org.sormula.operation.aggregate.SelectCountOperation<>(this, expression);
        selectOperation.setWhere(whereConditionName);
        selectOperation.setParameters(parameters);
        selectOperation.execute();
        T result = selectOperation.readAggregate();
        selectOperation.close();
        return result;
    }
    
    
    /**
     * Selects minimum value.
     * 
     * @param <T> aggregate result type
     * @param expression expression to use as parameter to function; typically it is the name of a column
     * @return minimum value for expression  
     * @throws SormulaException if error
     */
    public <T> T selectMin(String expression) throws SormulaException
    {
        return this.<T>selectMin(expression, "");
    }
    
    
    /**
     * Selects minimum value.
     * 
     * @param <T> aggregate result type
     * @param expression expression to use as parameter to function; typically it is the name of a column
     * @param whereConditionName name of where condition to use; empty string to count all rows in table
     * @param parameters parameter values for where condition
     * @return minimum value for expression and where condition 
     * @throws SormulaException if error
     */
    public <T> T selectMin(String expression, String whereConditionName, Object...parameters) throws SormulaException
    {
        SelectAggregateOperation<R, T> selectOperation = new SelectMinOperation<>(this, expression);
        selectOperation.setWhere(whereConditionName);
        selectOperation.setParameters(parameters);
        selectOperation.execute();
        T result = selectOperation.readAggregate();
        selectOperation.close();
        return result;
    }
    
    
    /**
     * Selects maximum value.
     * 
     * @param <T> aggregate result type
     * @param expression expression to use as parameter to function; typically it is the name of a column
     * @return maximum value for expression  
     * @throws SormulaException if error
     */
    public <T> T selectMax(String expression) throws SormulaException
    {
        return this.<T>selectMax(expression, "");
    }
    
    
    /**
     * Selects maximum value.
     * 
     * @param <T> aggregate result type
     * @param expression expression to use as parameter to function; typically it is the name of a column
     * @param whereConditionName name of where condition to use; empty string to count all rows in table
     * @param parameters parameter values for where condition
     * @return maximum value for expression and where condition 
     * @throws SormulaException if error
     */
    public <T> T selectMax(String expression, String whereConditionName, Object...parameters) throws SormulaException
    {
        SelectAggregateOperation<R, T> selectOperation = new SelectMaxOperation<>(this, expression);
        selectOperation.setWhere(whereConditionName);
        selectOperation.setParameters(parameters);
        selectOperation.execute();
        T result = selectOperation.readAggregate();
        selectOperation.close();
        return result;
    }
    
    
    /**
     * Selects average value.
     * 
     * @param <T> aggregate result type
     * @param expression expression to use as parameter to function; typically it is the name of a column
     * @return average value for expression  
     * @throws SormulaException if error
     */
    public <T> T selectAvg(String expression) throws SormulaException
    {
        return this.<T>selectAvg(expression, "");
    }
    
    
    /**
     * Selects average value.
     * 
     * @param <T> aggregate result type
     * @param expression expression to use as parameter to function; typically it is the name of a column
     * @param whereConditionName name of where condition to use; empty string to count all rows in table
     * @param parameters parameter values for where condition
     * @return average value for expression and where condition 
     * @throws SormulaException if error
     */
    public <T> T selectAvg(String expression, String whereConditionName, Object...parameters) throws SormulaException
    {
        SelectAggregateOperation<R, T> selectOperation = new SelectAvgOperation<>(this, expression);
        selectOperation.setWhere(whereConditionName);
        selectOperation.setParameters(parameters);
        selectOperation.execute();
        T result = selectOperation.readAggregate();
        selectOperation.close();
        return result;
    }
    
    
    /**
     * Selects sum.
     * 
     * @param <T> aggregate result type
     * @param expression expression to use as parameter to function; typically it is the name of a column
     * @return sum for expression  
     * @throws SormulaException if error
     * @since 1.7
     */
    public <T> T selectSum(String expression) throws SormulaException
    {
        return this.<T>selectSum(expression, "");
    }
    
    
    /**
     * Selects sum.
     * 
     * @param <T> aggregate result type
     * @param expression expression to use as parameter to function; typically it is the name of a column
     * @param whereConditionName name of where condition to use; empty string to count all rows in table
     * @param parameters parameter values for where condition
     * @return sum for expression and where condition 
     * @throws SormulaException if error
     * @since 1.7
     */
    public <T> T selectSum(String expression, String whereConditionName, Object...parameters) throws SormulaException
    {
        SelectAggregateOperation<R, T> selectOperation = new SelectSumOperation<>(this, expression);
        selectOperation.setWhere(whereConditionName);
        selectOperation.setParameters(parameters);
        selectOperation.execute();
        T result = selectOperation.readAggregate();
        selectOperation.close();
        return result;
    }
    
    
    /**
     * Inserts one row into table.
     * <p>
     * Example:
     * <blockquote><pre>
     * Database database = ...
     * Table&lt;Student&gt; table = database.getTable(Student.class);
     * Student s = new Student();
     * s.setId(1234);
     * s.setFirstName("Jeff");
     * s.setLastName("Miller");
     * s.setGraduationDate(new Date(System.currentTimeMillis()));
     * table.insert(s);
     * </pre></blockquote>
     * @param row row to insert
     * @return count of rows affected
     * @throws SormulaException if error
     */
    public int insert(R row) throws SormulaException
    {
        return new InsertOperation<R>(this).insert(row);
    }
    
    
    /**
     * Inserts collection of rows.
     * <p>
     * Example:
     * <blockquote><pre>
     * Database database = ...
     * Table&lt;Student&gt; table = database.getTable(Student.class);
     * ArrayList&lt;Student&gt; list = new ArrayList&lt;Student&gt;();
     * list.add(s1);
     * list.add(s2);
     * list.add(s3);
     * table.insertAll(list);
     * </pre></blockquote>
     * @param rows rows to insert
     * @return count of rows affected
     * @throws SormulaException if error
     */
    public int insertAll(Collection<R> rows) throws SormulaException
    {
        return new InsertOperation<R>(this).insertAll(rows);
    }
    
    
    /**
     * Updates one row in table by primary key. Primary key must be defined by one
     * or more {@link Column#primaryKey()} annotations.
     * <p>
     * Example:
     * <blockquote><pre>
     * Database database = ...
     * Table&lt;Student&gt; table = database.getTable(Student.class);
     * Student s = table.select(id);
     * s.setGraduationDate(...);
     * table.update(s);
     * </pre></blockquote>
     * @param row row to update
     * @return count of rows affected
     * @throws SormulaException if error
     */
    public int update(R row) throws SormulaException
    {
        return new UpdateOperation<R>(this).update(row);
    }
    
    
    /**
     * Updates collection of rows using primary key. Primary key must be defined by one
     * or more {@link Column#primaryKey()} annotations.
     * <p>
     * Example:
     * <blockquote><pre>
     * Database database = ...
     * Table&lt;Student&gt; table = database.getTable(Student.class);
     * List&lt;Student&gt; list = table.selectAll();
     * for (Student s: list)
     *     s.setGraduationDate(...);
     *   
     * table.updateAll(list);
     * </pre></blockquote>
     * @param rows rows to update
     * @return count of rows affected
     * @throws SormulaException if error
     */
    public int updateAll(Collection<R> rows) throws SormulaException
    {
        return new UpdateOperation<R>(this).updateAll(rows);
    }
    
    
    /**
     * Deletes by primary key. Primary key must be defined by one
     * or more {@link Column#primaryKey()} annotations.
     * <p>
     * Example:
     * <blockquote><pre>
     * Database database = ...
     * Table&lt;Student&gt; table = database.getTable(Student.class);
     * table.delete(1234); // deletes student with id of 1234
     * </pre></blockquote> 
     * @param parameters where condition values to use for delete (must be in same order as
     * primary key fields appear with row class)
     * @return count of rows affected
     * @throws SormulaException if error
     */
    public int delete(Object... parameters) throws SormulaException
    {
        return new DeleteOperation<R>(this).delete(parameters);
    }
    
    
    /**
     * Deletes by primary key. Primary key must be defined by one
     * or more {@link Column#primaryKey()} annotations.
     * <p>
     * Example:
     * <blockquote><pre>
     * Database database = ...
     * Table&lt;Student&gt; table = database.getTable(Student.class);
     * // delete student with id of 1234
     * Student s = table.select(1234);
     * table.delete(s);
     * </pre></blockquote> 
     * @param row get primary key values from this row
     * @return count of rows affected
     * @throws SormulaException if error
     */
    public int delete(R row) throws SormulaException
    {
        return new DeleteOperation<R>(this).delete(row);
    }
    
    
    /**
     * Deletes many rows by primary key. Primary key must be defined by one
     * or more {@link Column#primaryKey()} annotations.
     * <p>
     * Example:
     * <blockquote><pre>
     * Database database = ...
     * Table&lt;Student&gt; table = database.getTable(Student.class);
     * List&lt;Student&gt; list = getSomeStudents();
     * table.deleteAll(list);
     * </pre></blockquote> 
     * @param rows get primary key values from each row in this collection
     * @return count of rows affected
     * @throws SormulaException if error
     */
    public int deleteAll(Collection<R> rows) throws SormulaException
    {
        return new DeleteOperation<R>(this).deleteAll(rows);
    }
    
    
    /**
     * Deletes all rows in table.
     * <p>
     * Example:
     * <blockquote><pre>
     * Database database = ...
     * Table&lt;Student&gt; table = database.getTable(Student.class);
     * table.deleteAll();
     * </pre></blockquote> 
     * @return count of rows affected
     * @throws SormulaException if error
     */
    public int deleteAll() throws SormulaException
    {
        return new DeleteOperation<R>(this, "").delete();
    }
    

    /**
     * Uses {@link SaveOperation} to update an existing row or insert row if it
     * is not already in database.
     * 
     * @param row row to save
     * @return count of rows affected
     * @throws SormulaException if error
     */
    public int save(R row) throws SormulaException
    {
        return new SaveOperation<R>(this).modify(row);
    }
    
    
    /**
     * Uses {@link SaveOperation} to update an existing rows or insert rows if they
     * are not already in database.
     * 
     * @param rows collection of new and/or existing rows to save (may be mixture of new and existing)
     * @return count of rows affected
     * @throws SormulaException if error
     */
    public int saveAll(Collection<R> rows) throws SormulaException
    {
        return new SaveOperation<R>(this).modifyAll(rows);
    }
}
