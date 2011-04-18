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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sormula.log.ClassLogger;


/**
 * Base class for translators that are made of a list of column translators. Translates row object columns 
 * to/from jdbc objects for a row class.
 *
 * @since 1.0
 * @author Jeff Miller
 * @param <R> class of row to translate
 */
public class ColumnsTranslator<R>
{
    private static final ClassLogger log = new ClassLogger();
    Class<R> rowClass;
    List<ColumnTranslator<R>> columnTranslatorList;
    Map<String, ColumnTranslator<R>> columnTranslatorMap; // key is field name
    boolean includeIdentityColumns;
    
    
    /**
     * Constructs for a row class.
     * 
     * @param rowClass class of row to translate
     * @throws TranslatorException if error
     */
    public ColumnsTranslator(Class<R> rowClass) throws TranslatorException
    {
        this.rowClass = rowClass;
        includeIdentityColumns = true;
    }
    

    /**
     * Tests if identity columns are used by this translator. Default is true. Typically
     * it is true except for insert operations.
     * 
     * @return true to include all columns; false to include only non identity columns
     */
    public boolean isIncludeIdentityColumns()
    {
        return includeIdentityColumns;
    }


    /**
     * Sets when to use identity columns.
     * 
     * @param includeIdentityColumns true to include all columns; false to include only non identity columns
     */
    public void setIncludeIdentityColumns(boolean includeIdentityColumns)
    {
        this.includeIdentityColumns = includeIdentityColumns;
    }
    
    
    protected void initColumnTranslatorList(int columns)
    {
        columnTranslatorList = new ArrayList<ColumnTranslator<R>>(columns);
        
        // for lookup by field name
        columnTranslatorMap = new HashMap<String, ColumnTranslator<R>>(columns * 2);
    }
    

    /**
     * Reads a record from a result set.
     * 
     * @param resultSet jdbc result set
     * @param parameterIndex starting parameter index
     * @param row write values into this object
     * @return last parameter index + 1
     * 
     * @throws TranslatorException if error
     */
    public int read(ResultSet resultSet, int parameterIndex, R row) throws TranslatorException
    {
        if (log.isDebugEnabled()) log.debug("read parameterIndex=" + parameterIndex + " row=" + row);
        int p = parameterIndex;
        
        try
        {
            for (ColumnTranslator<R> c: columnTranslatorList)
            {
                if (includeIdentityColumns || !c.isIdentity())
                {
                    if (log.isDebugEnabled()) log.debug("read result set parameter " + p);
                    c.read(resultSet, p, row);
                    ++p;
                }
            }
        }
        catch (Exception e)
        {
            throw new TranslatorException("error reading result set parameter " + p, e);
        }
        
        return p;
    }
    
    
    /**
     * Sets parameters in a prepared statement.
     * 
     * @param preparedStatement set parameters for this prepared statment
     * @param parameterIndex starting parameter index
     * @param row read parameters from this row object
     * 
     * @return last parameter index + 1
     * @throws TranslatorException if error
     */
    public int write(PreparedStatement preparedStatement, int parameterIndex, R row) throws TranslatorException
    {
        if (log.isDebugEnabled()) log.debug("write parameterIndex=" + parameterIndex + " row=" + row);
        int p = parameterIndex;
        
        try
        {
            for (ColumnTranslator<R> c: columnTranslatorList)
            {
                if (includeIdentityColumns || !c.isIdentity())
                {
                    if (log.isDebugEnabled()) log.debug("write parameter " + p);
                    c.write(preparedStatement, p, row);
                    ++p;
                }
            }
        }
        catch (Exception e)
        {
            throw new TranslatorException("error preparing parameter " + p, e);
        }
        
        return p;
    }
    
    
    /**
     * Adds a column translator to use in {@link #read(ResultSet, int, Object)} and
     * {@link #write(PreparedStatement, int, Object)}.
     * 
     * @param c column translator
     */
    public void addColumnTranslator(ColumnTranslator<R> c)
    {
        columnTranslatorList.add(c);
        columnTranslatorMap.put(c.getField().getName(), c);
    }

    
    /**
     * Gets a column translator associated with a field.
     * 
     * @param fieldName row class field name
     * @return column translator for column or null if none found
     */
    public ColumnTranslator<R> getColumnTranslator(String fieldName)
    {
        return columnTranslatorMap.get(fieldName);    
    }
    
    
    /**
     * Gets Java class type for row.
     * 
     * @return row class for this translator
     */
    public Class<R> getRowClass()
    {
        return rowClass;
    }

    
    /**
     * Gets sql phrase as list of columns. Typically used in select and insert statements.
     * 
     * @return "c1, c2, c3..."
     */
    public String createColumnPhrase()
    {
        StringBuilder phrase = new StringBuilder(columnTranslatorList.size() * 20);
        
        for (ColumnTranslator<R> c: columnTranslatorList)
        {
            if (includeIdentityColumns || !c.isIdentity())
            {
                phrase.append(c.getColumnName());
                phrase.append(", ");
            }
        }

        if (phrase.length() > 2)
        {
            // remove last delimiter
            phrase.setLength(phrase.length() - 2);
        }
        
        return phrase.toString();
    }
    
    
    /**
     * Creates sql phrase of column names with parameters . Typically used
     * by where clause and update statement.
     * 
     * @return "c1=?, c2=?, c3=?..."
     */
    public String createColumnParameterPhrase()
    {
        StringBuilder phrase = new StringBuilder(columnTranslatorList.size() * 20);
        
        for (ColumnTranslator<R> c: columnTranslatorList)
        {
            if (includeIdentityColumns || !c.isIdentity())
            {
                phrase.append(c.getColumnName());
                phrase.append("=?, ");
            }
        }

        if (phrase.length() > 2)
        {
            // remove last delimiter
            phrase.setLength(phrase.length() - 2);
        }
        
        return phrase.toString();
    }
    

    /**
     * Creates parameter placeholders for all columns. Typically used by insert statement.
     * 
     * @return "?, ?, ?..."
     */
    public String createParameterPhrase()
    {
        StringBuilder phrase = new StringBuilder(columnTranslatorList.size() * 3);
        
        for (ColumnTranslator<R> c: columnTranslatorList)
        {
            if (includeIdentityColumns || !c.isIdentity())
            {
                phrase.append("?");
                phrase.append(", ");
            }
        }
    
        if (phrase.length() > 2)
        {
            // remove last delimiter
            phrase.setLength(phrase.length() - 2);
        }
        
        return phrase.toString();
    }


    /**
     * Gets list of all column translators used by this translator.
     * 
     * @return all column translators for this instance
     */
    public List<ColumnTranslator<R>> getColumnTranslatorList()
    {
        return columnTranslatorList;
    }
}
