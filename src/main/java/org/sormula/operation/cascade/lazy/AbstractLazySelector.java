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
package org.sormula.operation.cascade.lazy;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sormula.Database;
import org.sormula.SormulaException;
import org.sormula.Table;
import org.sormula.Transaction;
import org.sormula.annotation.Transient;
import org.sormula.annotation.cascade.SelectCascade;
import org.sormula.annotation.cascade.SelectCascadeAnnotationReader;
import org.sormula.log.ClassLogger;
import org.sormula.operation.cascade.SelectCascadeOperation;
import org.sormula.reflect.SormulaField;


/**
 * Typical state and behavior for performing lazy select cascades. This class keeps track of fields with 
 * pending lazy selects and performs {@link #checkLazySelects(String)} only once per lazy select field. 
 * <p>
 * This class may be used as a super class of row that has a reference to a lazy select field or it may be
 * used as a delegate when row requires a different super class.
 * <p>
 * Subclass must implement {@link #initDatabase()} to create the {@link Database} to use for the lazy
 * select. Usually {@link #pendingLazySelects(Database)} is overridden to record information about the
 * current {@link Database} to be used by {@link #initDatabase()}.
 * 
 * @author Jeff Miller
 * @since 1.8 and 2.2
 * 
 * @param <R> type of row class that is to be lazily selected
 */
abstract public class AbstractLazySelector<R> implements LazySelectable, Serializable
{
    private static final long serialVersionUID = 1L;
    private static final ClassLogger log = new ClassLogger();
    
    @Transient
    R source;

    @Transient
    boolean pendingLazySelects; 
    
    @Transient
    Map<String, Field> pendingLazySelectFields;

    @Transient
    Database database;

    @Transient
    boolean useTransaction;
    
    @Transient
    boolean localTransaction;
    

    /**
     * Constructs for use when AbstractLazySelector is base class of row that will contain lazy select fields. Typically 
     * the derived class is the one side of a one-to-many relationship or subclass has a reference to the 
     * other class in a one-to-one relationship. {@link #setUseTransaction(boolean)} is true by default.
     */
    @SuppressWarnings("unchecked") // assumes this class is super class of row
    public AbstractLazySelector()
    {
        source = (R)this;
        setUseTransaction(true);
    }

    
    /**
     * Constructs for use as delegate for row that will contain lazy select fields. 
     * {@link #setUseTransaction(boolean)} is true by default.
     * 
     * @param source row that contains fields with {@link SelectCascade#lazy()} is true; typically source
     * is the one side of a one-to-many relationship or source has a reference to the other class in a 
     * one-to-one relationship
     */
    public AbstractLazySelector(R source)
    {
        this.source = source;
        setUseTransaction(true);
    }
    
    
    /**
     * Invoked by {@link #checkLazySelects(String)} to create a {@link Database} instance that will be used to 
     * perform the lazy selects.
     * 
     * @throws LazyCascadeException if error
     * @since 1.9 and 2.3
     */
    abstract protected void openDatabase() throws LazyCascadeException;
    
    
    /**
     * Invoked by {@link #checkLazySelects(String)} to close the {@link Database} instance that 
     * was used to perform the lazy selects.
     * 
     * @throws LazyCascadeException
     * @since 1.9 and 2.3
     */
    abstract protected void closeDatabase() throws LazyCascadeException;
    
    
    /**
     * Gets the owner of the field(s) that will be lazily selected.
     *  
     * @return reference to this class if this class is a super class of row or source parameter if this class is a 
     * delegate selector for row
     */
    public R getSource()
    {
        return source;
    }


    /**
     * Reports if a transaction will be created if needed. The default value is true.
     * 
     * @return true if a transaction is to be created if none is active
     */
    public boolean isUseTransaction()
    {
        return useTransaction;
    }


    /**
     * Sets transaction use. If true and {@link Database#getTransaction()} reports false from {@link Transaction#isActive()},
     * meaning no transaction is active, then this class will create a transaction with {@link #begin()} prior to performing the lazy
     * selects.
     * 
     * @param useTransaction true to create a transaction for lazy select if none is active; false to never
     * create a transaction
     * @see #begin()
     * @see #commit()
     * @see #rollback()
     */
    public void setUseTransaction(boolean useTransaction)
    {
        this.useTransaction = useTransaction;
    }


    /**
     * Reports if this class has created a transaction for use when performing a lazy select.
     * 
     * @return true when a transaction was created by this class
     */
    public boolean isLocalTransaction()
    {
        return localTransaction;
    }


    /**
     * Gets the database to use for lazy select. 
     * 
     * @return database to use for lazy select
     */
    public Database getDatabase() 
    {
        return database;
    }
    
    
    /**
     * Sets the database to use for lazy selects.
     * 
     * @param database lazy select database (typically created in {@link #openDatabase()}
     * @since 1.9 and 2.3
     */
    public void setDatabase(Database database)
    {
        this.database = database;
    }
    
    
    /**
     * Reports lazy select status. Set to true when {@link #pendingLazySelects(Database)}
     * is invoked. Set to false when {@link #checkLazySelects(String)} determines that
     * there are no more fields to select.
     * 
     * @return true if one or more fields have lazy selects that have not been performed
     */
    public boolean isPendingLazySelects()
    {
        return pendingLazySelects;
    }


    /**
     * Gets the fields that will be initialized with lazy select. Field map is initialized the
     * first time that {@link #checkLazySelects(String)} is invoked while {@link #isPendingLazySelects()}
     * is true.
     * 
     * @return map of fields (key is field name); null if fields have not been initialized
     */
    public Map<String, Field> getPendingLazySelectFields()
    {
        return pendingLazySelectFields;
    }


