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
package org.sormula.tests.cache;

import java.util.Arrays;

import org.sormula.SormulaException;
import org.sormula.cache.CacheException;
import org.sormula.cache.DuplicateCacheException;
import org.sormula.operation.OperationException;
import org.sormula.operation.ScalarSelectOperation;
import org.sormula.reflect.FieldExtractor;
import org.sormula.reflect.ReflectException;
import org.sormula.tests.DatabaseTest;


/** 
 * Base class for all sormula cache tests. 
 * 
 * @author Jeff Miller
 */
public class CacheTest<R> extends DatabaseTest<R>
{
    FieldExtractor<R> primaryKeyExtractor;
    FieldExtractor<R> rowExtractor;
    ScalarSelectOperation<R> uncachedTable;

    
    @Override
    protected void createTable(Class<R> rowClass) throws Exception
    {
        super.createTable(rowClass);
        
        // for getting column and primary key values from a row
        try
        {
            primaryKeyExtractor = new FieldExtractor<R>(
                    getTable().getRowTranslator().getPrimaryKeyWhereTranslator().getColumnTranslatorList());
            rowExtractor = new FieldExtractor<R>(
                    getTable().getRowTranslator().getColumnTranslatorList());
        }
        catch (ReflectException e)
        {
            throw new CacheException("error creating primary key extractor", e);
        }
        
        // for selecting rows directly from database
        uncachedTable = new ScalarSelectOperation<R>(getTable());
        uncachedTable.setCached(false);
    }

    
    public ScalarSelectOperation<R> getUncachedTable()
    {
        return uncachedTable;
    }


    @Override
    public void closeDatabase()
    {
        super.closeDatabase();
    }
    
    
    protected void confirmCached(R row) throws SormulaException
    {
        // assume that row is cached if select gets the same row (==)
        Object[] primaryKeys = getPrimaryKeyValues(row);
        assert row == getTable().select(primaryKeys) : "row is not cached for " +
                Arrays.toString(primaryKeys);
    }
    
    
    protected void confirmNotCached(R row) throws SormulaException
    {
        // assume that row is not cached if select returns nothing or is not the same row (==)
        Object[] primaryKeys = getPrimaryKeyValues(row);
        R test = getTable().select(primaryKeys);
        assert test == null || test != row : "row is cached for " +
                Arrays.toString(primaryKeys);
    }
    
    
    protected void confirmInDatabase(R row) throws SormulaException
    {
        // assume row is in database if select is not null and all fields are equal
        Object[] primaryKeys = getPrimaryKeyValues(row);
        R uncachedRow = uncachedTable.select(primaryKeys);
        assert uncachedRow != null : "row is not in database for " + Arrays.toString(primaryKeys);
        assert equals(uncachedRow, row) : "database row not equal to test row for " +
                Arrays.toString(primaryKeys);
    }
    
    
    protected void confirmNotInDatabase(R row) throws SormulaException
    {
        // assume not in database if select returns null or some fields are not equal
        Object[] primaryKeys = getPrimaryKeyValues(row);
        R uncachedRow = uncachedTable.select(primaryKeys);
        assert uncachedRow == null || !equals(uncachedRow, row) : 
            "database row equal to test row for " + Arrays.toString(primaryKeys);
    }
    
    
    protected void confirmDuplicateException(R duplicate) throws SormulaException
    {
        try
        {
            assert getTable().insert(duplicate) == 1 : "duplicate inserted"; 
        }
        catch (OperationException e)
        {
            // look for duplicate cache exception
            Throwable t = e.getCause();
            while (t != null)
            {
                if (t instanceof DuplicateCacheException) break;
                else t = t.getCause();
            }
            
            if (t == null)
            {
                // did not find expected exception
                throw e;
            }
        }
    }
    
    
    protected Object[] getPrimaryKeyValues(R row) throws SormulaException
    {
        try
        {
            return primaryKeyExtractor.getFieldValues(row);
        }
        catch (ReflectException e)
        {
            throw new SormulaException("can't get primary key(s)", e);
        }
    }
    
    
    protected boolean equals(R row1, R row2) throws SormulaException
    {
        try
        {
            return Arrays.equals(rowExtractor.getFieldValues(row1), rowExtractor.getFieldValues(row2));
        }
        catch (ReflectException e)
        {
            throw new SormulaException("error comparing rows", e);
        }
    }
}