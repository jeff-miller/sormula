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
package org.sormula.translator;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.sormula.Table;
import org.sormula.annotation.Column;
import org.sormula.annotation.ImplicitType;
import org.sormula.annotation.ImplicitTypeAnnotationReader;
import org.sormula.annotation.Row;
import org.sormula.annotation.Transient;
import org.sormula.annotation.UnusedColumn;
import org.sormula.annotation.UnusedColumns;
import org.sormula.annotation.cascade.Cascade;
import org.sormula.annotation.cascade.OneToManyCascade;
import org.sormula.annotation.cascade.OneToOneCascade;
import org.sormula.log.ClassLogger;
import org.sormula.translator.standard.StandardColumnTranslator;


/**
 * Translates a row to prepared statement and from result set. {@link ColumnTranslator} objects
 * are created for each column based upon annotations on row class. If no annotation exists for a member then
 * column name is same as member name and {@link StandardColumnTranslator} is used.
 * 
 * @since 1.0
 * @author Jeff Miller
 * @param <R> row class
 */
public class RowTranslator<R> extends ColumnsTranslator<R>
{
    private static final ClassLogger log = new ClassLogger();
    Table<R> table;
    PrimaryKeyWhereTranslator<R> primaryKeyWhereTranslator;
    String unusedColumnInsertNamesSql;
    String unusedColumnInsertValuesSql;
    String unusedColumnUpdateSql;
    ColumnTranslator<R> identityColumnTranslator;
    
    
    /**
     * Constructs for a table.
     * 
     * @param table table associated with this row translator
     * @throws TranslatorException if error
     * @since 1.6 and 2.0
     */
    public RowTranslator(Table<R> table) throws TranslatorException
    {
        super(table.getRowClass());
        this.table = table;
        initColumnTranslators();
        initUnusedColumnSql(rowClass);
        primaryKeyWhereTranslator = new PrimaryKeyWhereTranslator<>(this);
        
        if (log.isDebugEnabled())
        {
            log.debug(rowClass.getCanonicalName() + " primary key columns:");
            
            for (ColumnTranslator<R> ct: primaryKeyWhereTranslator.getColumnTranslatorList())
            {
                log.debug(ct.getColumnName());
            }
        }
    }

    
    /**
     * Column translator used to set the value of a row column that is the identity column 
     * for row. Typically used by insert operations for column that is annotated with
     * {@link Column#identity()}.
     * 
     * @return column translator for identity column; null if no identity column
     * @see Column#identity()
     */
    public ColumnTranslator<R> getIdentityColumnTranslator()
    {
        return identityColumnTranslator;
    }

    
    /**
     * Process {@link Transient} and {@link Cascade} annotations.
     * <p>
     * Versions prior to 1.9.2 did not allow type translators to be replaced. Starting with
     * version 1.9.2 and 2.3.2, this method will replace existing type translator with new
     * one {@link ImplicitType#translator()} is different from existing.
     * 
     * @throws TranslatorException if error
     */
    @SuppressWarnings("unchecked") // field types are only known at runtime 
    protected void initColumnTranslators() throws TranslatorException
    {
        Class<R> rowClass = getRowClass();
        Field[] fields = rowClass.getDeclaredFields();
      //TODO Field[] fields = rowClass.getFields(); // TODO allow subclass fields?
        initColumnTranslatorList(fields.length);
        
        // for all fields
        for (Field f: fields)
        {
            if (Modifier.isStatic(f.getModifiers())) continue; // skip static members

            if (f.isAnnotationPresent(Transient.class))
            {
                // transient column, don't translate
            	if (log.isDebugEnabled()) log.debug("transient " + rowClass.getCanonicalName() + "#" + f.getName());
            }
            else if (f.isAnnotationPresent(Cascade.class) ||
                     f.isAnnotationPresent(OneToManyCascade.class) ||
                     f.isAnnotationPresent(OneToOneCascade.class))
            {
                if (log.isDebugEnabled()) log.debug("cascade " + rowClass.getCanonicalName() + "#" + f.getName());
            }
            else 
            {
                try
                {
                    // check for Type annotation on field type and field 
                    new ImplicitTypeAnnotationReader(table, f).install();
                }
                catch (Exception e)
                {
                    throw new TranslatorException("error installing ImplicitType", e);
                }
                
                // determine column translator to use
                String columnName = "";
                Class<? extends ColumnTranslator> columnTranslatorClass;
                Column columnAnnotation = f.getAnnotation(Column.class);
    
                if (columnAnnotation != null)
                {
                    // use Column annotation
                    columnTranslatorClass = (Class<? extends ColumnTranslator>)columnAnnotation.translator();
                    columnName = columnAnnotation.name();
                }
                else
                {
                    // no Column annotation for field
                    columnTranslatorClass = (Class<? extends ColumnTranslator>)StandardColumnTranslator.class;
                }
                
                if (columnName.length() == 0)
                {
                    // column name not supplied or no annotation, default is field name
                    columnName = table.translateName(f.getName());
                }
                
                // create column translator
                ColumnTranslator<R> translator = (ColumnTranslator<R>)
                    AbstractColumnTranslator.newInstance(columnTranslatorClass, f, columnName);
                addColumnTranslator(translator);

                if (translator instanceof AbstractColumnTranslator) 
                {
                    TypeTranslator<?> typeTranslator = table.getTypeTranslator(f.getType());
                    
                    if (typeTranslator != null)
                    {
                        if (log.isDebugEnabled()) log.debug("set type translator=" + typeTranslator + " on column translator=" + translator);
                        // set type translator for subclasses of AbstractColumnTranslator 
                        ((AbstractColumnTranslator)translator).setTypeTranslator(typeTranslator);
                    }
                }
                
                if (translator.isIdentity())
                {
                    if (identityColumnTranslator == null)
                    {
                        // first identity column encountered
                        identityColumnTranslator = translator;
                    }
                    else
                    {
                        throw new TranslatorException("more than one identity column declared at " + 
                                rowClass.getCanonicalName()+ "#" + f.getName());
                    }
                }
            }
        }
    }
    
    
    /**
     * Do not use. Replaced with {@link ImplicitTypeAnnotationReader}.
     * @since 1.6 and 2.0
     */
    @Deprecated
    protected TypeTranslator<?> readimplicitType(AnnotatedElement... annotatedElements) throws TranslatorException
    {
        return readImplicitType(annotatedElements);
    }
    
    
    /**
     * Do not use. Replaced with {@link ImplicitTypeAnnotationReader}.
     * Searches any {@link ImplicitType} annotations and returns new instance of {@link ImplicitType#translator()}
     * for the first one found.
     * 
     * @param annotatedElements objects that may contain an {@link ImplicitType} annotation
     * @return new {@link TypeTranslator} or null if no {@link ImplicitType} annotations
     * @throws TranslatorException if error creating new instance of type translator
     * @since 1.8 and 2.2
     */
    @Deprecated 
    protected TypeTranslator<?> readImplicitType(AnnotatedElement... annotatedElements) throws TranslatorException
    {
        for (AnnotatedElement ae : annotatedElements)
        {
            ImplicitType typeAnnotation = ae.getAnnotation(ImplicitType.class);
            
            if (typeAnnotation != null)
            {
                // create type translator
                try
                {
                    return typeAnnotation.translator().newInstance(); // deprecated since creates new instance even when not needed
                }
                catch (Exception e)
                {
                    throw new TranslatorException("error instantiating " + typeAnnotation.translator().getCanonicalName(), e);
                }
            }
        }
        
        // not found or error
        return null;
    }
    
    
    /**
     * Process {@link UnusedColumns} annotations.
     * 
     * @param rowClass
     * @throws TranslatorException
     */
    protected void initUnusedColumnSql(Class<R> rowClass) throws TranslatorException
    {
        // look for annotation on table subclass and row class
        UnusedColumns unusedColumnsAnnotation = table.getClass().getAnnotation(UnusedColumns.class);
        if (unusedColumnsAnnotation == null) unusedColumnsAnnotation = rowClass.getAnnotation(UnusedColumns.class);
        
        if (unusedColumnsAnnotation != null)
        {
            // at least one unused column
            UnusedColumn[] unusedColumnAnnotations = unusedColumnsAnnotation.value();
            
            // allocate typical space needed
            StringBuilder insertNames = new StringBuilder(unusedColumnAnnotations.length * 20);
            StringBuilder insertValues = new StringBuilder(unusedColumnAnnotations.length * 5);
            StringBuilder update = new StringBuilder(unusedColumnAnnotations.length * 25);
            
            // for each unused
            for (UnusedColumn uc: unusedColumnAnnotations)
            {
                // preceed with comma so that it may be appended to end of normal sql
                insertNames.append(", ");
                insertValues.append(", ");
                update.append(", ");
                
                insertNames.append(uc.name());
                insertValues.append(uc.value());
                update.append(uc.name());
                update.append("=");
                update.append(uc.value());
            }
            
            unusedColumnInsertNamesSql = insertNames.toString();    // ", uc1, uc2, uc3..."
            unusedColumnInsertValuesSql = insertValues.toString();  // ", v1, v2, v3..."
            unusedColumnUpdateSql = update.toString();              // ", uc1=v1, uc2=v2, uc3=v3..."
        }
        else
        {
            // none
            unusedColumnInsertNamesSql = "";
            unusedColumnInsertValuesSql = "";
            unusedColumnUpdateSql = "";
        }
    }
    
    
    /**
     * Gets translator defined by {@link Row#nameTranslator()}.
     * Use {@link Table#getNameTranslators()} instead of this method.
     * 
     * @return translator for converting java names to sql names
     */
    @Deprecated
    public NameTranslator getNameTranslator()
    {
        return table.getNameTranslator();
    }
    