    /**
     * Notifies selector that at least one field has lazy select to be performed.
     * Subclasses typically save some information about the database that can be used
     * by {@link #checkLazySelects(String)} to perform the lazy select.
     *  
     * @param database perform lazy selects from this database
     * @throws LazyCascadeException if error
     */
    public void pendingLazySelects(Database database) throws LazyCascadeException
    {
        pendingLazySelects = true;
    }
    

    /**
     * Checks if field should be selected. If field is defined with a lazy select and the
     * lazy select has not been performed, then {@link #lazySelect(Field)} will be invoked to
     * select the field. A field is selected only once.
     * 
     * @param fieldName name of field to select with lazy select
     * @throws LazyCascadeException if error
     */
    public void checkLazySelects(String fieldName) throws LazyCascadeException
    {
        if (log.isDebugEnabled()) log.debug("checkLazySelects() " + fieldName);
        if (pendingLazySelects)
        {
            // row has been identified with lazy selects
            openDatabase();
            
            if (pendingLazySelectFields == null)
            {
                // init first time
                initPendingLazySelectCascadeFields();
            }
                
            Field field = pendingLazySelectFields.get(fieldName);
                    
            if (field != null)
            {
                // fieldName is pending (not yet selected)
                lazySelect(field);
                
                // don't do field again
                pendingLazySelectFields.remove(fieldName);
                
                // more to do?
                pendingLazySelects = pendingLazySelectFields.size() > 0;
            }
            
            closeDatabase();
        }
    }
    
    
    /**
     * Performs lazy select for all select cascades of field where {@link SelectCascade#lazy()} is
     * true.
     * 
     * @param field field to affect
     * @throws LazyCascadeException if error
     */
    protected void lazySelect(Field field) throws LazyCascadeException
    {
        try
        {
            if (log.isDebugEnabled()) log.debug("lazySelect() " + field.getName());
            
            // init loop variables
            SelectCascadeAnnotationReader scar = new SelectCascadeAnnotationReader(field);
            SormulaField<R, ?> targetField = new SormulaField<R, Object>(scar.getSource());
            Table<?> targetTable = getDatabase().getTable(scar.getTargetClass());
            
            begin();
            
            // for select cascade annotation(s)
            for (SelectCascade c: scar.getSelectCascades())
            {
                if (c.lazy())
                {
                    // select cascade currently does not use source table so can't write test to verfiy source table is correct
                    @SuppressWarnings("unchecked") // source field type is not known at compile time
                    Table<R> sourceTable = (Table<R>)getDatabase().getTable(field.getDeclaringClass());
                    
                    @SuppressWarnings("unchecked") // target field type is not known at compile time
                    SelectCascadeOperation<R, ?> operation = new SelectCascadeOperation(sourceTable, targetField, targetTable, c);
                    try
                    {
                        if (c.setForeignKeyValues()) operation.setForeignKeyFieldNames(scar.getForeignKeyValueFields());
                        if (c.setForeignKeyReference()) operation.setForeignKeyReferenceFieldName(scar.getForeignKeyReferenceField());
                        operation.prepare();
                        operation.cascade(source);
                    }
                    finally
                    {
                        operation.close();
                    }
                }
            }
            
            commit();
        }
        catch (SormulaException e)
        {
            try
            {
                rollback();
            }
            catch (SormulaException re)
            {
                // log message instead of throwing so SormulaException can be thrown
                log.error("rollback error", re);
            }
            
            throw new LazyCascadeException("error performing lazy select for " + field.getName(), e);
        }
    }
    
    
    /**
     * Initializes map of all fields that have a lazy select. Map is available with
     * {@link #getPendingLazySelectFields()}.
     * 
     * @throws LazyCascadeException if error
     */
    protected void initPendingLazySelectCascadeFields() throws LazyCascadeException
    {
        try
        {
            // delay init until first cascade request so less cpu time if no cascades are needed
            if (log.isDebugEnabled()) log.debug("initPendingLazySelectCascadeFields() for source=" + source);
            
            // create map of field name to field for all that contain lazy select cascade
            List<Field> lazyFields = getDatabase().getTable(source.getClass()).getLazySelectCascadeFields();
            pendingLazySelectFields = new HashMap<String, Field>(lazyFields.size() * 2);
            for (Field f: lazyFields) pendingLazySelectFields.put(f.getName(), f);
        }
        catch (SormulaException e)
        {
            throw new LazyCascadeException("error initializing lazy select fields for source=" + source, e);
        }
    }


    /**
     * Starts a transaction for {@link #lazySelect(Field)}. A transaction is used only if {@link #isUseTransaction()} is
     * true and the transaction for database is not already in use.
     * 
     * @throws SormulaException if error
     */
    protected void begin() throws SormulaException
    {
        Transaction transaction = getDatabase().getTransaction();
        
        if (useTransaction && !transaction.isActive())
        {
            // transaction is desired and no transaction is active, start one
            localTransaction = true;
            transaction.begin();
        }
    }
    
    
    /**
     * Commits the transaction for {@link #lazySelect(Field)}. A transaction is committed only
     * if {@link #begin()} started a transaction as indicated by true from {@link #isLocalTransaction()}.
     *  
     * @throws SormulaException if error
     */
    protected void commit() throws SormulaException
    {
        if (localTransaction)
        {
            // transaction was started by me
            localTransaction = false;
            getDatabase().getTransaction().commit();
        }
    }
    
    
    /**
     * Rolls back the transaction for {@link #lazySelect(Field)}. A transaction is rolled back only
     * if {@link #begin()} started a transaction as indicated by true from {@link #isLocalTransaction()}.
     *  
     * @throws SormulaException if error
     */
    protected void rollback() throws SormulaException
    {
        if (localTransaction)
        {
            // transaction was started by me
            localTransaction = false;
            getDatabase().getTransaction().rollback();
        }
    }
}
