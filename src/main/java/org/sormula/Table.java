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
package org.sormula;

import java.util.Collection;
import java.util.List;

import org.sormula.annotation.Column;
import org.sormula.annotation.OrderBy;
import org.sormula.annotation.Row;
import org.sormula.annotation.Where;
import org.sormula.log.ClassLogger;
import org.sormula.operation.ArrayListSelectOperation;
import org.sormula.operation.DeleteOperation;
import org.sormula.operation.FullDelete;
import org.sormula.operation.FullInsert;
import org.sormula.operation.FullListSelect;
import org.sormula.operation.FullScalarSelect;
import org.sormula.operation.FullUpdate;
import org.sormula.operation.InsertOperation;
import org.sormula.operation.ListSelectOperation;
import org.sormula.operation.ScalarSelectOperation;
import org.sormula.operation.UpdateOperation;
import org.sormula.operation.aggregate.SelectAggregateOperation;
import org.sormula.operation.aggregate.SelectCountOperation;
import org.sormula.translator.NameTranslator;
import org.sormula.translator.NoNameTranslator;
import org.sormula.translator.RowTranslator;


/**
 * A table within a sql database. Contains a {@linkplain RowTranslator} for reading/writing
 * data form/to database and contains methods for common input/output operations. The RowTranslator
 * is created based upon fields within the row class of type R and from annoations for the
 * class. All common input/output methods use the primary key defined by {@linkplain Column#primaryKey()} 
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
public class Table<R>
{
    private static final ClassLogger log = new ClassLogger();
    
    Database database;
    String tableName;
    RowTranslator<R> rowTranslator;
    NameTranslator nameTranslator;
    
    
    /**
     * Constructs for a database and the class that used for row objects. Creates
     * a {@linkplain RowTranslator} for mapping row objects to/from database.
     * 
     * @param database database for this table
     * @param rowClass row objects are of this type
     * @throws SormulaException if error
     */
    public Table(Database database, Class<R> rowClass) throws SormulaException
    {
        this.database = database;

        // process row annoation
        Row rowAnnotation = rowClass.getAnnotation(Row.class);
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

        rowTranslator = new RowTranslator<R>(rowClass, nameTranslator);
        
        if (log.isDebugEnabled())
        {
            log.debug("nameTranslator=" + nameTranslator.getClass().getCanonicalName());
            log.debug("table name = " + tableName);
            log.debug("number of columns=" + rowTranslator.getColumnTranslatorList().size());
        }
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
     * Gets the table name used in sql statements.
     * 
     * @return table name (without schema prefix)
     */
    public String getTableName()
    {
        return tableName;
    }


    /**
     * Gets table name used in sql statements with optional schema prefix if necessary.
     *  
     * @return schema.tablename if schema is not empty string; otherwise tablename
     */
    public String getQualifiedTableName()
    {
        String schema = database.getSchema();
        if (schema.length() > 0) return schema + "." + tableName;
        else return tableName;
    }

    
    /**
     * Gets translator defined by {@linkplain Row#nameTranslator()}.
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
     * Selects all rows in table.
     * <p>
     * Example:
     * <blockquote><pre>
     * Database database = ...
     * Table&lt;Order&gt; table = database.getTable(Order.class);
     * List&ltOrder&gt; orders = table.selectAll();
     * </pre></blockquote>
     * @return list of all rows
     * @throws SormulaException if error
     */
    public List<R> selectAll() throws SormulaException
    {
        return new FullListSelect<R>(createSelectAllOperation()).executeAll();
    }
    
    
    /**
     * Selects one row in table using primary key. Primary key must be defined by one
     * or more {@linkplain Column#primaryKey()} annotations.
     * <p>
     * Example:
     * <blockquote><pre>
     * Database database = ...
     * Table&lt;Order&gt; table = database.getTable(Order.class);
     * int orderNumber = 123456; // primary key
     * Order order = table.select(orderNumber);
     * </pre></blockquote>
     * @param parameters primary key values to use for select (must be in same order as
     * primary key columns appear with row class)
     * @return row or null if none found
     * @throws SormulaException if error
     */
    public R select(Object... parameters) throws SormulaException
    {
        return new FullScalarSelect<R>(this).execute(parameters);
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
     * @return list of rows selected
     * @throws SormulaException if error
     */
    public List<R> selectAllCustom(String customSql, Object... parameters) throws SormulaException
    {
    	ArrayListSelectOperation<R> operation = new ArrayListSelectOperation<R>(this);
    	operation.setCustomSql(customSql);
    	return new FullListSelect<R>(operation).executeAll(parameters);
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
    	ScalarSelectOperation<R> operation = new ScalarSelectOperation<R>(this);
    	operation.setCustomSql(customSql);
    	return new FullScalarSelect<R>(operation).execute(parameters);
    }
    
    
    /**
     * Creates operation to select all rows in table. This method is an alternate to:<br>
     * <blockquote><pre>
     * new ArrayListSelectOperation&lt;R&gt;(table)
     * </pre></blockquote>
     * <p>
     * Example:
     * <blockquote><pre>
     * Database database = ...
     * Table&lt;Student&gt; table = database.getTable(Student.class);
     * ListSelectOperation&ltStudent&gt; operation = table.createSelectAllOperation();
     * 
     * //read as a collection
     * operation.execute();
     * for (Student s: operation.readAll())
     *     System.out.println(s);
     *   
     * // read one row at a time
     * operation.execute();
     * for (Student s = operation.readNext(); s != null; s = operation.readNext())
     *       System.out.println(s);
     * 
     * operation.close();
     * </pre></blockquote>
     * @return select operation
     * @throws SormulaException if error
     */
    public ListSelectOperation<R> createSelectAllOperation() throws SormulaException
    {
        return createSelectOperation("");
    }
    
    
    /**
     * Creates operation to select by primary key. Primary key must be defined by one
     * or more {@linkplain Column#primaryKey()} annotations. This method is an alternate to:<br>
     * <blockquote><pre>
     * ScalarSelectOperation<R> selectOperation = new ScalarSelectOperation&lt;R&gt;(table);
     * selectOperation.setWhere("primaryKey");
     * </pre></blockquote>
     * <p>
     * Example:
     * <blockquote><pre>
     * Database database = ...
     * Table&lt;Order&gt; table = database.getTable(Order.class);
     * ScalarSelectOperation&lt;Order&gt; operation = table.createSelectOperation();
     * int orderNumber = 123456; // primary key
     * operation.setParameters(orderNumber);
     * operation.execute();
     * Order order = operation.readNext();
     * operation.close();
     * </pre></blockquote>
     * @return select operation
     * @throws SormulaException if error
     */
    public ScalarSelectOperation<R> createSelectOperation() throws SormulaException
    {
        ScalarSelectOperation<R> selectOperation = new ScalarSelectOperation<R>(this);
        selectOperation.setWhere("primaryKey");
        return selectOperation;
    }
    
    
    /**
     * Creates operation for select by a named where condition. This method is an alternate to:<br>
     * <blockquote><pre>
     * ArrayListSelectOperation<R> selectOperation = new ArrayListSelectOperation&lt;R&gt;(table);
     * selectOperation.setWhere(whereConditionName);
     * selectOperation.setOrderBy(orderByName);
     * </pre></blockquote>
     * <p>
     * Example:
     * <blockquote><pre>
     * Database database = ...
     * Table&lt;Student&gt; table = database.getTable(Student.class);
     * 
     * // graduated is name of Where annotation on Student to select by graduation date
     * // byName is name of OrderBy annotation  on Student to order by last name, first name
     * ListSelectOperation&ltStudent&gt; operation = table.createSelectOperation("graduated", "byName");
     * 
     * operation.execute();
     * for (Student s: operation.readAll())
     *     System.out.println(s);
     *   
     * operation.close();
     * </pre></blockquote>
     * 
     * @param whereConditionName name of where condition to use or use empty string to select all rows in table
     * @param orderByName name of order phrase; see {@link ScalarSelectOperation#setOrderBy(String)}
     * @return select operation
     * @throws SormulaException if error
     * @see Where
     * @see OrderBy
     */
    public ListSelectOperation<R> createSelectOperation(String whereConditionName, String orderByName) throws SormulaException
    {
        ListSelectOperation<R> selectOperation = new ArrayListSelectOperation<R>(this);
        selectOperation.setWhere(whereConditionName);
        selectOperation.setOrderBy(orderByName);
        return selectOperation;
    }
    

    /**
     * Creates operation for select by a named where condition. This method is an alternate to:<br>
     * <blockquote><pre>
     * ArrayListSelectOperation<R> selectOperation = new ArrayListSelectOperation&lt;R&gt;(table);
     * selectOperation.setWhere(whereConditionName);
     * </pre></blockquote>
     * <p>
     * Example: See {@link #createSelectOperation(String, String)} omit order by.
     * @param whereConditionName name of where condition to use or use empty string to select all rows in table
     * @return select operation
     * @throws SormulaException if error
     * @see Where
     */
    // TODO replace createSelectOperation by adding whereConditionName to ArrayListSelectOperation constructor
    // TODO likewise for other create methods thus reducing methods and size of Table class
    // TODO make version 1.1 since it is major change and not backward compatible
    // TODO move SelectCountOperation to aggregate package also?
    public ListSelectOperation<R> createSelectOperation(String whereConditionName) throws SormulaException
    {
        ListSelectOperation<R> selectOperation = new ArrayListSelectOperation<R>(this);
        selectOperation.setWhere(whereConditionName);
        return selectOperation;
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
     * @param whereConditionName name of where condition to use empty string to count all rows in table
     * @param parameters parameters for where condition
     * @return count of all rows in table
     * @throws SormulaException if error
     */
    public int selectCount(String whereConditionName, Object...parameters) throws SormulaException
    {
        SelectCountOperation<R> selectCountOperation = createSelectCountOperation(whereConditionName);
        selectCountOperation.setParameters(parameters);
        selectCountOperation.execute();
        int count = selectCountOperation.readCount();
        selectCountOperation.close();
        return count;
    }
    
    
    /**
     * Creates operation to select count of rows for a named where condition. This method is from older 
     * versions and is kept for backward compatiblity. It is equivalent to 
     * <Integer>createSelectCountOperation("*", whereConditionName). 
     * <p>
     * This method is an alternate to:<br>
     * <blockquote><pre>
     * SelectCountOperation<R> selectOperation = new SelectCountOperation&lt;R&gt;(table);
     * selectOperation.setWhere(whereConditionName);
     * </pre></blockquote>
     * <p>
     * Example:
     * <blockquote><pre>
     * Database database = ...
     * Table&lt;Order&gt; table = database.getTable(Order.class);
     *  
     * // quantityExceeds is the name of a Where annotation on Order that filters quantity >= ?
     * SelectCountOperation<R> selectCountOperation = table.createSelectCountOperation("quanityExceeds");
     * selectCountOperation.setParameters(100);
     * selectCountOperation.execute();
     * int bigOrderCount = selectCountOperation.readCount();
     * selectCountOperation.close();
     * </pre></blockquote> 
     * @param whereConditionName name of where condition to use empty string to count all rows in table
     * @return select operation
     * @throws SormulaException if error
     * @see Where
     */
    public SelectCountOperation<R> createSelectCountOperation(String whereConditionName) throws SormulaException
    {
        SelectCountOperation<R> selectCountOperation = new SelectCountOperation<R>(this);
        selectCountOperation.setWhere(whereConditionName);
        return selectCountOperation;
    }
    

    public <T> T selectCount(String expression) throws SormulaException
    {
        return this.<T>selectCount(expression, "");
    }
    public <T> T selectCount(String expression, String whereConditionName, Object...parameters) throws SormulaException
    {
        SelectAggregateOperation<R, T> selectOperation = this.<T>createSelectCountOperation(expression, whereConditionName);
        selectOperation.setParameters(parameters);
        selectOperation.execute();
        T result = selectOperation.readAggregate();
        selectOperation.close();
        return result;
    }
    public <T> SelectAggregateOperation<R, T> createSelectCountOperation(String expression, String whereConditionName) throws SormulaException
    {
        return this.<T>createSelectAggregateOperation("COUNT", expression, whereConditionName);
    }
    
    
    // TODO javadoc
    public <T> T selectMin(String expression) throws SormulaException
    {
        return this.<T>selectMin(expression, "");
    }
    public <T> T selectMin(String expression, String whereConditionName, Object...parameters) throws SormulaException
    {
        SelectAggregateOperation<R, T> selectOperation = this.<T>createSelectMinOperation(expression, whereConditionName);
        selectOperation.setParameters(parameters);
        selectOperation.execute();
        T result = selectOperation.readAggregate();
        selectOperation.close();
        return result;
    }
    public <T> SelectAggregateOperation<R, T> createSelectMinOperation(String expression, String whereConditionName) throws SormulaException
    {
        return this.<T>createSelectAggregateOperation("MIN", expression, whereConditionName);
    }
    
    
    public <T> T selectMax(String expression) throws SormulaException
    {
        return this.<T>selectMax(expression, "");
    }
    public <T> T selectMax(String expression, String whereConditionName, Object...parameters) throws SormulaException
    {
        SelectAggregateOperation<R, T> selectOperation = this.<T>createSelectMaxOperation(expression, whereConditionName);
        selectOperation.setParameters(parameters);
        selectOperation.execute();
        T result = selectOperation.readAggregate();
        selectOperation.close();
        return result;
    }
    public <T> SelectAggregateOperation<R, T> createSelectMaxOperation(String expression, String whereConditionName) throws SormulaException
    {
        return this.<T>createSelectAggregateOperation("MAX", expression, whereConditionName);
    }
    
    
    public <T> T selectAvg(String expression) throws SormulaException
    {
        return this.<T>selectAvg(expression, "");
    }
    public <T> T selectAvg(String expression, String whereConditionName, Object...parameters) throws SormulaException
    {
        SelectAggregateOperation<R, T> selectOperation = this.<T>createSelectAvgOperation(expression, whereConditionName);
        selectOperation.setParameters(parameters);
        selectOperation.execute();
        T result = selectOperation.readAggregate();
        selectOperation.close();
        return result;
    }
    public <T> SelectAggregateOperation<R, T> createSelectAvgOperation(String expression, String whereConditionName) throws SormulaException
    {
        return this.<T>createSelectAggregateOperation("AVG", expression, whereConditionName);
    }
    
    
    /**
     * Creates an instance of {@link SelectAggregateOperation}. Use this as alternative to using
     * new operator to construct and then set where condition.
     * 
     * @param <T> result type of aggregate expression
     * @param function aggregate SQL function like MIN, MAX, SUM, etc.
     * @param expression expression to use as parameter to function; typically it is the
     * name of a column to that aggregate function operates upon (example: SUM(amount) SUM is function and
     * amount is expression)  
     * @param whereConditionName name of where condition annotation on R that limits the rows used in aggregate
     * @return aggregate operation
     * @throws SormulaException if error
     */
    public <T> SelectAggregateOperation<R, T> createSelectAggregateOperation(String function, String expression, String whereConditionName) throws SormulaException
    {
        SelectAggregateOperation<R, T> selectOperation = 
            new SelectAggregateOperation<R, T>(this, function, expression);
        selectOperation.setWhere(whereConditionName);
        return selectOperation;
    }

    
    /**
     * Inserts one row into table.
     * <p>
     * Example:
     * <blockquote><pre>
     * Database database = ...
     * Table&lt;Student&gt; table = database.getTable(Student.class);
     * Student student = new Student();
     * student.setId(1234);
     * student.setFirstName("Jeff");
     * student.setLastName("Miller");
     * student.setGraduationDate(new Date(System.currentTimeMillis()));
     * table.insert(student);
     * </pre></blockquote>
     * @param row row to insert
     * @return count of rows affected
     * @throws SormulaException if error
     */
    public int insert(R row) throws SormulaException
    {
        return new FullInsert<R>(this).execute(row);
    }
    
    
    /**
     * Inserts collection of rows.
     * <p>
     * Example:
     * <blockquote><pre>
     * Database database = ...
     * Table&lt;Student&gt; table = database.getTable(Student.class);
     * ArrayList&lt;Student&gt; list = new ArrayList&lt;Student&gt;();
     * list.add(student1);
     * list.add(student2);
     * list.add(student3);
     * table.insertAll(list);
     * </pre></blockquote>
     * @param rows rows to insert
     * @return count of rows affected
     * @throws SormulaException if error
     */
    public int insertAll(Collection<R> rows) throws SormulaException
    {
        return new FullInsert<R>(this).executeAll(rows);
    }
    
    
    /**
     * Creates insert operation.
     * <p>
     * Example:
     * <blockquote><pre>
     * Database database = ...
     * Table&lt;Student&gt; table = database.getTable(Student.class);
     * InsertOperation&lt;Student&gt; insertOperation = table.createInsertOperation();
     * 
     * while (some condition)
     * {
     *     Student student = new Student();
     *     student.setId(...);
     *     student.setFirstName(...);
     *     student.setLastName(...);
     *     student.setGraduationDate(...);
     *     insertOperation.setRow(student);
     *     insertOperation.execute();
     * }
     * 
     * insertOperation.close();
     * </pre></blockquote>
     * @return insert operation
     * @throws SormulaException if error
     */
    public InsertOperation<R> createInsertOperation() throws SormulaException
    {
        return new InsertOperation<R>(this);
    }
    
    
    /**
     * Updates one row in table by primary key. Primary key must be defined by one
     * or more {@linkplain Column#primaryKey()} annotations.
     * <p>
     * Example:
     * <blockquote><pre>
     * Database database = ...
     * Table&lt;Student&gt; table = database.getTable(Student.class);
     * Student student = table.select(id);
     * student.setGraduationDate(...);
     * table.update(student);
     * </pre></blockquote>
     * @param row row to update
     * @return count of rows affected
     * @throws SormulaException if error
     */
    public int update(R row) throws SormulaException
    {
        return new FullUpdate<R>(this).execute(row);
    }
    
    
    /**
     * Updates collection of rows using primary key. Primary key must be defined by one
     * or more {@linkplain Column#primaryKey()} annotations.
     * <p>
     * Example:
     * <blockquote><pre>
     * Database database = ...
     * Table&lt;Student&gt; table = database.getTable(Student.class);
     * List&lt;Student&gt; list = table.selectAll();
     * for (Student s: list)
     *     student.setGraduationDate(...);
     *   
     * table.updateAll(list);
     * </pre></blockquote>
     * @param rows rows to update
     * @return count of rows affected
     * @throws SormulaException if error
     */
    public int updateAll(Collection<R> rows) throws SormulaException
    {
        return new FullUpdate<R>(this).executeAll(rows);
    }
    
    
    /**
     * Creates update by primary key operation. Primary key must be defined by one
     * or more {@linkplain Column#primaryKey()} annotations.
     * <p>
     * Example:
     * <blockquote><pre>
     * Database database = ...
     * Table&lt;Student&gt; table = database.getTable(Student.class);
     * UpdateOperation&lt;Student&gt; updateOperation = table.createUpdateOperation();
     * 
     * while (some condition)
     * {
     *     Student student = getSomeStudent();
     *     student.setGraduationDate(...);
     *     updateOperation.setRow(student);
     *     updateOperation.execute();
     * }
     * 
     * updateOperation.close();
     * </pre></blockquote>
     * @return update operation
     * @throws SormulaException if error
     */
    public UpdateOperation<R> createUpdateOperation() throws SormulaException
    {
        return createUpdateOperation("primaryKey");
    }
    
    
    /**
     * Creates operation to update by named where condition.
     * <p>
     * Example:
     * <blockquote><pre>
     * Database database = ...
     * Table&lt;Student&gt; table = database.getTable(Student.class);
     * // specialCondition is name of Where annotation on Student class
     * UpdateOperation&lt;Student&gt; updateOperation = table.createUpdateOperation("specialCondition");
     * Student student = new Student();
     * student.setId(...);
     * student.setFirstName(...);
     * student.setLastName(...);
     * student.setGraduationDate(...);
     * updateOperation.setRow(student);
     * updateOperation.execute(); // updates all rows with values from student for specialCondition
     * updateOperation.close();
     * </pre></blockquote>
     * @param whereConditionName name of where condition to use
     * @return update operation
     * @throws SormulaException if error
     */
    public UpdateOperation<R> createUpdateOperation(String whereConditionName) throws SormulaException
    {
        UpdateOperation<R> updateOperation = new UpdateOperation<R>(this);
        updateOperation.setWhere(whereConditionName);
        return updateOperation;
    }
    
    
    /**
     * Deletes by primary key. Primary key must be defined by one
     * or more {@linkplain Column#primaryKey()} annotations.
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
        return new FullDelete<R>(this).executeObject(parameters);
    }
    
    
    /**
     * Deletes by primary key. Primary key must be defined by one
     * or more {@linkplain Column#primaryKey()} annotations.
     * <p>
     * Example:
     * <blockquote><pre>
     * Database database = ...
     * Table&lt;Student&gt; table = database.getTable(Student.class);
     * // delete student with id of 1234
     * Student student = table.select(1234);
     * table.delete(student);
     * </pre></blockquote> 
     * @param row get primary key values from this row
     * @return count of rows affected
     * @throws SormulaException if error
     */
    public int delete(R row) throws SormulaException
    {
        return new FullDelete<R>(this).execute(row);
    }
    
    
    /**
     * Deletes many rows by primary key. Primary key must be defined by one
     * or more {@linkplain Column#primaryKey()} annotations.
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
        return new FullDelete<R>(this).executeAll(rows);
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
        return new FullDelete<R>(createDeleteAllOperation()).executeObject();
    }

    
    /**
     * Creates operation to delete all rows in table (no where condition).
     * <p>
     * Example:
     * <blockquote><pre>
     * Database database = ...
     * Table&lt;Student&gt; table = database.getTable(Student.class);
     * DeleteOperation&lt;Student&gt; deleteAllOperation = table.createDeleteAllOperation();
     * deleteAllOperation.execute();
     * deleteAllOperation.close();
     * </pre></blockquote> 
     * @return delete operation
     * @throws SormulaException if error
     */
    public DeleteOperation<R> createDeleteAllOperation() throws SormulaException
    {
        return createDeleteOperation("");
    }
    
    
    /**
     * Creates operation to delete by primary key.
     * <p>
     * Example:
     * <blockquote><pre>
     * Database database = ...
     * Table&lt;Student&gt; table = database.getTable(Student.class);
     * DeleteOperation&lt;Student&gt; deleteOperation = table.createDeleteOperation();
     * 
     * while (some condition)
     * {
     *     Student student = getSomeStudent();
     *     deleteOperation.setRow(student);
     *     deleteOperation.execute();
     * }
     * 
     * deleteOperation.close();
     * </pre></blockquote>
     * @return delete operation
     * @throws SormulaException if error
     */
    public DeleteOperation<R> createDeleteOperation() throws SormulaException
    {
        return createDeleteOperation("primaryKey");
    }
    
    
    /**
     * Creates delete operation to delete by named where condition.
     * <p>
     * Example:
     * <blockquote><pre>
     * Database database = ...
     * Table&lt;Student&gt; table = database.getTable(Student.class);
     * // specialCondition is name of Where annotation on Student class
     * DeleteOperation&lt;Student&gt; deleteOperation = table.createDeleteOperation("specialCondition");
     * Student student = new Student();
     * student.setId(...);
     * student.setFirstName(...);
     * student.setLastName(...);
     * student.setGraduationDate(...);
     * deleteOperation.setRow(student);
     * deleteOperation.execute(); // deletes rows with values from student for specialCondition
     * deleteOperation.close();
     * </pre></blockquote>
     * @param whereConditionName name of where condition to use
     * @return delete operation
     * @throws SormulaException if error
     */
    public DeleteOperation<R> createDeleteOperation(String whereConditionName) throws SormulaException
    {
        DeleteOperation<R> deleteOperation = new DeleteOperation<R>(this);
        deleteOperation.setWhere(whereConditionName);
        return deleteOperation;
    }
    
    
    // TODO implement as SaveOperation?
    public int save(R row) throws SormulaException
    {
        return 0;
    }
    public int saveAll(Collection<R> rows) throws SormulaException
    {
        return 0;
    }
}