    /**
     * Creates new instance of row object using zero-argument constructor. New instances
     * are typically created by select operations when they are reading data from a
     * result set.
     * 
     * Moved to {@link Table}.
     * 
     * @return object to hold one row 
     * @throws TranslatorException if new row instance cannot be created
     */
    @Deprecated
    public R newInstance() throws TranslatorException
    {
        R row;
        
        try
        {
            row = getRowClass().newInstance();
        }
        catch (Exception e)
        {
            throw new TranslatorException("error creating row instance for " + getRowClass().getName() +
                    "; make sure row has public zero-arg constructor", e);
        }
        
        return row;
    }


    /**
     * Gets primary key translator for use in where clause.
     * 
     * @return translator for primary key columns
     */
    public PrimaryKeyWhereTranslator<R> getPrimaryKeyWhereTranslator()
    {
        return primaryKeyWhereTranslator;
    }


    /**
     * Gets names of unused columns for insert statement.
     * 
     * @return ", uc1, uc2, uc3..." for all unused columns
     */
    public String getUnusedColumnInsertNamesSql()
    {
        return unusedColumnInsertNamesSql;
    }


    /**
     * Gets values for unused columns for insert statement.
     * 
     * @return ", v1, v2, v3..." for all unused columns
     */
    public String getUnusedColumnInsertValuesSql()
    {
        return unusedColumnInsertValuesSql;
    }


    /**
     * Gets column=value for all unused columns for update statement.
     * 
     * @return ", uc1=v1, uc2=v2, uc3=v3..." for all unused columns
     */
    public String getUnusedColumnUpdateSql()
    {
        return unusedColumnUpdateSql;
    }
}
