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
package org.sormula.translator;

import java.lang.reflect.Field;

import org.sormula.annotation.Column;
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
 * Translates a row to prepared statement and from result set. {@linkplain ColumnTranslator} objects
 * are created for each column based upon annoations on row class. If no annotation exists for a member then
 * column name is same as member name and {@linkplain StandardColumnTranslator} is used.
 * 
 * @since 1.0
 * @author Jeff Miller
 * @param <R> row class
 */
public class RowTranslator<R> extends ColumnsTranslator<R>
{
    private static final ClassLogger log = new ClassLogger();
    NameTranslator nameTranslator;
    PrimaryKeyWhereTranslator<R> primaryKeyWhereTranslator;
    String unusedColumnInsertNamesSql;
    String unusedColumnInsertValuesSql;
    String unusedColumnUpdateSql;

    
    /**
     * Constructs for a row class.
     * 
     * @param rowClass read annotations from this class
     * @param nameTranslator obtain table and column names from this translator
     * @throws TranslatorException if error
     */
    public RowTranslator(Class<R> rowClass, NameTranslator nameTranslator) throws TranslatorException
    {
        super(rowClass);
        this.nameTranslator = nameTranslator;
        initColumnTranslators();
        initUnusedColumnSql(rowClass);
        primaryKeyWhereTranslator = new PrimaryKeyWhereTranslator<R>(this);
        
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
     * Process {@link Transient} and {@link Cascade} annotations.
     * 
     * @throws TranslatorException if error
     */
    @SuppressWarnings("unchecked") // annotations cannot be parameterized
    protected void initColumnTranslators() throws TranslatorException
    {
        Class<R> rowClass = getRowClass();
        Field[] fields = rowClass.getDeclaredFields();
        initColumnTranslatorList(fields.length);
        
        // for all fields
        for (Field f: fields)
        {
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
                
                if (columnName.equals(""))
                {
                    // column name not supplied or no annotation, default is field name
                    columnName = nameTranslator.translate(f.getName(), rowClass);
                }
                
                ColumnTranslator<R> translator = (ColumnTranslator<R>)
                    AbstractColumnTranslator.newInstance(columnTranslatorClass, f, columnName);
                addColumnTranslator(translator);
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
        UnusedColumns unusedColumnsAnnotation = rowClass.getAnnotation(UnusedColumns.class);
        
        if (unusedColumnsAnnotation != null)
        {
            // at least one unused column
            
            // allocate typical space needed
            UnusedColumn[] unusedColumnAnnotations = unusedColumnsAnnotation.unusedColumns();
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
     * Gets translator defined by {@linkplain Row#nameTranslator()}.
     * 
     * @return translator for converting java names to sql names
     */
    public NameTranslator getNameTranslator()
    {
        return nameTranslator;
    }


    /**
     * Creates new instance of row object using zero-argument constructor. New instances
     * are typically created by select operations when they are reading data from a
     * result set.
     * 
     * @return object to hold one row 
     */
    public R newInstance() throws IllegalAccessException, InstantiationException
    {
        return getRowClass().newInstance();
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
     * Gets column=value for all columns for update statement.
     * 
     * @return ", uc1=v1, uc2=v2, uc3=v3..." for all unused columns
     */
    public String getUnusedColumnUpdateSql()
    {
        return unusedColumnUpdateSql;
    }
}
