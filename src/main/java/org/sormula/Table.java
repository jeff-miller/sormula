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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sormula.annotation.Column;
import org.sormula.annotation.ExplicitTypeAnnotationReader;
import org.sormula.annotation.OrderBy;
import org.sormula.annotation.Row;
import org.sormula.annotation.cache.Cached;
import org.sormula.annotation.cache.CachedAnnotationReader;
import org.sormula.annotation.cascade.SelectCascade;
import org.sormula.annotation.cascade.SelectCascadeAnnotationReader;
import org.sormula.cache.Cache;
import org.sormula.cache.CacheException;
import org.sormula.log.ClassLogger;
import org.sormula.operation.ArrayListSelectOperation;
import org.sormula.operation.DeleteOperation;
import org.sormula.operation.InsertOperation;
import org.sormula.operation.ModifyOperation;
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
public class Table<R> implements TypeTranslatorMap, TransactionListener
{
    private static final ClassLogger log = new ClassLogger();
    
    Database database;
    Class<R> rowClass;
    String tableName;
    RowTranslator<R> rowTranslator;
    @Deprecated
    NoNameTranslator noNameTranslator = new NoNameTranslator(); // remove when NoNameTranslator is removed
    List<? extends NameTranslator> nameTranslators;
    Map<String, TypeTranslator<?>> typeTranslatorMap; // key is row class canonical name
    List<Field> lazySelectCascadeFields;
    Cache<R> cache;
    boolean legacyAnnotationPrecedence;
    
    
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
        
        // LEGACY_ANNOTATION_PRECEDENCE=true means read table annotations prior to row annotations
        // by default version 3.0 and later will read row annotations prior to table annotations
        String lap = System.getenv("LEGACY_ANNOTATION_PRECEDENCE");
        if (lap != null && Boolean.valueOf(lap)) legacyAnnotationPrecedence = true;
        
        initTypeTranslatorMap();
        
        // process row annotation
        Row rowAnnotation;
        if (legacyAnnotationPrecedence)
        {
            // table then row
            rowAnnotation = getClass().getAnnotation(Row.class);
            if (rowAnnotation == null) rowAnnotation = rowClass.getAnnotation(Row.class);
        }
        else
        {
            // row then table
            rowAnnotation = rowClass.getAnnotation(Row.class); 
            if (rowAnnotation == null) rowAnnotation = getClass().getAnnotation(Row.class); 
        }
        
        nameTranslators = initNameTranslators(rowAnnotation);
        rowTranslator = initRowTranslator();
        tableName = initTableName(rowAnnotation);
        cache = initCache();
        
        database.getTransaction().addListener(this);
        
