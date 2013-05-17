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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

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
import org.sormula.operation.ModifyOperation;
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
    boolean inheritedFields;
    boolean zeroRowCountPostExecute;
    
    
    /**
     * Constructs for a table.
     * 
     * @param table table associated with this row translator
     * @throws TranslatorException if error
     * @since 1.6 and 2.0
     */
    @Deprecated // use RowTranslator(Table<R>, Row) constructor 
    public RowTranslator(Table<R> table) throws TranslatorException
    {
        this(table, null);
    }
    
    
    /**
     * Constructs for a table.
     * 
     * @param table table associated with this row translator
     * @param rowAnnotation row annotation on table or row class
     * @throws TranslatorException if error
     * @since 3.0
     */
    public RowTranslator(Table<R> table, Row rowAnnotation) throws TranslatorException
    {
        super(table.getRowClass());
        this.table = table;
        
        if (rowAnnotation != null)
        {
            inheritedFields = rowAnnotation.inhertedFields();
            zeroRowCountPostExecute = rowAnnotation.zeroRowCountPostExecute();
        }
        
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
     * Reports if super class fields are used.
     * 
     * @return true if fields used include super class fields; false to use only fields in {@link #getRowClass()}
     * @since 3.0
     * @see Row#inhertedFields()
     */
    public boolean isInheritedFields()
    {
        return inheritedFields;
    }

    
    /**
     * Sets if super class fields are used.
     * @param inheritedFields true if fields used include super class fields; false to use only fields in {@link #getRowClass()}
     * @since 3.0
     * @see Row#inhertedFields()
     */
    public void setInheritedFields(boolean inheritedFields)
    {
        this.inheritedFields = inheritedFields;
    }


    /**
     * Reports when to invoke {@link ModifyOperation} post execute methods.
     * 
     * @return true to invoke post execute methods unconditionally; false to invoke 
     * post execute methods only when database has been modified by insert, update, or delete
     * @since 3.0
     * @see Row#zeroRowCountPostExecute()
     */
    public boolean isZeroRowCountPostExecute()
    {
        return zeroRowCountPostExecute;
    }


    /**
     * Sets when to invoke {@link ModifyOperation} post execute methods.
     * 
     * @param zeroRowCountPostExecute true to invoke post execute methods unconditionally; 
     * false to invoke post execute methods only when database has been modified by 
     * insert, update, or delete
     * @since 3.0
     * @see Row#zeroRowCountPostExecute()
     */
    public void setZeroRowCountPostExecute(boolean zeroRowCountPostExecute)
    {
        this.zeroRowCountPostExecute = zeroRowCountPostExecute;
    }


    /**
     * Gets all of the declared fields for {@link #getRowClass()}. Also gets declared fields for
     * subclass(es) if {@link #isInheritedFields()} is true. Subclass fields appear in array prior
     * to superclass fields.
     * 
     * @return all fields for row class (and optionaly row class superclass(es))
     * @since 3.0
     */
    public Field[] getDeclaredFields()
    {
        if (!inheritedFields || rowClass.getSuperclass() == null)
        {
            // simple case
            return rowClass.getDeclaredFields();
        }
        else
        {
            // find recursively
            List<Field[]> classHierarchyFields = new ArrayList<Field[]>();
            getDeclaredFields(rowClass, classHierarchyFields);
            
            // calculate return array length
            int length = 0;
            for (Field[] fields : classHierarchyFields) length += fields.length;
            
            // create return array
            int i = 0;
            Field[] returnFields = new Field[length];
            for (Field[] fields : classHierarchyFields) 
            {
                for (Field f : fields) returnFields[i++] = f;      
            }
            
            return returnFields;
        }
    }
    
    
    /**
     * Recurisvely searches for declared fields in super class(es) of clazz and then clazz parameter.
     * 
     * @param clazz get fields from this class and superclass(es)
     * @param classHierarchyFields each class fields are added to this list 
     * @since 3.0
     */
    protected void getDeclaredFields(Class<?> clazz, List<Field[]> classHierarchyFields)
    {
        Class<?> superClass = clazz.getSuperclass();
        
        if (superClass != null)
        {
            // check super class first 
            getDeclaredFields(superClass, classHierarchyFields);
        }

        // clazz's fields
        classHierarchyFields.add(clazz.getDeclaredFields());
    }

    
    /**
     * Gets a declared field of {@link #getRowClass()}. Searches for declared fields in
     * subclass(es) of {@link #getRowClass()} if {@link #isInheritedFields()} is true. Subclasses 
     * are searched prior to to superclasses.
     *
     * @param fieldName get field for this name
     * @return field for row class (and optionaly row class superclass(es)); null if field not found
     * @since 3.0
     */
    public Field getDeclaredField(String fieldName)
    {
        if (inheritedFields)
        {
            // recursively check starting with super most class
            return getDeclaredField(rowClass, fieldName);
        }
        else
        {
            // search row class only
            Field field;
            
            try
            {
                field = rowClass.getDeclaredField(fieldName);
            }
            catch (NoSuchFieldException e)
            {
                field = null;
            }
            
            return field;
        }
    }
    
    
    /**
     * Recurisvely searches for declared field in superclass(es) of clazz and then clazz parameter.
     * 
     * @param clazz get fields from this class and superclass(es)
     * @param fieldName get field for this name
     * @return field or null if not found
     * @since 3.0
     */
    protected Field getDeclaredField(Class<?> clazz, String fieldName)
    {
        Field field = null;
        
        if (inheritedFields)
        {
            Class<?> superClass = clazz.getSuperclass();
            
            if (superClass != null)
            {
                // check super class first
                field = getDeclaredField(superClass, fieldName);
            }
        }
        
        if (field == null)
        {
            // not found in super class, check clazz
            try
            {
                field = clazz.getDeclaredField(fieldName);
            }
            catch (NoSuchFieldException e)
            {
            }
        }
        
        return field;
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
        Field[] fields = getDeclaredFields();
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