        if (log.isDebugEnabled())
        {
            StringBuilder sb = new StringBuilder(nameTranslators.size() * 30);
            for (NameTranslator nt: nameTranslators)
            {
                sb.append(nt.getClass().getCanonicalName());
                sb.append(" ");
            }
            
            log.debug("nameTranslators=" + sb);
            log.debug("table name = " + tableName);
            log.debug("number of columns=" + rowTranslator.getColumnTranslatorList().size());
            if (cache != null) log.debug("cache = " + cache);
        }
    }
    
    
    /**
     * Indicates if table is cached.
     * 
     * @return true if table has a cache; false if not
     * @since 3.0
     */
    public boolean isCached()
    {
    	return cache != null; 
    }
    
    
    /**
     * Gets the cache for this table.
     * 
     * @return cache for this table or null if table is not cached
     * @since 3.0
     */
	public Cache<R> getCache()
    {
        return cache;
    }

	
	/**
	 * Writes uncommitted cache changes to database and then evicts all rows from cache.
	 * 
	 * @throws SormulaException if error
	 * @since 3.0
	 */
	public void flush() throws SormulaException
	{
	    if (cache != null)
	    {
	        if (log.isDebugEnabled()) log.debug("flush for Table " + rowClass.getCanonicalName());
	        cache.write();
	        cache.evictAll();
	    }
	}
	

	/**
	 * Indicates the order in which annotations are processed. Prior to version 3.0, table annotations
	 * were read first and thus had precedence over row annotations. Returns false by default. Set
	 * LEGACY_ANNOTATION_PRECEDENCE=true to have this method return true.
	 * 
	 * @return true if table annotations have precedence over row annotations
	 * @since 3.0
	 */
    public boolean isLegacyAnnotationPrecedence()
    {
        return legacyAnnotationPrecedence;
    }


    /**
     * Initializes type translators for table. Invoked by the constructor. Override to add 
     * custom types via {@link #putTypeTranslator(Class, TypeTranslator)}.
     * 
     * @throws SormulaException if error
     * @since 1.9.2 and 2.3.2
     */
    protected void initTypeTranslatorMap() throws SormulaException
    {
        typeTranslatorMap = new HashMap<String, TypeTranslator<?>>();
        
        // process any type annotations
        try
        {
            if (legacyAnnotationPrecedence)
            {
                // table then row
                new ExplicitTypeAnnotationReader(this, this.getClass(), rowClass).install();
            }
            else
            {
                // row then table
                new ExplicitTypeAnnotationReader(this, rowClass, this.getClass()).install();
            }
        }
        catch (Exception e)
        {
            throw new SormulaException("error getting ExplicitType from table " + 
                    getClass().getCanonicalName(), e);
        }
    }
    
    
    /**
     * Initialize all name translators annotated on table. Subclasses of {@link Table} may
     * contain {@link NameTranslator} annotations instead of annotating row class.
     * 
     * @param rowAnnotation the annotation that defines name translator(s)
     * @return list of name translators; empty list if none
     * @since 1.8 and 2.2
     */
    @SuppressWarnings("unchecked") // type of name translator is not known until runtime
    protected List<? extends NameTranslator> initNameTranslators(Row rowAnnotation) 
    {
        Class<? extends NameTranslator>[] nameTranslatorClasses;
        
        if (rowAnnotation != null)
        {
            // row annotation is available
            // remove if statement when deprecated is removed
            if (rowAnnotation.nameTranslator().getName().equals(NoNameTranslator.class.getName()))
            {
                // deprecated use is default, use replacement method
                nameTranslatorClasses = rowAnnotation.nameTranslators(); // keep this when deprecated is removed
            }
            else
            {
                // deprecated method defines something other than default, use it
                // remove this block when deprecated is removed
                nameTranslatorClasses = new Class[1];
                nameTranslatorClasses[0] = rowAnnotation.nameTranslator();
            }
        }
        else
        {
            // no row annotation so no name translators (empty array)
            nameTranslatorClasses = new Class[0]; 
        }
        
        // default name translator check
        if (nameTranslatorClasses.length == 0) 
        {
            // no name translators for row, use database translators as default
            nameTranslatorClasses = database.getNameTranslatorClasses().toArray(nameTranslatorClasses);
        }
        
        // instantiate name translators
        List<NameTranslator> translators = new ArrayList<NameTranslator>(nameTranslatorClasses.length);
        for (Class<? extends NameTranslator> ntc: nameTranslatorClasses)
        {
            try
            {
                translators.add(ntc.newInstance());
            }
            catch (Exception e)
            {
                log.error("error creating name translator", e);
            }
        }
        
        return translators;
    }
    
    
    /**
     * Initializes table name from row annotation. If rowAnnotation is not null, then
     * Table name is {@link Row#tableName()}. If no name is supplied, then table name will
     * be row class simple name, {@link Class#getSimpleName()}.
     * 
     * @param rowAnnotation row annotation on table or null if none
     * @return name to use in SQL statements for table
     */
    protected String initTableName(Row rowAnnotation)
    {
        String name = null;
        
        if (rowAnnotation != null)
        {
            name = rowAnnotation.tableName();
        }
        
        if (name == null || name.length() == 0) 
        {
            // no table name is provided, get table name from class name
            name = translateName(rowClass.getSimpleName());
        }
        
        return name;
    }
    
    
    /**
     * Creates a {@link RowTranslator} for use by this table. Invoked by constructor.
     * 
     * @return row translator
     * @throws TranslatorException if error
     */
    protected RowTranslator<R> initRowTranslator() throws TranslatorException
    {
        // default
        return new RowTranslator<R>(this);
        // TODO add method getFields() to RowTranslator => getDeclaredFields or getFields based upon Row.inheritFields()
    }
    
    
    /**
     * Initialize cache for this table. Subclasses may override to provide custom implementation.
     * 
     * @return cache for this table
     * @throws CacheException if error
     * @since 3.0
     */
    protected Cache<R> initCache() throws CacheException
    {
        if (log.isDebugEnabled()) log.debug("initCache() for " + rowClass);
        Cache<R> cache = null;        
        Cached cachedAnnotation = initCachedAnnotation();
        
        if (cachedAnnotation != null && cachedAnnotation.enabled())
        {
            try
            {
                @SuppressWarnings("unchecked") // row type not known at compile time
                Constructor<Cache<R>> cacheConstructor = (Constructor<Cache<R>>)cachedAnnotation.type().getConstructor(
                        Table.class, Cached.class);
                cache = cacheConstructor.newInstance(this, cachedAnnotation);
            }
            catch (Exception e)
            {
                throw new CacheException("error creating cache", e);
            }
            
            if (database.getTransaction().isActive())
            {
                // cache must be opened for use since transaction notify was invoked prior to this table creation
                begin(database.getTransaction());
            }
        }

        return cache;
    }
    
    
    /**
     * Gets the annotation that defines caching. Subclasses can override to check for Cached 
     * annotation in other classes if desired.
     * 
     * @return cached annotation or null if no Cached annotation for table
     * @since 3.0
     */
    protected Cached initCachedAnnotation()
    {
        if (legacyAnnotationPrecedence)
        {
            // table, row, database
            return new CachedAnnotationReader(getClass(), rowClass, database.getClass()).getAnnotation();
        }
        else
        {
            // row, table, database
            return new CachedAnnotationReader(rowClass, getClass(), database.getClass()).getAnnotation();
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
     * @return row class supplied in constructor
     * @since 1.6 and 2.0
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
     * Use {@link #getNameTranslators()} instead of this method.
     * 
     * @return translator for converting java names to sql table and column names
     */
    @Deprecated
    public NameTranslator getNameTranslator()
    {
        if (nameTranslators.size() > 0) return nameTranslators.get(0);
        else return noNameTranslator; // to be backward compatible can't return null
    }


    /**
     * Gets the name translators used by {@link #translateName(String)}.
     * 
     * @return list of name translators; empty list if none
     * @since 1.8 and 2.2
     */
    public List<? extends NameTranslator> getNameTranslators()
    {
        return nameTranslators;
    }


    /**
     * Converts a Java class or field name to corresponding SQL name. Invokes 
     * {@link NameTranslator#translate(String, Class)} for all name translators in
     * the order that they are defined.
     * 
     * @return SQL name that corresponds to Java class or field name
     * @since 1.8 and 2.2
     * @see Row#nameTranslators()
     * @see Database#addNameTranslatorClass(Class)
     */
    public String translateName(String javaName)
    {
        String translatedName = javaName;
        
        for (NameTranslator nt: nameTranslators)
        {
            translatedName = nt.translate(translatedName, rowClass);
        }
        
        return translatedName;
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
     * Gets fields for table record class that are annotated with {@link SelectCascade#lazy()} true.
     * 
     * @return list of fields
     * @since 1.8 and 2.2
     */
    public List<Field> getLazySelectCascadeFields()
    {
        if (lazySelectCascadeFields == null)
        {
            // create only when first asked
            lazySelectCascadeFields = new ArrayList<Field>();
            
            // for all fields
            for (Field field: getRowClass().getDeclaredFields())
            {
                SelectCascadeAnnotationReader scar = new SelectCascadeAnnotationReader(field);
                SelectCascade[] selectCascades = scar.getSelectCascades();
                
                for (SelectCascade c: selectCascades)
                {
                    if (c.lazy()) lazySelectCascadeFields.add(field);
                }
            }
        }
        
        return lazySelectCascadeFields;
    }


    /**
     * Creates new instance of row. Typically used by select operations for
     * each row that is read from result set.
     * 
     * @return new instance of row created with zero-arg constructor
     * @throws SormulaException if error
     * @since 1.7 and 2.1
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
     * @since 1.6 and 2.0
     */
    public void putTypeTranslator(Class<?> typeClass, TypeTranslator<?> typeTranslator)
    {
        putTypeTranslator(typeClass.getCanonicalName(), typeTranslator);
    }

    
    /**
     * Same as {@link #putTypeTranslator(Class, TypeTranslator)} but uses class name. Usefull for adding
     * primative types like "int", "boolean", "float", etc.
     * 
     * @param typeClassName class name that translator operates upon
     * @param typeTranslator translator to use for typeClass
     * @since 1.9.2 and 2.3.2
     */
    public void putTypeTranslator(String typeClassName, TypeTranslator<?> typeTranslator)
    {
        if (log.isDebugEnabled()) log.debug("adding " + typeTranslator + " for " + typeClassName);
        typeTranslatorMap.put(typeClassName, typeTranslator);
    }
    
    
    /**
     * Gets the translator to use to convert a value to a prepared statement or to 
     * convert from a result set. If none are set for this table, then
     * translator is obtained from {@link Database#getTypeTranslator(Class)}. See
     * {@link Database#getTypeTranslator(Class)} for more details.
     * 
     * @param typeClass class that translator operates upon
     * @return translator to use for typeClass
     * @since 1.6 and 2.0
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
     * @since 1.7 and 2.1
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
     * @since 1.7 and 2.1
     */
    public List<R> selectAllWhere(String whereConditionName, Object...parameters) throws SormulaException
    {
        ArrayListSelectOperation<R> operation = new ArrayListSelectOperation<R>(this, whereConditionName);
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
     * @since 1.7 and 2.1
     */
    public List<R> selectAllWhereOrdered(String whereConditionName, String orderByName, Object...parameters) throws SormulaException
    {
        ArrayListSelectOperation<R> operation = new ArrayListSelectOperation<R>(this, whereConditionName);
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
    	ArrayListSelectOperation<R> operation = new ArrayListSelectOperation<R>(this, "");
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
    	ScalarSelectOperation<R> operation = new ScalarSelectOperation<R>(this, "");
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
        org.sormula.operation.SelectCountOperation<R> selectCountOperation = new SelectCountOperation<R>(this);
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
            new org.sormula.operation.aggregate.SelectCountOperation<R, T>(this, expression);
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
        SelectAggregateOperation<R, T> selectOperation = new SelectMinOperation<R, T>(this, expression);
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
        SelectAggregateOperation<R, T> selectOperation = new SelectMaxOperation<R, T>(this, expression);
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
        SelectAggregateOperation<R, T> selectOperation = new SelectAvgOperation<R, T>(this, expression);
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
     * @since 1.7 and 2.1
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
     * @since 1.7 and 2.1
     */
    public <T> T selectSum(String expression, String whereConditionName, Object...parameters) throws SormulaException
    {
        SelectAggregateOperation<R, T> selectOperation = new SelectSumOperation<R, T>(this, expression);
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
     * Inserts a collection of rows in batch mode. See limitations about batch inserts
     * in {@link ModifyOperation#setBatch(boolean)}.
     * 
     * @param rows rows to insert
     * @return count of rows affected
     * @throws SormulaException if error
     * @since 1.9 and 2.3
     */
    public int insertAllBatch(Collection<R> rows) throws SormulaException
    {
        InsertOperation<R> operation = new InsertOperation<R>(this);
        operation.setBatch(true);
        return operation.insertAll(rows);
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
     * Updates collection of rows using primary key in batch mode. Primary key must be defined by one
     * or more {@link Column#primaryKey()} annotations. See limitations about batch updates
     * in {@link ModifyOperation#setBatch(boolean)}.
     * 
     * @param rows rows to update
     * @return count of rows affected
     * @throws SormulaException if error
     * @since 1.9 and 2.3
     */
    public int updateAllBatch(Collection<R> rows) throws SormulaException
    {
        UpdateOperation<R> operation = new UpdateOperation<R>(this);
        operation.setBatch(true);
        return operation.updateAll(rows);
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
     * Deletes many rows by primary key in batch mode. Primary key must be defined by one
     * or more {@link Column#primaryKey()} annotations.  See limitations about batch deletes
     * in {@link ModifyOperation#setBatch(boolean)}.
     * 
     * @param rows get primary key values from each row in this collection
     * @return count of rows affected
     * @throws SormulaException if error
     * @since 1.9 and 2.3
     */
    public int deleteAllBatch(Collection<R> rows) throws SormulaException
    {
        DeleteOperation<R> operation = new DeleteOperation<R>(this);
        operation.setBatch(true);
        return operation.deleteAll(rows);
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
    
    
    /**
     * {@link TransactionListener#begin(Transaction)} implementation. Normally other
     * classes do not invoke this method. Subclasses may override if needed.
     * 
     * @param transaction database transaction that invoked this method
     * @since 3.0
     */
    public void begin(Transaction transaction)
    {
        if (log.isDebugEnabled()) log.debug("begin(transaction) " + getQualifiedTableName());
        if (cache != null)
        {
            try
            {
                cache.begin(transaction);
            }
            catch (CacheException e)
            {
                log.error("cache error", e);
            }
        }
    }
    
    
    /**
     * {@link TransactionListener#commit(Transaction)} implementation. Normally other
     * classes do not invoke this method. Subclasses may override if needed.
     * 
     * @param transaction database transaction that invoked this method
     * @since 3.0
     */
    public void commit(Transaction transaction)
    {
        if (log.isDebugEnabled()) log.debug("commit(transaction) " + getQualifiedTableName());
        if (cache != null)
        {
            try
            {
                cache.commit(transaction);
            }
            catch (CacheException e)
            {
                log.error("cache error", e);
            }
        }
    }
    
    
    /**
     * {@link TransactionListener#rollback(Transaction)} implementation. Normally other
     * classes do not invoke this method. Subclasses may override if needed.
     * 
     * @param transaction database transaction that invoked this method
     * @since 3.0
     */
    public void rollback(Transaction transaction)
    {
        if (log.isDebugEnabled()) log.debug("rollback(transaction) " + getQualifiedTableName());
        if (cache != null)
        {
            try
            {
                cache.rollback(transaction);
            }
            catch (CacheException e)
            {
                log.error("cache error", e);
            }
        }
    }
}
